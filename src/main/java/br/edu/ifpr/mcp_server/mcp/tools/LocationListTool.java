package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.edu.ifpr.mcp_server.domain.LocationCount;
import br.edu.ifpr.mcp_server.mcp.utils.JsonSchema;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * Ferramenta {@value #NAME}: lista cidades (ou bairros de uma cidade) com a
 * quantidade de imóveis disponíveis.
 */
public class LocationListTool extends PropertyTool {
   public static final String NAME = "listar_localidades";

   public LocationListTool(PropertyRepository repository,
                           ToolResults results) {
      super(repository,results);
   }

   @Override
   public Tool tool() {
      // define o esquema para troca de dados (todos os parâmetros são
      // opcionais)
      Map<String, Object> inputSchema =
                          JsonSchema.object()
                                    .string("estado","Sigla da UF com 2 " +
                                            "letras, ex.: PR.")
                                    .string("cidade","Se informada, lista os " +
                                            "bairros desta cidade.")
                                    .build();
      // define a ferramenta
      return Tool.builder(NAME,inputSchema)
                 .title("Listar localidades")
                 .description("Lista as cidades com imóveis disponíveis ou, " +
                              "se 'cidade' for informada, os bairros dessa " +
                              "cidade, com a quantidade de imóveis em cada " +
                              "um. Use para confirmar os nomes de cidades e " +
                              "bairros antes de chamar 'buscar_imoveis'.")
                 // informa ao cliente que a ferramenta apenas consulta dados
                 .annotations(readOnly("Listar localidades"))
                 .build();
   }

   @Override
   protected CallToolResult execute(ToolArguments args) {
      // recupera o(s) parâmetro(s) passados pelo cliente, se houver algum
      String state = args.optValue("estado",LocationListTool::parseState);
      String city  = args.optString("cidade");

      // consulta as cidades ou, se 'cidade' foi informada, os bairros dela
      List<LocationCount> locations = repository.listLocations(state,city);

      // nenhuma localidade encontrada não é erro: apenas informa o cliente
      if (locations.isEmpty())
         return results.message("Nenhuma localidade com imóveis " +
                                "disponíveis encontrada.");
      // retorna o resultado da ferramenta ('tool') ao cliente
      return results.success(locations.size() + (city == null
                                                    ? " cidade(s)"
                                                    : " bairro(s)") +
                             " com imóveis disponíveis.",locations);
   }

   private static String parseState(String text) {
      // aceita apenas 2 letras e as converte em maiúsculas (ex.: pr -> PR)
      if (!text.matches("[A-Za-z]{2}"))
         throw new IllegalArgumentException("deve ser a sigla da UF com 2 " +
                                            "letras, ex.: PR");
      return text.toUpperCase(Locale.ROOT);
   }
}
