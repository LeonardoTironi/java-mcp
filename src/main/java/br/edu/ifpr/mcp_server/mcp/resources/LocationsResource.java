package br.edu.ifpr.mcp_server.mcp.resources;

import java.util.List;

import br.edu.ifpr.mcp_server.domain.LocationCount;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import tools.jackson.databind.json.JsonMapper;

/**
 * Recurso {@value #URI}: cidades com imóveis disponíveis e a quantidade de
 * imóveis em cada uma.
 */
public class LocationsResource extends PropertyResource {
   public static final String URI  = "imoveis://localidades";
   public static final String NAME = "localidades";

   public LocationsResource(PropertyRepository repository, JsonMapper json) {
      super(repository,json);
   }

   public Resource resource() {
      // define o recurso (URI fixa, sem parâmetros)
      return Resource.builder(URI,NAME)
                     .title("Localidades com imóveis")
                     .description("Cidades (com UF) que têm imóveis " +
                                  "disponíveis e a quantidade de imóveis em " +
                                  "cada uma. Útil para saber os nomes exatos " +
                                  "das cidades atendidas.")
                     // formato do conteúdo retornado ao cliente
                     .mimeType(MIME_TYPE)
                     .build();
   }

   @Override
   public void register(
                    SyncSpecification<SingleSessionSyncSpecification> mcp) {
      // vincula o objeto recurso (acima) à implementação da leitura e
      // acrescenta o recurso ao servidor
      //
      // OBSERVAÇÃO:
      // quando o cliente ler o recurso, o método "read" (herdado de
      // PropertyResource) será invocado e chamará "content" (abaixo)
      //
      mcp.resources(new SyncResourceSpecification(resource(),this::read));
   }

   @Override
   protected List<LocationCount> content(String uri) {
      // consulta todas as cidades (sem filtro de UF ou de cidade)
      return repository.listLocations(null,null);
   }
}
