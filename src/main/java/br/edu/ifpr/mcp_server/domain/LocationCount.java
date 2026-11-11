package br.edu.ifpr.mcp_server.domain;

/**
 * Quantidade de imóveis disponíveis em uma localidade. {@code neighborhood} é
 * nulo quando a contagem é por cidade.
 */
public record LocationCount(String state,
                            String city,
                            String neighborhood,
                            long total) {
}
