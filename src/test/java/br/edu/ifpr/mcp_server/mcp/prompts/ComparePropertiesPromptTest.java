package br.edu.ifpr.mcp_server.mcp.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class ComparePropertiesPromptTest {
   private final ComparePropertiesPrompt prompt =
                                               new ComparePropertiesPrompt();

   private String text(Map<String, Object> args) {
      // gera o prompt como o servidor faria ('exchange' nulo: o prompt não
      // conversa com o cliente) e retorna o texto da primeira mensagem
      return ((TextContent) prompt.get(null,
                                       new GetPromptRequest(
                                             ComparePropertiesPrompt.NAME,args))
                                  .messages().get(0).content()).text();
   }

   private void assertInvalid(Map<String, Object> args, String fragment) {
      // argumento inválido em prompt é erro de protocolo (exceção), com o
      // código JSON-RPC de parâmetros inválidos e uma mensagem explicativa
      McpError ex = assertThrows(McpError.class,() -> text(args));

      assertEquals(ErrorCodes.INVALID_PARAMS,ex.getJsonRpcError().code());
      assertTrue(ex.getMessage().contains(fragment),ex.getMessage());
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam o prompt 'comparar_imoveis':
   // - o argumento 'codigos' é obrigatório?
   // - os códigos são normalizados (maiúsculas, sem repetição)?
   // - o texto orienta o uso da ferramenta 'detalhar_imovel'?
   // - quantidade fora de 2 a 5 ou caracteres estranhos são rejeitados?
   // ==========================================================================

   @Test
   void definicaoDeveTerCodigosObrigatorio() {
      assertEquals("comparar_imoveis",prompt.prompt().name());
      assertTrue(prompt.prompt().arguments().get(0).required());
   }

   @Test
   void deveListarCodigosNormalizadosEFerramenta() {
      // aceita vírgula, ponto e vírgula e espaços como separadores
      String text = text(Map.of("codigos","imv-0001, IMV-0004;IMV-0007"));

      assertTrue(text.contains("IMV-0001, IMV-0004, IMV-0007"));
      assertTrue(text.contains("'detalhar_imovel'"));
   }

   @Test
   void deveRemoverDuplicadosMantendoOrdem() {
      assertEquals(List.of("IMV-0004","IMV-0001"),
                   ComparePropertiesPrompt.parseCodes(
                                       "IMV-0004 imv-0004,IMV-0001"));
   }

   @Test
   void codigosAusentesOuInsuficientesSaoInvalidos() {
      assertInvalid(Map.of(),"obrigatório");
      assertInvalid(Map.of("codigos","IMV-0001"),"de 2 a 5");
      // o mesmo código repetido conta uma vez só
      assertInvalid(Map.of("codigos","IMV-0001,imv-0001"),"de 2 a 5");
      assertInvalid(Map.of("codigos","A,B,C,D,E,F"),"de 2 a 5");
   }

   @Test
   void codigoComCaracteresEstranhosEhInvalido() {
      // impede que texto arbitrário seja injetado nas instruções do prompt
      assertInvalid(Map.of("codigos","IMV-0001, x' OR 1=1 --"),
                    "Código inválido");
   }
}
