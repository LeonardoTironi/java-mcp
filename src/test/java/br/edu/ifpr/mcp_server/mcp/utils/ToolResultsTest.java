package br.edu.ifpr.mcp_server.mcp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.domain.PropertyType;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import tools.jackson.databind.json.JsonMapper;

class ToolResultsTest {
   private final ToolResults results = new ToolResults(new JsonMapper());

   private record Sample(String code, PropertyType type, BigDecimal price) {}

   private static String text(CallToolResult result) {
      assertEquals(1,result.content().size());
      return ((TextContent) result.content().get(0)).text();
   }

   @Test
   void sucessoDeveConterResumoEJson() {
      CallToolResult result = results.success("1 imóvel encontrado.",
            List.of(new Sample("IMV-0001",PropertyType.HOUSE,
                               new BigDecimal("850000.00"))));
      String         text   = text(result);

      assertFalse(result.isError());
      assertTrue(text.startsWith("1 imóvel encontrado."));
      assertTrue(text.contains("\"code\" : \"IMV-0001\""));
      assertTrue(text.contains("\"type\" : \"HOUSE\""));
      assertTrue(text.contains("850000.00"));
   }

   @Test
   void mensagemDeveSerSucessoSemJson() {
      CallToolResult result = results.message("Nenhum imóvel encontrado.");

      assertFalse(result.isError());
      assertEquals("Nenhum imóvel encontrado.",text(result));
   }

   @Test
   void erroDeveSerMarcadoComoErro() {
      CallToolResult result = results.error("Parâmetro 'tipo' inválido.");

      assertTrue(result.isError());
      assertEquals("Parâmetro 'tipo' inválido.",text(result));
   }
}
