package br.edu.ifpr.mcp_server.mcp.utils;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;

/**
 * Recurso (MCP "resource") oferecido pelo servidor: um dado identificado por
 * uma URI que o cliente lê e anexa ao contexto da IA.
 * <p>
 * Pode ser um recurso de URI fixa (registrado com
 * {@code SyncSpecification.resources}) ou um modelo de URI com parâmetros
 * (registrado com {@code SyncSpecification.resourceTemplates}); cada
 * implementação decide como se registra.
 */
public interface McpResourceDefinition {

   /** Acrescenta o recurso ao servidor. */
   void register(SyncSpecification<SingleSessionSyncSpecification> mcp);

   /**
    * Atende a requisição {@code resources/read}. Falhas geram um erro
    * JSON-RPC ({@code McpError}) com a mensagem em português.
    */
   ReadResourceResult read(McpSyncServerExchange exchange,
                           ReadResourceRequest request);
}
