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

import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

class PriceStatsToolTest {
   private final FakePropertyRepository repository =
                                                 new FakePropertyRepository();
   private final PriceStatsTool         tool       =
                                    new PriceStatsTool(repository,RESULTS);

   // ==========================================================================
   // Os métodos de teste a seguir verificam a ferramenta 'estatisticas_precos'
   // usando o repositório em memória (sem banco):
   // - os argumentos viram o filtro certo?
   // - sem finalidade, o resultado avisa que venda e aluguel se misturam?
   // - sem dados não é erro; argumentos inválidos e banco fora do ar são?
   // ==========================================================================

   @Test
   void deveConverterArgumentosNoFiltro() {
      CallToolResult result = call(tool,Map.of("cidade","Londrina",
                                               "tipo","APARTMENT",
                                               "finalidade","rent"));
      // o filtro que a ferramenta montou e enviou ao repositório
      PropertyFilter filter = (PropertyFilter) repository.calls.get(0);

      assertFalse(result.isError());
      assertEquals("Londrina",filter.city());
      assertEquals(PropertyType.APARTMENT,filter.type());
      assertEquals(Purpose.RENT,filter.purpose());
      assertTrue(text(result).contains("para locação"));
      assertTrue(text(result).contains("\"avgPrice\" : 675000.00"));
   }

   @Test
   void semFinalidadeDeveAvisarMistura() {
      assertTrue(text(call(tool,Map.of())).contains("venda e aluguel juntos"));
   }

   @Test
   void semDadosNaoEhErro() {
      repository.stats = List.of();
      CallToolResult result = call(tool,Map.of("tipo","FARM"));

      assertFalse(result.isError());
      assertTrue(text(result).startsWith("Não há imóveis"));
   }

   @Test
   void argumentosInvalidosDevemSerErro() {
      assertTrue(call(tool,Map.of("tipo","mansão")).isError());
      // 'BOTH' existe no banco, mas não é aceito como filtro
      assertTrue(call(tool,Map.of("finalidade","BOTH")).isError());
      // rejeitados antes de consultar o repositório
      assertTrue(repository.calls.isEmpty());
   }

   @Test
   void falhaDoBancoDeveVirarResultadoDeErro() {
      // simula o banco fora do ar: a ferramenta não lança exceção, retorna
      // um resultado com 'isError' verdadeiro
      repository.offline = true;

      assertTrue(call(tool,Map.of()).isError());
   }
}
