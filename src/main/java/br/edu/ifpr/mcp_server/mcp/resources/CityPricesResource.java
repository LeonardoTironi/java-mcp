package br.edu.ifpr.mcp_server.mcp.resources;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import br.edu.ifpr.mcp_server.domain.PriceStats;
import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Modelo de recurso {@value #URI_TEMPLATE}: estatísticas de preço (R$) e área
 * (m²) dos imóveis disponíveis em uma cidade, por tipo, separando venda e
 * aluguel. Ex.: {@code imoveis://precos/Curitiba} ou
 * {@code imoveis://precos/S%C3%A3o%20Jos%C3%A9%20dos%20Pinhais}.
 */
public class CityPricesResource extends PropertyResource {
   public static final String URI_PREFIX   = "imoveis://precos/";
   public static final String URI_TEMPLATE = URI_PREFIX + "{cidade}";
   public static final String NAME         = "precos_cidade";
   static final int           MAX_CITY_LEN = 100;

   /** Conteúdo do recurso: estatísticas de venda e de aluguel da cidade. */
   public record CityPrices(String city,
                            List<PriceStats> sell,
                            List<PriceStats> rent) {
   }

   public CityPricesResource(PropertyRepository repository, JsonMapper json) {
      super(repository,json);
   }

   public ResourceTemplate resourceTemplate() {
      // define o modelo de recurso: "{cidade}" na URI é o parâmetro que o
      // cliente substitui pelo nome da cidade
      return ResourceTemplate.builder(URI_TEMPLATE,NAME)
                             .title("Preços por cidade")
                             .description("Preço mínimo, médio e máximo " +
                                          "(R$) e área média (m²) dos " +
                                          "imóveis disponíveis na cidade, " +
                                          "por tipo, separados em venda " +
                                          "('sell') e aluguel mensal " +
                                          "('rent'). Use o nome da cidade " +
                                          "(ou parte dele) codificado para " +
                                          "URL, ex.: " + URI_PREFIX +
                                          "Curitiba.")
                             // formato do conteúdo retornado ao cliente
                             .mimeType(MIME_TYPE)
                             .build();
   }

   @Override
   public void register(
                    SyncSpecification<SingleSessionSyncSpecification> mcp) {
      // vincula o modelo de recurso (acima) à implementação da leitura e
      // acrescenta o modelo ao servidor
      //
      // OBSERVAÇÃO:
      // quando o cliente ler uma URI que siga o modelo, o método "read"
      // (herdado de PropertyResource) será invocado e chamará "content"
      // (abaixo)
      //
      mcp.resourceTemplates(new SyncResourceTemplateSpecification(
                                                resourceTemplate(),this::read));
   }

   @Override
   protected CityPrices content(String uri) {
      // recupera o parâmetro (nome da cidade) da URI pedida pelo cliente
      String           city = parseCity(uri);
      // calcula as estatísticas separadamente, pois venda e aluguel têm
      // preços em escalas diferentes
      List<PriceStats> sell = stats(city,Purpose.SELL);
      List<PriceStats> rent = stats(city,Purpose.RENT);

      // cidade sem imóveis: gera um erro "recurso não encontrado"
      if (sell.isEmpty() && rent.isEmpty())
         throw notFound("Não há imóveis disponíveis na cidade '" + city +
                        "'. Consulte as cidades atendidas em " +
                        LocationsResource.URI + ".");
      // retorna os dados do recurso, que serão convertidos em JSON
      return new CityPrices(city,sell,rent);
   }

   private List<PriceStats> stats(String city, Purpose purpose) {
      // consulta as estatísticas da cidade para uma finalidade
      return repository.priceStats(PropertyFilter.builder()
                                                 .city(city)
                                                 .purpose(purpose)
                                                 .build());
   }

   /** Extrai e decodifica o nome da cidade da URI. */
   static String parseCity(String uri) {
      if (uri == null || !uri.startsWith(URI_PREFIX))
         throw new IllegalArgumentException("deve seguir o modelo " +
                                            URI_TEMPLATE);

      // decodifica o nome da cidade
      //
      // OBSERVAÇÃO:
      // nomes com acentos e espaços chegam codificados para URL
      // (ex.: S%C3%A3o%20Jos%C3%A9 -> São José)
      //
      String city = URLDecoder.decode(uri.substring(URI_PREFIX.length()),
                                      StandardCharsets.UTF_8).strip();

      // valida o nome: não pode ser vazio nem longo demais
      if (city.isEmpty())
         throw new IllegalArgumentException("informe o nome da cidade");
      if (city.length() > MAX_CITY_LEN)
         throw new IllegalArgumentException("o nome da cidade deve ter no " +
                                            "máximo " + MAX_CITY_LEN +
                                            " caracteres");
      return city;
   }
}
