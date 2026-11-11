package br.edu.ifpr.mcp_server.domain;

import java.math.BigDecimal;

/**
 * Resumo de um imóvel retornado pela busca. Preços em R$ e áreas em m²; em
 * buscas por locação, o preço de imóveis "venda e locação" é o do aluguel.
 */
public record PropertySummary(String id,
                              String code,
                              String title,
                              PropertyType type,
                              Purpose purpose,
                              UsageType usageType,
                              BigDecimal minPrice,
                              BigDecimal maxPrice,
                              BigDecimal minArea,
                              BigDecimal maxArea,
                              Integer bedrooms,
                              Integer parkingSpots,
                              boolean featured,
                              String neighborhood,
                              String city,
                              String state) {
}
