package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.domain.PriceStats;
import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.mcp.utils.JsonSchema;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * Ferramenta {@value #NAME}: preço mínimo, médio e máximo e área média dos
 * imóveis disponíveis, agrupados por tipo.
 */
public class PriceStatsTool extends PropertyTool {
   public static final String NAME = "estatisticas_precos";

   public PriceStatsTool(PropertyRepository repository, ToolResults results) {
      super(repository,results);
   }

   @Override
   public Tool tool() {
      // define o esquema para troca de dados (todos os parâmetros são
      // opcionais)
      Map<String, Object> inputSchema =
                          JsonSchema.object()
                                    .string("cidade","Nome (ou parte do " +
                                            "nome) da cidade.")
                                    .enumeration("tipo","Tipo do imóvel.",
                                                 PropertyType.values())
                                    // BOTH fica de fora: venda e aluguel
                                    // têm preços em escalas diferentes
                                    .enumeration("finalidade","'comprar' = " +
                                                 "SELL, 'alugar' = RENT " +
                                                 "(preços mensais).",
                                                 Purpose.SELL,Purpose.RENT)
                                    .build();
      // define a ferramenta
      return Tool.builder(NAME,inputSchema)
                 .title("Estatísticas de preços")
                 .description("Calcula, por tipo de imóvel, a quantidade de " +
                              "imóveis disponíveis e os preços mínimo, médio " +
                              "e máximo (R$) e a área média (m²). Responde " +
                              "perguntas como 'quanto custa uma casa em " +
                              "Curitiba?'. Informe 'finalidade' para não " +
                              "misturar preços de venda e de aluguel.")
                 // informa ao cliente que a ferramenta apenas consulta dados
                 .annotations(readOnly("Estatísticas de preços"))
                 .build();
   }

   @Override
   protected CallToolResult execute(ToolArguments args) {
      // recupera o(s) parâmetro(s) passados pelo cliente, se houver algum
      Purpose purpose = args.optEnum("finalidade",Purpose.class);

      // o esquema não oferece BOTH, mas o cliente pode enviá-lo mesmo assim
      if (purpose == Purpose.BOTH)
         throw new IllegalArgumentException("Parâmetro 'finalidade' deve ser " +
                                            "SELL ou RENT.");

      // monta o filtro e calcula as estatísticas no banco de dados
      PropertyFilter   filter = PropertyFilter.builder()
                                     .city(args.optString("cidade"))
                                     .type(args.optEnum("tipo",
                                                        PropertyType.class))
                                     .purpose(purpose)
                                     .build();
      List<PriceStats> stats  = repository.priceStats(filter);

      // nenhum imóvel encontrado não é erro: apenas informa o cliente
      if (stats.isEmpty())
         return results.message("Não há imóveis disponíveis para calcular " +
                                "estatísticas com esses critérios.");
      // retorna o resultado da ferramenta ('tool') ao cliente
      return results.success("Estatísticas de preço (R$) e área (m²) por " +
                             "tipo de imóvel" +
                             (purpose == null
                                 ? " (venda e aluguel juntos)."
                                 : " para " + purpose.label() + "."),
                             stats);
   }
}
