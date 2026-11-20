package br.edu.ifpr.mcp_server.mcp.utils;

import java.util.List;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Modelo de prompt (MCP "prompt template") oferecido pelo servidor.
 * <p>
 * Cada implementação define sua descrição ({@link #prompt()}) e o texto da
 * mensagem gerada a partir dos argumentos ({@link #render(ToolArguments)}).
 * O registro e a montagem do resultado são comuns e ficam aqui.
 */
public interface McpPromptDefinition {

   /** Nome, descrição e argumentos expostos aos clientes MCP. */
   Prompt prompt();

   /**
    * Texto da mensagem do usuário que o cliente enviará à IA.
    *
    * @throws IllegalArgumentException se algum argumento for inválido
    */
   String render(ToolArguments args);

   /** Acrescenta o prompt ao servidor. */
   default void register(
                    SyncSpecification<SingleSessionSyncSpecification> mcp) {
      // vincula o objeto prompt (definido em "prompt()") à implementação e
      // acrescenta o prompt ao servidor
      //
      // OBSERVAÇÃO:
      // quando o cliente pedir o prompt, o método "get" (abaixo) será
      // invocado para gerar a mensagem
      //
      mcp.prompts(new SyncPromptSpecification(prompt(),this::get));
   }

   /**
    * Atende a requisição {@code prompts/get}. Argumentos inválidos geram um
    * erro JSON-RPC {@code INVALID_PARAMS} com a mensagem em português.
    */
   default GetPromptResult get(McpSyncServerExchange exchange,
                               GetPromptRequest request) {
      try {
         // recupera o(s) argumento(s) passados pelo cliente e gera o texto
         // da mensagem (definido por cada implementação em "render")
         String       text    = render(new ToolArguments(request.arguments()));
         // prepara a mensagem, enviada à IA como se fosse do usuário
         PromptMessage message = new PromptMessage(Role.USER,
                                                   new TextContent(text));

         // retorna o resultado do prompt ao cliente
         return new GetPromptResult(prompt().description(),List.of(message));
      }
      catch (IllegalArgumentException ex) {
         // argumento inválido: gera um erro JSON-RPC ao cliente
         throw McpError.builder(ErrorCodes.INVALID_PARAMS)
                       .message(ex.getMessage())
                       .build();
      }
   }
}
