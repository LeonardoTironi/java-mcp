package br.edu.ifpr.mcp_server.domain;

import java.math.BigDecimal;

/** Estatísticas de preço (R$) e área (m²) por tipo de imóvel. */
public record PriceStats(PropertyType type,
                         long total,
                         BigDecimal minPrice,
                         BigDecimal avgPrice,
                         BigDecimal maxPrice,
                         BigDecimal avgArea) {
}
