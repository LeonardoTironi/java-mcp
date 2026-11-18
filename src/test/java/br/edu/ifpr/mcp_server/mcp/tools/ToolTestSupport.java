package br.edu.ifpr.mcp_server.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import tools.jackson.databind.json.JsonMapper;

/** Utilitários comuns aos testes das ferramentas. */
final class ToolTestSupport {
   // monta os resultados das ferramentas (texto + JSON), como no servidor
   static final ToolResults RESULTS = new ToolResults(new JsonMapper());

   // classe utilitária: só métodos estáticos, não deve ser instanciada
   private ToolTestSupport() {}

   /** Executa a ferramenta como o servidor faria (sem 'exchange'). */
   static CallToolResult call(PropertyTool tool, Map<String, Object> args) {
      return tool.run(null,new CallToolRequest(tool.tool().name(),args));
   }

   /** Exige um único 'content' e retorna o seu texto. */
   static String text(CallToolResult result) {
      assertEquals(1,result.content().size());
      return ((TextContent) result.content().get(0)).text();
   }
}
