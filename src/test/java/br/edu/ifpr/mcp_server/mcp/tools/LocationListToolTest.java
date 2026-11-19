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

class LocationListToolTest {
   private final FakePropertyRepository repository =
                                                 new FakePropertyRepository();
   private final LocationListTool       tool       =
                                  new LocationListTool(repository,RESULTS);

   // ==========================================================================
   // Os métodos de teste a seguir verificam a ferramenta 'listar_localidades'
   // usando o repositório em memória (sem banco):
   // - sem argumentos, lista as cidades?
   // - com a cidade, lista os bairros (estado normalizado)?
   // - estado inválido é erro, sem consultar o repositório?
   // - lista vazia não é erro; banco fora do ar é?
   // ==========================================================================

   @Test
   void semArgumentosDeveListarCidades() {
      CallToolResult result = call(tool,Map.of());

      assertFalse(result.isError());
      // o repositório falso registra a chamada como "estado/cidade"
      assertEquals(List.of("null/null"),repository.calls);
      assertTrue(text(result).startsWith("1 cidade(s)"));
   }

   @Test
   void comCidadeDeveListarBairros() {
      CallToolResult result = call(tool,Map.of("estado","pr",
                                               "cidade","Curitiba"));

      // a sigla do estado chega ao repositório em maiúsculas
      assertEquals(List.of("PR/Curitiba"),repository.calls);
      assertTrue(text(result).startsWith("1 bairro(s)"));
   }

   @Test
   void estadoInvalidoDeveSerErro() {
      // o estado deve ser a sigla com duas letras, não o nome
      CallToolResult result = call(tool,Map.of("estado","Paraná"));

      assertTrue(result.isError());
      assertTrue(text(result).contains("'estado'"));
      // rejeitado antes de consultar o repositório
      assertTrue(repository.calls.isEmpty());
   }

   @Test
   void listaVaziaNaoEhErro() {
      repository.locations = List.of();
      CallToolResult result = call(tool,Map.of("estado","AC"));

      assertFalse(result.isError());
      assertTrue(text(result).startsWith("Nenhuma localidade"));
   }

   @Test
   void falhaDoBancoDeveVirarResultadoDeErro() {
      // simula o banco fora do ar: a ferramenta não lança exceção, retorna
      // um resultado com 'isError' verdadeiro
      repository.offline = true;

      assertTrue(call(tool,Map.of()).isError());
   }
}
