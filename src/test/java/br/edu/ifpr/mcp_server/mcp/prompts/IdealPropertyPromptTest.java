package br.edu.ifpr.mcp_server.mcp.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class IdealPropertyPromptTest {
   private final IdealPropertyPrompt prompt = new IdealPropertyPrompt();

   private String text(Map<String, Object> args) {
      // gera o prompt como o servidor faria ('exchange' nulo: o prompt não
      // conversa com o cliente)
      GetPromptResult result =
            prompt.get(null,new GetPromptRequest(IdealPropertyPrompt.NAME,args));

      // o prompt é uma única mensagem, escrita como se fosse do usuário
      assertEquals(1,result.messages().size());
      assertEquals(Role.USER,result.messages().get(0).role());
      return ((TextContent) result.messages().get(0).content()).text();
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam o prompt 'buscar_imovel_ideal':
   // - o argumento 'perfil' é opcional?
   // - o texto inclui o perfil e as ferramentas que o modelo deve usar?
   // - sem perfil, o texto pede os critérios ao usuário?
   // - um perfil longo demais é rejeitado?
   // ==========================================================================

   @Test
   void definicaoDeveTerPerfilOpcional() {
      Prompt definition = prompt.prompt();

      assertEquals("buscar_imovel_ideal",definition.name());
      assertEquals(1,definition.arguments().size());
      assertEquals("perfil",definition.arguments().get(0).name());
      assertFalse(definition.arguments().get(0).required());
   }

   @Test
   void deveIncluirPerfilEFerramentas() {
      String text = text(Map.of("perfil","casa em Curitiba até 900 mil"));

      assertTrue(text.contains("O que procuro: casa em Curitiba até 900 mil"));
      assertTrue(text.contains("'listar_localidades'"));
      assertTrue(text.contains("'buscar_imoveis'"));
      assertTrue(text.contains("'estatisticas_precos'"));
      assertTrue(text.contains("'detalhar_imovel'"));
      // sem resultados, o modelo pode ampliar o orçamento em até 10%
      assertTrue(text.contains("até 10%"));
   }

   @Test
   void semPerfilDevePedirCriterios() {
      assertTrue(text(Map.of()).contains("Ainda não informei o que procuro."));
   }

   @Test
   void perfilMuitoLongoDeveSerInvalido() {
      // um caractere acima do limite já deve ser rejeitado
      String  longo = "x".repeat(IdealPropertyPrompt.MAX_PROFILE_LEN + 1);
      McpError ex   = assertThrows(McpError.class,
                                   () -> text(Map.of("perfil",longo)));

      assertEquals(ErrorCodes.INVALID_PARAMS,ex.getJsonRpcError().code());
      assertTrue(ex.getMessage().contains("'perfil'"));
   }
}
