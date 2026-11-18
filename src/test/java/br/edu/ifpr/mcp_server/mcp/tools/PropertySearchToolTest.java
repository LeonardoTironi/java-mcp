package br.edu.ifpr.mcp_server.mcp.tools;

import static br.edu.ifpr.mcp_server.mcp.tools.ToolTestSupport.RESULTS;
import static br.edu.ifpr.mcp_server.mcp.tools.ToolTestSupport.call;
import static br.edu.ifpr.mcp_server.mcp.tools.ToolTestSupport.text;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.domain.SortOrder;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

class PropertySearchToolTest {
   private final FakePropertyRepository repository =
                                                 new FakePropertyRepository();
   private final PropertySearchTool     tool       =
                                new PropertySearchTool(repository,RESULTS);

   // ==========================================================================
   // Os métodos de teste a seguir verificam a ferramenta 'buscar_imoveis'
   // usando o repositório em memória (sem banco):
   // - a definição expõe o nome e os parâmetros, todos opcionais?
   // - os argumentos viram o filtro certo; sem eles, valem os padrões?
   // - o resultado traz um resumo e o JSON; lista vazia não é erro?
   // - argumentos inválidos e banco fora do ar são erros?
   // ==========================================================================

   @Test
   void definicaoDeveExporNomeEParametrosOpcionais() {
      Tool definition = tool.tool();

      assertEquals("buscar_imoveis",definition.name());
      // indica ao cliente que a ferramenta apenas lê, não altera dados
      assertTrue(definition.annotations().readOnlyHint());
      // sem a chave 'required': nenhum parâmetro é obrigatório
      assertNull(definition.inputSchema().get("required"));
      Map<?, ?> properties = (Map<?, ?>) definition.inputSchema()
                                                   .get("properties");
      Map<?, ?> tipo       = (Map<?, ?>) properties.get("tipo");

      assertEquals(16,properties.size());
      // os parâmetros mantêm a ordem de declaração: 'texto' é o primeiro
      assertEquals("texto",properties.keySet().iterator().next());
      // a descrição explica cada valor aceito ao modelo
      assertTrue(tipo.get("description").toString().contains("HOUSE = casa"));
      // 'BOTH' não é oferecido como filtro
      assertEquals(List.of("SELL","RENT"),
                   ((Map<?, ?>) properties.get("finalidade")).get("enum"));
   }

   @Test
   void deveConverterArgumentosNoFiltro() {
      // valores como chegam em JSON: textos em qualquer caixa e números
      // decimais (3.0) mesmo onde se espera inteiro
      CallToolResult result =
            call(tool,Map.of("tipo","house","finalidade","SELL",
                             "cidade","Curitiba","estado","pr",
                             "precoMax",900000.0,"quartosMin",3.0,
                             "ordenarPor","preco_asc","limite",5));
      // o filtro que a ferramenta montou e enviou ao repositório
      PropertyFilter filter = (PropertyFilter) repository.calls.get(0);

      assertFalse(result.isError());
      assertEquals(PropertyType.HOUSE,filter.type());
      assertEquals(Purpose.SELL,filter.purpose());
      assertEquals("Curitiba",filter.city());
      assertEquals("PR",filter.state());
      // compareTo ignora a escala: 900000.0 e 900000 são o mesmo valor
      assertEquals(0,new BigDecimal("900000").compareTo(filter.maxPrice()));
      assertEquals(3,filter.minBedrooms());
      assertEquals(SortOrder.PRICE_ASC,filter.sortOrder());
      assertEquals(5,filter.limit());
   }

   @Test
   void semArgumentosDeveUsarPadroes() {
      call(tool,Map.of());
      PropertyFilter filter = (PropertyFilter) repository.calls.get(0);

      assertEquals(PropertyFilter.DEFAULT_LIMIT,filter.limit());
      assertEquals(SortOrder.FEATURED,filter.sortOrder());
      assertNull(filter.type());
   }

   @Test
   void sucessoDeveRetornarResumoEJson() {
      CallToolResult result = call(tool,Map.of());
      String         text   = text(result);

      assertFalse(result.isError());
      assertTrue(text.startsWith("1 imóvel encontrado."));
      assertTrue(text.contains("IMV-0001"));
   }

   @Test
   void resultadoVazioNaoEhErro() {
      // não encontrar imóveis é uma resposta válida, não uma falha
      repository.summaries = List.of();
      CallToolResult result = call(tool,Map.of("cidade","Atlântida"));

      assertFalse(result.isError());
      assertTrue(text(result).startsWith("Nenhum imóvel encontrado"));
   }

   @Test
   void tipoInvalidoDeveRetornarErroSemConsultar() {
      CallToolResult result = call(tool,Map.of("tipo","castelo"));

      assertTrue(result.isError());
      // a mensagem cita o parâmetro errado, para o modelo corrigir
      assertTrue(text(result).contains("'tipo'"));
      // rejeitado antes de consultar o repositório
      assertTrue(repository.calls.isEmpty());
   }

   @Test
   void finalidadeBothDeveSerRejeitada() {
      CallToolResult result = call(tool,Map.of("finalidade","BOTH"));

      assertTrue(result.isError());
      assertTrue(text(result).contains("'finalidade'"));
   }

   @Test
   void faixaDePrecoInvertidaDeveSerErro() {
      CallToolResult result = call(tool,Map.of("precoMin",500,"precoMax",100));

      assertTrue(result.isError());
      assertTrue(text(result).contains("preço"));
   }

   @Test
   void limiteForaDoIntervaloDeveSerErro() {
      assertTrue(call(tool,Map.of("limite",500)).isError());
      // ordenação desconhecida também é rejeitada
      assertTrue(call(tool,Map.of("ordenarPor","aleatorio")).isError());
   }

   @Test
   void falhaDoBancoDeveVirarResultadoDeErro() {
      // simula o banco fora do ar: a ferramenta não lança exceção, retorna
      // um resultado com 'isError' verdadeiro
      repository.offline = true;
      CallToolResult result = call(tool,Map.of());

      assertTrue(result.isError());
      assertTrue(text(result).contains("banco de dados"));
   }
}
