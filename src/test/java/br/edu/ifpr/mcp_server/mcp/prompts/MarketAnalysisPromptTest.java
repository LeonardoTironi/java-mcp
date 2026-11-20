package br.edu.ifpr.mcp_server.mcp.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.domain.Purpose;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class MarketAnalysisPromptTest {
   private final MarketAnalysisPrompt prompt = new MarketAnalysisPrompt();

   private String text(Map<String, Object> args) {
      // gera o prompt como o servidor faria ('exchange' nulo: o prompt não
      // conversa com o cliente) e retorna o texto da primeira mensagem
      return ((TextContent) prompt.get(null,
                                       new GetPromptRequest(
                                             MarketAnalysisPrompt.NAME,args))
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
   // Os métodos de teste a seguir verificam o prompt 'analisar_mercado':
   // - 'cidade' é obrigatória e 'finalidade' é opcional?
   // - o texto inclui a cidade e as ferramentas que o modelo deve usar?
   // - sem finalidade, venda e aluguel são analisados separadamente?
   // - sinônimos em português ("comprar", "aluguel") são aceitos?
   // - argumentos ausentes, longos ou desconhecidos são rejeitados?
   // ==========================================================================

   @Test
   void definicaoDeveTerCidadeObrigatoriaEFinalidadeOpcional() {
      Prompt definition = prompt.prompt();

      assertEquals("analisar_mercado",definition.name());
      assertEquals(2,definition.arguments().size());
      assertEquals("cidade",definition.arguments().get(0).name());
      assertTrue(definition.arguments().get(0).required());
      assertEquals("finalidade",definition.arguments().get(1).name());
      assertFalse(definition.arguments().get(1).required());
   }

   @Test
   void deveIncluirCidadeEFerramentas() {
      String text = text(Map.of("cidade","Curitiba"));

      assertTrue(text.contains("mercado imobiliário de Curitiba"));
      assertTrue(text.contains("'listar_localidades'"));
      assertTrue(text.contains("'estatisticas_precos'"));
      assertTrue(text.contains("'buscar_imoveis'"));
      assertTrue(text.contains("preco_asc"));
      assertTrue(text.contains("preco_desc"));
   }

   @Test
   void semFinalidadeDeveSepararVendaEAluguel() {
      String text = text(Map.of("cidade","Curitiba"));

      // preços de venda e de aluguel não podem entrar na mesma média
      assertTrue(text.contains("finalidade = SELL"));
      assertTrue(text.contains("finalidade = RENT"));
      assertTrue(text.contains("nunca misture venda e aluguel"));
   }

   @Test
   void comFinalidadeDeveUsarApenasEla() {
      String text = text(Map.of("cidade","Curitiba","finalidade","alugar"));

      assertTrue(text.contains("para quem quer alugar"));
      assertTrue(text.contains("finalidade = RENT"));
      assertFalse(text.contains("finalidade = SELL"));
   }

   @Test
   void deveAceitarSinonimosDeFinalidade() {
      assertEquals(Purpose.SELL,MarketAnalysisPrompt.parsePurpose("Comprar"));
      assertEquals(Purpose.SELL,MarketAnalysisPrompt.parsePurpose("SELL"));
      assertEquals(Purpose.RENT,MarketAnalysisPrompt.parsePurpose("aluguel"));
      assertEquals(Purpose.RENT,MarketAnalysisPrompt.parsePurpose("rent"));
   }

   @Test
   void argumentosInvalidos() {
      // ausente ou só com espaços: tratada como não informada
      assertInvalid(Map.of(),"'cidade' é obrigatório");
      assertInvalid(Map.of("cidade","   "),"'cidade' é obrigatório");
      assertInvalid(Map.of("cidade","x".repeat(
                                MarketAnalysisPrompt.MAX_CITY_LEN + 1)),
                    "no máximo");
      assertInvalid(Map.of("cidade","Curitiba","finalidade","trocar"),
                    "'finalidade'");
   }
}
