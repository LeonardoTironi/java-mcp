package br.edu.ifpr.mcp_server.mcp.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.mcp.tools.FakePropertyRepository;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import tools.jackson.databind.json.JsonMapper;

class CityPricesResourceTest {
   // repositório em memória: dispensa o banco e registra cada consulta
   // recebida em 'calls'; o JUnit cria uma instância nova a cada teste
   private final FakePropertyRepository repository =
                                                 new FakePropertyRepository();
   private final CityPricesResource     resource   =
                          new CityPricesResource(repository,new JsonMapper());

   private String read(String uri) {
      // lê o recurso como o servidor faria e retorna o texto (JSON)
      return ((TextResourceContents) resource.read(
                                            null,new ReadResourceRequest(uri))
                                             .contents().get(0)).text();
   }

   private McpError fail(String uri) {
      // erro na leitura de recurso é erro de protocolo: lança exceção
      return assertThrows(McpError.class,
                          () -> resource.read(null,
                                              new ReadResourceRequest(uri)));
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam o recurso 'imoveis://precos/...':
   // - a definição é um modelo de URI com a cidade?
   // - venda e aluguel são consultados separadamente?
   // - o nome da cidade é decodificado da URI (acentos, espaços)?
   // - URI inválida, cidade sem imóveis e banco fora do ar geram o erro
   //   certo?
   // ==========================================================================

   @Test
   void definicaoDeveSerModeloComCidade() {
      ResourceTemplate definition = resource.resourceTemplate();

      assertEquals("imoveis://precos/{cidade}",definition.uriTemplate());
      assertEquals("precos_cidade",definition.name());
      assertEquals("application/json",definition.mimeType());
   }

   @Test
   void deveSepararVendaEAluguelDaCidade() {
      String               text    = read("imoveis://precos/Londrina");
                                        // percorre as consultas registradas...
      List<PropertyFilter> filters = repository.calls.stream()
                                        // ...tratando cada uma como filtro
                                        .map(PropertyFilter.class::cast)
                                        .toList();

      // uma consulta para venda e outra para aluguel
      assertEquals(2,filters.size());
      assertEquals("Londrina",filters.get(0).city());
      assertEquals(Purpose.SELL,filters.get(0).purpose());
      assertEquals(Purpose.RENT,filters.get(1).purpose());
      assertTrue(text.contains("\"city\" : \"Londrina\""),text);
      assertTrue(text.contains("\"sell\""),text);
      assertTrue(text.contains("\"rent\""),text);
      // média configurada nas estatísticas do repositório falso
      assertTrue(text.contains("\"avgPrice\" : 675000.00"),text);
   }

   @Test
   void deveDecodificarNomeDaCidade() {
      // na URI, acentos e espaços vêm codificados (%C3%A3 = 'ã', %20 = ' ')
      read("imoveis://precos/S%C3%A3o%20Jos%C3%A9%20dos%20Pinhais");

      assertEquals("São José dos Pinhais",
                   ((PropertyFilter) repository.calls.get(0)).city());
   }

   @Test
   void cidadeSemImoveisNaoEncontrada() {
      // o repositório passa a não ter estatísticas para nenhuma cidade
      repository.stats = List.of();

      McpError ex = fail("imoveis://precos/Atlantida");

      assertEquals(ErrorCodes.RESOURCE_NOT_FOUND,ex.getJsonRpcError().code());
      assertTrue(ex.getMessage().contains("'Atlantida'"),ex.getMessage());
   }

   @Test
   void uriInvalidaDeveSerErroDeParametro() {
      // cidade em branco
      assertEquals(ErrorCodes.INVALID_PARAMS,
                   fail("imoveis://precos/%20").getJsonRpcError().code());
      // codificação inválida ('%zz' não é um hexadecimal)
      assertEquals(ErrorCodes.INVALID_PARAMS,
                   fail("imoveis://precos/%zz").getJsonRpcError().code());
      // URI que não segue o modelo
      assertEquals(ErrorCodes.INVALID_PARAMS,
                   fail("imoveis://outra/Curitiba").getJsonRpcError().code());
      // nome de cidade longo demais
      assertEquals(ErrorCodes.INVALID_PARAMS,
                   fail("imoveis://precos/" + "a".repeat(101))
                         .getJsonRpcError().code());
      // tudo foi rejeitado antes de consultar o repositório
      assertTrue(repository.calls.isEmpty());
   }

   @Test
   void falhaDoBancoDeveVirarErroInterno() {
      // simula o banco fora do ar: toda consulta lança RepositoryException
      repository.offline = true;

      assertEquals(ErrorCodes.INTERNAL_ERROR,
                   fail("imoveis://precos/Curitiba").getJsonRpcError().code());
   }
}
