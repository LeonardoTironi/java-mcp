package br.edu.ifpr.mcp_server.mcp.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.mcp.tools.FakePropertyRepository;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import tools.jackson.databind.json.JsonMapper;

class LocationsResourceTest {
   // repositório em memória: dispensa o banco e registra cada consulta
   // recebida em 'calls'; o JUnit cria uma instância nova a cada teste
   private final FakePropertyRepository repository =
                                                 new FakePropertyRepository();
   private final LocationsResource      resource   =
                           new LocationsResource(repository,new JsonMapper());

   private ReadResourceResult read() {
      // lê o recurso como o servidor faria ('exchange' nulo)
      return resource.read(null,new ReadResourceRequest(LocationsResource.URI));
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam o recurso 'imoveis://localidades':
   // - a URI, o nome e o tipo (JSON) são fixos?
   // - a leitura lista as cidades em JSON?
   // - o banco fora do ar vira erro interno?
   // ==========================================================================

   @Test
   void definicaoDeveTerUriENomeFixos() {
      Resource definition = resource.resource();

      assertEquals("imoveis://localidades",definition.uri());
      assertEquals("localidades",definition.name());
      assertEquals("application/json",definition.mimeType());
   }

   @Test
   void deveListarCidadesEmJson() {
      ReadResourceResult   result   = read();
      TextResourceContents contents =
                           (TextResourceContents) result.contents().get(0);

      assertEquals(1,result.contents().size());
      assertEquals(LocationsResource.URI,contents.uri());
      assertEquals("application/json",contents.mimeType());
      assertTrue(contents.text().contains("\"city\" : \"Curitiba\""),
                 contents.text());
      assertTrue(contents.text().contains("\"total\" : 5"),contents.text());
      // o repositório falso registra a chamada como "estado/cidade"; sem
      // filtro de estado nem de cidade, lista as cidades
      assertEquals("null/null",repository.calls.get(0));
   }

   @Test
   void falhaDoBancoDeveVirarErroInterno() {
      // simula o banco fora do ar: toda consulta lança RepositoryException
      repository.offline = true;

      McpError ex = assertThrows(McpError.class,this::read);

      assertEquals(ErrorCodes.INTERNAL_ERROR,ex.getJsonRpcError().code());
      assertTrue(ex.getMessage().contains("banco de dados"),ex.getMessage());
   }
}
