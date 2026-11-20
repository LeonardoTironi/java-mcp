package br.edu.ifpr.mcp_server.mcp.prompts;

import java.util.List;
import java.util.Locale;

import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.mcp.tools.LocationListTool;
import br.edu.ifpr.mcp_server.mcp.tools.PriceStatsTool;
import br.edu.ifpr.mcp_server.mcp.tools.PropertySearchTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpPromptDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

/**
 * Prompt {@value #NAME}: pede à IA um panorama do mercado imobiliário de uma
 * cidade (faixa de preços, bairros com mais ofertas e exemplos de imóveis
 * mais baratos e mais caros).
 */
public class MarketAnalysisPrompt implements McpPromptDefinition {
   public static final String NAME         = "analisar_mercado";
   static final int           MAX_CITY_LEN = 100;

   @Override
   public Prompt prompt() {
      // define o prompt e seu(s) argumento(s)
      return Prompt.builder(NAME)
                   .title("Analisar mercado")
                   .description("Panorama do mercado imobiliário de uma " +
                                "cidade: faixa de preços por tipo de imóvel, " +
                                "bairros com mais ofertas e exemplos dos " +
                                "imóveis mais baratos e mais caros.")
                   .arguments(List.of(
                         PromptArgument.builder("cidade")
                                       .title("Cidade")
                                       .description("Cidade a analisar, " +
                                                    "ex.: Curitiba.")
                                       .required(true)
                                       .build(),
                         PromptArgument.builder("finalidade")
                                       .title("Finalidade")
                                       .description("'comprar' ou 'alugar'. " +
                                                    "Se omitida, analisa " +
                                                    "venda e aluguel " +
                                                    "separadamente.")
                                       .required(false)
                                       .build()))
                   .build();
   }

   @Override
   public String render(ToolArguments args) {
      // recupera e valida o(s) argumento(s) passados pelo cliente
      String  city    = parseCity(args.optString("cidade"));
      Purpose purpose = args.optValue("finalidade",
                                      MarketAnalysisPrompt::parsePurpose);

      // monta o texto da mensagem; os trechos que dependem da finalidade
      // são gerados por "describe", "statsHint" e "searchHint" (abaixo)
      return """
             Faça uma análise do mercado imobiliário de %s, %s.

             1. Use a ferramenta '%s' (sem 'cidade') para confirmar o nome \
             exato da cidade. Se ela não existir, avise, sugira cidades \
             parecidas da lista e pare.
             2. Use '%s' com a cidade para descobrir os bairros e quantos \
             imóveis disponíveis há em cada um.
             3. Use '%s' com a cidade %s para obter os preços mínimo, médio \
             e máximo e a área média por tipo de imóvel.
             4. Use '%s' com a cidade%s, limite = 3, uma vez com \
             ordenarPor = preco_asc e outra com ordenarPor = preco_desc, \
             para mostrar exemplos dos extremos do mercado.

             Apresente o resultado assim:
             - Resumo em 2 ou 3 frases.
             - Tabela de preços por tipo de imóvel (quantidade, mínimo, \
             médio, máximo, área média e preço médio por m²).
             - Os 5 bairros com mais imóveis disponíveis.
             - Os imóveis mais baratos e mais caros encontrados (código, \
             título, bairro e preço).
             - Observações: onde há mais oferta, que tipo de imóvel é mais \
             acessível e o que chama a atenção nos dados.

             Calcule o preço por m² apenas a partir dos dados retornados. \
             Nunca invente imóveis, preços ou bairros que não tenham vindo \
             das ferramentas e deixe claro quando houver poucos imóveis \
             para tirar conclusões.
             """.formatted(city,describe(purpose),
                           LocationListTool.NAME,LocationListTool.NAME,
                           PriceStatsTool.NAME,statsHint(purpose),
                           PropertySearchTool.NAME,searchHint(purpose));
   }

   static String parseCity(String city) {
      // a cidade é obrigatória e tem tamanho limitado
      if (city == null)
         throw new IllegalArgumentException("Argumento 'cidade' é " +
                                            "obrigatório.");
      if (city.length() > MAX_CITY_LEN)
         throw new IllegalArgumentException("Argumento 'cidade' deve ter no " +
                                            "máximo " + MAX_CITY_LEN +
                                            " caracteres.");
      return city;
   }

   /** Aceita 'comprar'/'alugar' (ou SELL/RENT). */
   static Purpose parsePurpose(String text) {
      // aceita os termos em português e os nomes da enumeração
      return switch (text.toLowerCase(Locale.ROOT)) {
         case "comprar", "venda", "sell" -> Purpose.SELL;
         case "alugar", "aluguel", "locação", "locacao", "rent" -> Purpose.RENT;
         default -> throw new IllegalArgumentException(
                        "valor '" + text + "' inválido. Use 'comprar' ou " +
                        "'alugar'");
      };
   }

   private static String describe(Purpose purpose) {
      // complemento do título da análise
      if (purpose == null)
         return "comparando imóveis à venda e para alugar";
      return purpose == Purpose.SELL ? "para quem quer comprar"
                                     : "para quem quer alugar (valores mensais)";
   }

   private static String statsHint(Purpose purpose) {
      // sem finalidade, pede duas consultas para não misturar venda e
      // aluguel nas estatísticas
      if (purpose == null)
         return "duas vezes, uma com finalidade = SELL e outra com " +
                "finalidade = RENT (nunca misture venda e aluguel),";
      return "e finalidade = " + purpose.name();
   }

   private static String searchHint(Purpose purpose) {
      // complemento da instrução de busca de exemplos
      return purpose == null
                ? " (para venda e para aluguel separadamente)"
                : ", finalidade = " + purpose.name();
   }
}
