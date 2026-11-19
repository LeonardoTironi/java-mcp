package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.Map;
import java.util.Optional;

import br.edu.ifpr.mcp_server.domain.PropertyDetail;
import br.edu.ifpr.mcp_server.mcp.utils.JsonSchema;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * Ferramenta {@value #NAME}: retorna todos os dados de um imóvel a partir do
 * id ou do código.
 */
public class PropertyDetailTool extends PropertyTool {
   public static final String NAME = "detalhar_imovel";

   public PropertyDetailTool(PropertyRepository repository,
                             ToolResults results) {
      super(repository,results);
   }

   @Override
   public Tool tool() {
      // define o esquema para troca de dados
      //
      // OBSERVAÇÃO:
      // nenhum parâmetro é marcado como obrigatório, pois basta informar um
      // dos dois; a verificação é feita em "execute" (abaixo)
      //
      Map<String, Object> inputSchema =
                          JsonSchema.object()
                                    .string("codigo","Código do imóvel, ex.: " +
                                            "IMV-0001 (preferencial).")
                                    .string("id","Identificador interno do " +
                                            "imóvel, retornado por " +
                                            "'buscar_imoveis'.")
                                    .build();
      // define a ferramenta
      return Tool.builder(NAME,inputSchema)
                 .title("Detalhar imóvel")
                 .description("Retorna todos os dados de um imóvel: " +
                              "descrição, preços, áreas, características " +
                              "(quartos, banheiros, vagas...), endereço, " +
                              "proprietário e condomínio. Informe 'codigo' " +
                              "ou 'id'.")
                 // informa ao cliente que a ferramenta apenas consulta dados
                 .annotations(readOnly("Detalhar imóvel"))
                 .build();
   }

   @Override
   protected CallToolResult execute(ToolArguments args) {
      // recupera o(s) parâmetro(s) passados pelo cliente
      String code = args.optString("codigo");
      String id   = args.optString("id");

      // ao menos um dos dois deve ser informado
      if (code == null && id == null)
         return results.error("Informe o parâmetro 'codigo' ou 'id' do " +
                              "imóvel.");

      // busca o imóvel no banco de dados (o código tem preferência)
      Optional<PropertyDetail> detail = code != null
                                           ? repository.findByCode(code)
                                           : repository.findById(id);

      // retorna o resultado da ferramenta ('tool') ao cliente: os dados do
      // imóvel ou um erro, se ele não for encontrado
      return detail.map(d -> results.success("Imóvel " + d.code() + ": " +
                                             d.title(),d))
                   .orElseGet(() -> results.error(
                         "Imóvel " + (code != null ? "com código '" + code
                                                   : "com id '" + id) +
                         "' não encontrado ou indisponível."));
   }
}
