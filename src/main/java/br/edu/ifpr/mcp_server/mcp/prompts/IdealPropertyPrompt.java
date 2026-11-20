package br.edu.ifpr.mcp_server.mcp.prompts;

import java.util.List;

import br.edu.ifpr.mcp_server.mcp.tools.LocationListTool;
import br.edu.ifpr.mcp_server.mcp.tools.PriceStatsTool;
import br.edu.ifpr.mcp_server.mcp.tools.PropertyDetailTool;
import br.edu.ifpr.mcp_server.mcp.tools.PropertySearchTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpPromptDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

/**
 * Prompt {@value #NAME}: conduz a IA em uma conversa para encontrar o imóvel
 * ideal, coletando os critérios do usuário e usando as ferramentas do
 * servidor.
 */
public class IdealPropertyPrompt implements McpPromptDefinition {
   public static final String NAME            = "buscar_imovel_ideal";
   static final int           MAX_PROFILE_LEN = 500;

   @Override
   public Prompt prompt() {
      // define o prompt e seu(s) argumento(s)
      return Prompt.builder(NAME)
                   .title("Buscar imóvel ideal")
                   .description("Ajuda o usuário a encontrar o imóvel ideal: " +
                                "pergunta os critérios que faltam, consulta " +
                                "o banco de imóveis e apresenta as melhores " +
                                "opções.")
                   .arguments(List.of(
                         // opcional: sem ele, a IA pergunta os critérios
                         PromptArgument.builder("perfil")
                                       .title("Perfil")
                                       .description("O que o usuário procura, " +
                                                    "em texto livre (ex.: " +
                                                    "casal com 2 filhos, " +
                                                    "quer comprar casa em " +
                                                    "Curitiba até R$ 900 mil).")
                                       .required(false)
                                       .build()))
                   .build();
   }

   @Override
   public String render(ToolArguments args) {
      // recupera o argumento passado pelo cliente, se houver algum
      String profile = args.optString("perfil");

      // limita o tamanho do texto livre incluído na mensagem
      if (profile != null && profile.length() > MAX_PROFILE_LEN)
         throw new IllegalArgumentException("Argumento 'perfil' deve ter no " +
                                            "máximo " + MAX_PROFILE_LEN +
                                            " caracteres.");
      // monta o texto da mensagem, preenchendo o perfil e os nomes das
      // ferramentas que a IA deve usar
      //
      // OBSERVAÇÃO:
      // em "formatted", o símbolo de porcentagem literal é escrito "%%"
      //
      return """
             Você é um corretor de imóveis atencioso. Ajude-me a encontrar o \
             imóvel ideal usando apenas os dados do servidor de imóveis.

             %s

             Siga estes passos:
             1. Verifique se já sabe: cidade (e bairro, se houver \
             preferência), finalidade (comprar ou alugar), tipo de imóvel, \
             orçamento máximo e número mínimo de quartos. Pergunte apenas o \
             que faltar, tudo em uma única mensagem.
             2. Use a ferramenta '%s' para confirmar os nomes exatos da \
             cidade e do bairro.
             3. Se eu não souber quanto custa, use '%s' para mostrar a faixa \
             de preços da região.
             4. Use '%s' com os critérios (ordenarPor = preco_asc e \
             limite = 5). Se não houver resultados, amplie a busca aos \
             poucos (remova o bairro, aumente o orçamento em até 10%%) e \
             explique o que mudou.
             5. Apresente até 3 opções em uma tabela com código, título, \
             bairro, preço, área e quartos, e diga por que cada uma combina \
             com o meu perfil.
             6. Ofereça ver os detalhes de qualquer opção com '%s'.

             Nunca invente imóveis, preços ou características que não \
             tenham vindo das ferramentas.
             """.formatted(profile == null
                              ? "Ainda não informei o que procuro."
                              : "O que procuro: " + profile,
                           LocationListTool.NAME,PriceStatsTool.NAME,
                           PropertySearchTool.NAME,PropertyDetailTool.NAME);
   }
}
