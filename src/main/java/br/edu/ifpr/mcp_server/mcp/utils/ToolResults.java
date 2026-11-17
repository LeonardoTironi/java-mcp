package br.edu.ifpr.mcp_server.mcp.utils;

import java.util.Objects;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cria os resultados ({@link CallToolResult}) das ferramentas MCP de forma
 * padronizada: um resumo legível seguido dos dados em JSON, ou uma mensagem
 * de erro marcada com {@code isError = true} para que a IA possa reagir (por
 * exemplo, corrigindo um parâmetro) em vez de receber uma exceção.
 */
public final class ToolResults {
   private final ObjectWriter writer;

   public ToolResults(JsonMapper json) {
      this.writer = Objects.requireNonNull(json,"json")
                           .writerWithDefaultPrettyPrinter();
   }

   /** Resultado de sucesso: resumo + dados serializados em JSON. */
   public CallToolResult success(String summary, Object data) {
      String text = summary + System.lineSeparator() +
                    System.lineSeparator() + writer.writeValueAsString(data);

      return CallToolResult.builder()
                           .addTextContent(text)
                           .isError(false)
                           .build();
   }

   /** Resultado de sucesso apenas com texto (ex.: nenhum imóvel encontrado). */
   public CallToolResult message(String text) {
      return CallToolResult.builder()
                           .addTextContent(text)
                           .isError(false)
                           .build();
   }

   /** Resultado de erro com a mensagem informada. */
   public CallToolResult error(String message) {
      return CallToolResult.builder()
                           .addTextContent(message)
                           .isError(true)
                           .build();
   }
}
