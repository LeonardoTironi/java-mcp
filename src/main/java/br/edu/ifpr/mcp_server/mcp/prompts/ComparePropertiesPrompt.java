package br.edu.ifpr.mcp_server.mcp.prompts;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import br.edu.ifpr.mcp_server.mcp.tools.PropertyDetailTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpPromptDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;

/**
 * Prompt {@value #NAME}: pede à IA que detalhe e compare de 2 a
 * {@value #MAX_CODES} imóveis em uma tabela.
 */
public class ComparePropertiesPrompt implements McpPromptDefinition {
   public static final String NAME      = "comparar_imoveis";
   static final int           MIN_CODES = 2;
   static final int           MAX_CODES = 5;

   /** Códigos aceitos: letras, dígitos e hífen (ex.: IMV-0001). */
   private static final String CODE_PATTERN = "[A-Za-z0-9-]{1,30}";

   @Override
   public Prompt prompt() {
      // define o prompt e seu(s) argumento(s)
      return Prompt.builder(NAME)
                   .title("Comparar imóveis")
                   .description("Compara de " + MIN_CODES + " a " + MAX_CODES +
                                " imóveis lado a lado (preço, área, quartos, " +
                                "localização e custo por m²).")
                   .arguments(List.of(
                         // os códigos chegam juntos, em um único texto
                         PromptArgument.builder("codigos")
                                       .title("Códigos")
                                       .description("Códigos dos imóveis " +
                                                    "separados por vírgula, " +
                                                    "ex.: IMV-0001, IMV-0004.")
                                       .required(true)
                                       .build()))
                   .build();
   }

   @Override
   public String render(ToolArguments args) {
      // recupera e valida o argumento passado pelo cliente
      List<String> codes = parseCodes(args.optString("codigos"));

      // monta o texto da mensagem, preenchendo os códigos e o nome da
      // ferramenta que a IA deve usar
      return """
             Compare os imóveis com os códigos: %s.

             1. Use a ferramenta '%s' com o parâmetro 'codigo' para cada um \
             deles. Se algum não for encontrado, avise e compare os demais.
             2. Monte uma tabela com uma coluna por imóvel e as linhas: tipo, \
             finalidade, cidade/bairro, preço, área, preço por m², quartos, \
             banheiros, vagas e condomínio.
             3. Destaque as principais vantagens e desvantagens de cada um e \
             termine com uma recomendação curta, explicando para que perfil \
             cada imóvel é mais indicado.

             Use apenas os dados retornados pela ferramenta.
             """.formatted(String.join(", ",codes),PropertyDetailTool.NAME);
   }

   /** Separa, valida e remove duplicados, mantendo a ordem informada. */
   static List<String> parseCodes(String text) {
      if (text == null)
         throw new IllegalArgumentException("Argumento 'codigos' é " +
                                            "obrigatório.");

      // LinkedHashSet descarta códigos repetidos e mantém a ordem informada
      Set<String> codes = new LinkedHashSet<>();

      // aceita vírgula, ponto e vírgula ou espaço como separador
      for (String code : Arrays.stream(text.split("[,;\\s]+"))
                               .filter(c -> !c.isBlank()).toList()) {
         if (!code.matches(CODE_PATTERN))
            throw new IllegalArgumentException("Código inválido: '" + code +
                                               "'. Use letras, números e " +
                                               "hífen, ex.: IMV-0001.");
         codes.add(code.toUpperCase(Locale.ROOT));
      }
      // verifica a quantidade de códigos diferentes
      if (codes.size() < MIN_CODES || codes.size() > MAX_CODES)
         throw new IllegalArgumentException("Informe de " + MIN_CODES +
                                            " a " + MAX_CODES + " códigos " +
                                            "diferentes em 'codigos'.");
      return List.copyOf(codes);
   }
}
