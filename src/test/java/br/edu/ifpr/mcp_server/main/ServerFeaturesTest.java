package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.mcp.resources.CityPricesResource;
import br.edu.ifpr.mcp_server.mcp.resources.LocationsResource;
import br.edu.ifpr.mcp_server.mcp.tools.PropertyTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.JdbcPropertyRepository;
import br.edu.ifpr.mcp_server.repository.PropertyRowMapper;
import br.edu.ifpr.mcp_server.repository.PropertySqlBuilder;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import tools.jackson.databind.json.JsonMapper;

class ServerFeaturesTest {

   // ==========================================================================
   // Os métodos de teste a seguir verificam o que o servidor oferece ao
   // cliente, sem iniciar o servidor:
   // - todas as ferramentas, prompts e recursos são registrados?
   // - as ferramentas usam o repositório informado?
   // - dependências nulas são rejeitadas?
   // ==========================================================================

   @Test
   void deveOferecerTodasAsFerramentasEPrompts() {
      // apenas monta os objetos: nenhuma conexão com o banco é aberta aqui
      ServerFeatures features = ServerFeatures.fromEnvironment();

      assertEquals(List.of("buscar_imoveis","detalhar_imovel",
                           "listar_localidades","estatisticas_precos"),
                   features.tools().stream()
                           // fica apenas com as ferramentas de imóveis...
                           .filter(PropertyTool.class::isInstance)
                           // ...e obtém o nome de cada uma
                           .map(t -> ((PropertyTool) t).tool().name())
                           .toList());
      // as 4 de imóveis mais a 'obter_documento' (SimpleReportTool)
      assertEquals(5,features.tools().size());
      assertEquals(List.of("buscar_imovel_ideal","comparar_imoveis",
                           "analisar_mercado"),
                   features.prompts().stream()
                           .map(p -> p.prompt().name()).toList());
      assertEquals(List.of(LocationsResource.class,CityPricesResource.class),
                   features.resources().stream()
                           .map(Object::getClass).toList());
   }

   @Test
   void ferramentasDevemUsarORepositorioInformado() {
      // conexão que sempre falha: a ferramenta deve retornar erro, sem lançar
      JsonMapper        json     = new JsonMapper();
      ServerFeatures    features = new ServerFeatures(
            new JdbcPropertyRepository(
                  () -> { throw new SQLException("banco fora do ar"); },
                  new PropertySqlBuilder(),new PropertyRowMapper(json)),
            new ToolResults(json));
      // a primeira ferramenta registrada é a 'buscar_imoveis'
      McpToolDefinition search   = features.tools().get(0);

      assertEquals(true,search.run(null,
                                   new CallToolRequest("buscar_imoveis",
                                                       Map.of()))
                              .isError());
   }

   @Test
   void dependenciasNulasSaoRejeitadas() {
      assertThrows(NullPointerException.class,
                   () -> new ServerFeatures(null,
                                            new ToolResults(new JsonMapper())));
   }
}
