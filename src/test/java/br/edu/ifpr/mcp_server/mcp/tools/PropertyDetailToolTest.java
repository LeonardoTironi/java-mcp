package br.edu.ifpr.mcp_server.mcp.tools;

import static br.edu.ifpr.mcp_server.mcp.tools.ToolTestSupport.RESULTS;
import static br.edu.ifpr.mcp_server.mcp.tools.ToolTestSupport.call;
import static br.edu.ifpr.mcp_server.mcp.tools.ToolTestSupport.text;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

class PropertyDetailToolTest {
   private final FakePropertyRepository repository =
                                                 new FakePropertyRepository();
   private final PropertyDetailTool     tool       =
                                new PropertyDetailTool(repository,RESULTS);

   // ==========================================================================
   // Os métodos de teste a seguir verificam a ferramenta 'detalhar_imovel'
   // usando o repositório em memória (sem banco):
   // - a definição expõe os parâmetros 'codigo' e 'id'?
   // - o imóvel é detalhado pelo código ou pelo id?
   // - o código tem prioridade quando os dois são informados?
   // - sem código nem id, imóvel inexistente ou banco fora do ar são
   //   erros?
   // ==========================================================================

   @Test
   void definicaoDeveExporCodigoEId() {
      // os parâmetros ficam em 'properties' no JSON Schema de entrada
      Map<?, ?> properties = (Map<?, ?>) tool.tool().inputSchema()
                                             .get("properties");

      assertEquals("detalhar_imovel",tool.tool().name());
      assertEquals(List.of("codigo","id"),List.copyOf(properties.keySet()));
   }

   @Test
   void deveDetalharPorCodigo() {
      // código em minúsculas também é encontrado
      CallToolResult result = call(tool,Map.of("codigo","imv-0001"));
      String         text   = text(result);

      assertFalse(result.isError());
      assertTrue(text.startsWith("Imóvel IMV-0001: Casa no Batel"));
      assertTrue(text.contains("\"ownerName\" : \"Maria\""));
      assertTrue(text.contains("\"neighborhood\" : \"Batel\""));
   }

   @Test
   void deveDetalharPorId() {
      CallToolResult result = call(tool,Map.of("id","prop-001"));

      assertFalse(result.isError());
      assertEquals(List.of("id:prop-001"),repository.calls);
   }

   @Test
   void codigoTemPrioridadeSobreId() {
      call(tool,Map.of("codigo","IMV-0001","id","prop-999"));

      // apenas a busca por código foi feita; o id foi ignorado
      assertEquals(List.of("code:IMV-0001"),repository.calls);
   }

   @Test
   void semCodigoNemIdDeveSerErro() {
      // código só com espaços conta como não informado
      CallToolResult result = call(tool,Map.of("codigo","  "));

      assertTrue(result.isError());
      assertTrue(text(result).contains("'codigo' ou 'id'"));
      // rejeitado antes de consultar o repositório
      assertTrue(repository.calls.isEmpty());
   }

   @Test
   void imovelInexistenteDeveSerErro() {
      CallToolResult result = call(tool,Map.of("codigo","IMV-9999"));

      assertTrue(result.isError());
      // a mensagem cita o código procurado
      assertTrue(text(result).contains("IMV-9999"));
   }

   @Test
   void falhaDoBancoDeveVirarResultadoDeErro() {
      // simula o banco fora do ar: a ferramenta não lança exceção, retorna
      // um resultado com 'isError' verdadeiro
      repository.offline = true;

      assertTrue(call(tool,Map.of("id","prop-001")).isError());
   }
}
