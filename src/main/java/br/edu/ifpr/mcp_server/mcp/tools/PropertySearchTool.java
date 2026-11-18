package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertySummary;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.domain.SortOrder;
import br.edu.ifpr.mcp_server.domain.UsageType;
import br.edu.ifpr.mcp_server.mcp.utils.JsonSchema;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * Ferramenta {@value #NAME}: busca imóveis disponíveis com filtros opcionais.
 */
public class PropertySearchTool extends PropertyTool {
   public static final String NAME = "buscar_imoveis";

   public PropertySearchTool(PropertyRepository repository,
                             ToolResults results) {
      super(repository,results);
   }

   @Override
   public Tool tool() {
      // define a ferramenta (o esquema para troca de dados é montado em
      // "schema()", abaixo)
      return Tool.builder(NAME,schema())
                 .title("Buscar imóveis")
                 .description("Busca imóveis disponíveis (à venda ou para " +
                              "alugar) conforme filtros opcionais e retorna " +
                              "um resumo de cada imóvel encontrado. Use " +
                              "'listar_localidades' para descobrir cidades e " +
                              "bairros existentes e 'detalhar_imovel' para " +
                              "ver todos os dados de um imóvel.")
                 // informa ao cliente que a ferramenta apenas consulta dados
                 .annotations(readOnly("Buscar imóveis"))
                 .build();
   }

   private static Map<String, Object> schema() {
      // define o esquema para troca de dados (todos os parâmetros são
      // opcionais)
      //
      // OBSERVAÇÃO:
      // os limites informados aqui (mínimo e máximo) orientam a IA, mas são
      // validados novamente em "toFilter" (abaixo), pois o cliente pode
      // enviar qualquer valor
      //
      return JsonSchema.object()
            .string("texto","Palavras procuradas no título, descrição, " +
                            "bairro e características (ex.: piscina, " +
                            "churrasqueira).")
            .enumeration("tipo","Tipo do imóvel.",PropertyType.values())
            .enumeration("finalidade","'comprar' = SELL, 'alugar' = RENT. " +
                         "Imóveis anunciados para venda e locação aparecem " +
                         "nos dois casos.",Purpose.SELL,Purpose.RENT)
            .enumeration("uso","Uso do imóvel.",UsageType.values())
            .string("cidade","Nome (ou parte do nome) da cidade, ex.: " +
                             "Curitiba.")
            .string("bairro","Nome (ou parte do nome) do bairro.")
            .string("estado","Sigla da UF com 2 letras, ex.: PR.")
            .number("precoMin","Preço mínimo em reais (R$).")
            .number("precoMax","Preço máximo em reais (R$). Para aluguel, " +
                               "é o valor mensal.")
            .number("areaMin","Área mínima em m².")
            .number("areaMax","Área máxima em m².")
            .integer("quartosMin","Quantidade mínima de quartos.",0,20)
            .integer("vagasMin","Quantidade mínima de vagas de garagem.",0,20)
            .bool("destaque","true para retornar apenas imóveis em destaque.")
            .choice("ordenarPor","Ordenação dos resultados: " +
                    describeSortOrders() + ".",SortOrder.keys())
            .integer("limite","Quantidade máxima de imóveis retornados " +
                     "(padrão " + PropertyFilter.DEFAULT_LIMIT + ").",1,
                     PropertyFilter.MAX_LIMIT)
            .build();
   }

   private static String describeSortOrders() {
      // gera o texto "chave = descrição" de cada ordenação disponível
      return String.join(", ",
                         Arrays.stream(SortOrder.values())
                                         .map(o -> o.key() + " = " + o.label())
                                         .toList());
   }

   @Override
   protected CallToolResult execute(ToolArguments args) {
      // converte o(s) parâmetro(s) passados pelo cliente em um filtro e
      // realiza a busca no banco de dados
      List<PropertySummary> found = repository.search(toFilter(args));

      // nenhum imóvel encontrado não é erro: informa o cliente e sugere
      // como ampliar a busca
      if (found.isEmpty())
         return results.message("Nenhum imóvel encontrado com esses " +
                                "critérios. Tente ampliar a busca (por " +
                                "exemplo, remover filtros de preço, área ou " +
                                "bairro).");
      // retorna o resultado da ferramenta ('tool') ao cliente
      return results.success(found.size() == 1
                                ? "1 imóvel encontrado."
                                : found.size() + " imóveis encontrados.",
                             found);
   }

   /** Converte os argumentos MCP no filtro do repositório. */
   static PropertyFilter toFilter(ToolArguments args) {
      // recupera o(s) parâmetro(s) passados pelo cliente, se houver algum
      Purpose purpose = args.optEnum("finalidade",Purpose.class);

      // o esquema não oferece BOTH, mas o cliente pode enviá-lo mesmo assim
      if (purpose == Purpose.BOTH)
         throw new IllegalArgumentException("Parâmetro 'finalidade' deve ser " +
                                            "SELL ou RENT.");
      // monta o filtro; parâmetros ausentes ficam nulos e são ignorados
      return PropertyFilter.builder()
                           .text(args.optString("texto"))
                           .type(args.optEnum("tipo",PropertyType.class))
                           .purpose(purpose)
                           .usageType(args.optEnum("uso",UsageType.class))
                           .city(args.optString("cidade"))
                           .neighborhood(args.optString("bairro"))
                           .state(args.optString("estado"))
                           .minPrice(args.optDecimal("precoMin"))
                           .maxPrice(args.optDecimal("precoMax"))
                           .minArea(args.optDecimal("areaMin"))
                           .maxArea(args.optDecimal("areaMax"))
                           .minBedrooms(args.optInteger("quartosMin",0,20))
                           .minParkingSpots(args.optInteger("vagasMin",0,20))
                           .featured(args.optBoolean("destaque"))
                           .sortOrder(args.optValue("ordenarPor",
                                                    SortOrder::fromKey))
                           .limit(args.optInteger("limite",1,
                                                  PropertyFilter.MAX_LIMIT))
                           .build();
   }
}
