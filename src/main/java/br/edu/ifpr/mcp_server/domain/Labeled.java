package br.edu.ifpr.mcp_server.domain;

/**
 * Enumeração com um rótulo legível em português, usado nas descrições das
 * ferramentas MCP para que a IA associe, por exemplo, "casa" a HOUSE.
 */
public interface Labeled {
   String label();
}
