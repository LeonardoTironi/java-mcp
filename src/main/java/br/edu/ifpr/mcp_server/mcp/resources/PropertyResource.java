package br.edu.ifpr.mcp_server.mcp.resources;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_server.mcp.utils.McpResourceDefinition;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import br.edu.ifpr.mcp_server.repository.RepositoryException;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base dos recursos de imóveis. Concentra a serialização em JSON e o
 * tratamento de erros, de modo que cada subclasse define apenas sua descrição
 * e os dados de uma URI ({@link #content(String)}).
 * <p>
 * Diferente das ferramentas, recursos não têm um resultado "de erro": falhas
 * viram um {@link McpError} ({@code INVALID_PARAMS},
 * {@code RESOURCE_NOT_FOUND} ou {@code INTERNAL_ERROR}).
 */
public abstract class PropertyResource implements McpResourceDefinition {
   private static final Logger LOG =
                               LoggerFactory.getLogger(PropertyResource.class);

   public static final String MIME_TYPE = "application/json";

   protected final PropertyRepository repository;
   private final ObjectWriter         writer;

   protected PropertyResource(PropertyRepository repository, JsonMapper json) {
      this.repository = Objects.requireNonNull(repository,"repository");
      // os dados dos recursos são enviados como JSON formatado (indentado)
      this.writer     = Objects.requireNonNull(json,"json")
                               .writerWithDefaultPrettyPrinter();
   }

   /**
    * Dados da URI pedida, que serão serializados em JSON.
    *
    * @throws IllegalArgumentException se a URI tiver parâmetros inválidos
    * @throws McpError                 se o recurso não existir
    */
   protected abstract Object content(String uri);

   @Override
   public ReadResourceResult read(McpSyncServerExchange exchange,
                                  ReadResourceRequest request) {
      // recupera a URI do recurso pedido pelo cliente
      String uri = request.uri();

      try {
         // obtém os dados da URI (definidos pela subclasse) e os converte
         // em JSON
         String text = writer.writeValueAsString(content(uri));

         // retorna o conteúdo do recurso ao cliente
         return new ReadResourceResult(List.of(
                           new TextResourceContents(uri,MIME_TYPE,text)));
      }
      catch (IllegalArgumentException ex) {
         // URI com parâmetro inválido: gera um erro JSON-RPC ao cliente
         throw error(ErrorCodes.INVALID_PARAMS,"URI inválida '" + uri + "': " +
                                               ex.getMessage());
      }
      catch (RepositoryException ex) {
         // falha no banco: registra os detalhes no log do servidor e envia
         // ao cliente apenas uma mensagem genérica
         LOG.error("Erro de banco no recurso '{}'",uri,ex);
         throw error(ErrorCodes.INTERNAL_ERROR,"Não foi possível consultar " +
                                               "o banco de dados de imóveis. " +
                                               "Tente novamente mais tarde.");
      }
   }

   protected static McpError notFound(String message) {
      // erro para URI válida, mas sem dados (ex.: cidade sem imóveis)
      return error(ErrorCodes.RESOURCE_NOT_FOUND,message);
   }

   private static McpError error(int code, String message) {
      // monta o erro JSON-RPC com o código e a mensagem informados
      return McpError.builder(code).message(message).build();
   }
}
