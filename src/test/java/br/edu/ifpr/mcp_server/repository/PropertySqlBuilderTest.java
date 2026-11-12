package br.edu.ifpr.mcp_server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.domain.SortOrder;
import br.edu.ifpr.mcp_server.domain.UsageType;

class PropertySqlBuilderTest {
   private final PropertySqlBuilder builder = new PropertySqlBuilder();

   /** Cada '?' do SQL deve ter exatamente um parâmetro correspondente. */
   private static void assertPlaceholdersMatch(SqlQuery query) {
      long placeholders = query.sql().chars().filter(c -> c == '?').count();

      assertEquals(query.params().size(),placeholders,query.sql());
   }

   @Test
   void buscaSemFiltrosDeveTerApenasCondicoesFixasELimite() {
      SqlQuery query = builder.search(PropertyFilter.builder().build());

      assertTrue(query.sql().contains(PropertySqlBuilder.AVAILABLE));
      assertTrue(query.sql().endsWith(" LIMIT ?"));
      assertEquals(List.of(PropertyFilter.DEFAULT_LIMIT),query.params());
      assertPlaceholdersMatch(query);
   }

   @Test
   void buscaComTodosOsFiltrosDeveGerarUmParametroPorValor() {
      PropertyFilter filter = PropertyFilter.builder()
                                            .text("piscina")
                                            .type(PropertyType.HOUSE)
                                            .purpose(Purpose.SELL)
                                            .usageType(UsageType.RESIDENTIAL)
                                            .city("Curitiba")
                                            .neighborhood("Batel")
                                            .state("pr")
                                            .minPrice(new BigDecimal("100"))
                                            .maxPrice(new BigDecimal("900"))
                                            .minArea(new BigDecimal("50"))
                                            .maxArea(new BigDecimal("300"))
                                            .minBedrooms(3)
                                            .minParkingSpots(2)
                                            .featured(true)
                                            .limit(5)
                                            .build();
      SqlQuery query = builder.search(filter);

      assertPlaceholdersMatch(query);
      assertEquals(List.of("%piscina%","%piscina%","%piscina%","%piscina%",
                           "HOUSE","SELL","RESIDENTIAL",
                           "%Curitiba%","%Batel%","PR",
                           new BigDecimal("100"),new BigDecimal("900"),
                           new BigDecimal("50"),new BigDecimal("300"),
                           3,2,true,5),
                   query.params());
   }

   @Test
   void valoresDoUsuarioNuncaDevemIrParaOTextoSql() {
      String         attack = "x' OR 1=1 --";
      PropertyFilter filter = PropertyFilter.builder()
                                            .text(attack)
                                            .city(attack)
                                            .build();
      SqlQuery       query  = builder.search(filter);

      assertFalse(query.sql().contains(attack));
      assertTrue(query.params().contains("%" + attack + "%"));
   }

   @Test
   void finalidadeDeveIncluirImoveisVendaELocacao() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .purpose(Purpose.SELL)
                                                    .build());

      assertTrue(query.sql().contains("IN (CAST(? AS \"Purpose\"), 'BOTH')"));
   }

   @Test
   void locacaoDeveUsarValorDoAluguelParaImoveisBoth() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .purpose(Purpose.RENT)
                                                    .maxPrice(BigDecimal.TEN)
                                                    .build());

      assertTrue(query.sql().contains("rentPrice"));
      assertPlaceholdersMatch(query);
   }

   @Test
   void vendaNaoDeveUsarValorDoAluguel() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .purpose(Purpose.SELL)
                                                    .maxPrice(BigDecimal.TEN)
                                                    .build());

      assertFalse(query.sql().contains("rentPrice"));
   }

   @ParameterizedTest
   @EnumSource(SortOrder.class)
   void todaOrdenacaoDeveGerarOrderByComDesempate(SortOrder order) {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .sortOrder(order)
                                                    .build());

      assertTrue(query.sql().contains(" ORDER BY "));
      assertTrue(query.sql().contains(", p.code LIMIT ?"));
      assertPlaceholdersMatch(query);
   }

   @Test
   void ordenacaoPorMenorPrecoDeveSerAscendente() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .sortOrder(SortOrder.PRICE_ASC)
                                                    .build());

      assertTrue(query.sql().contains("ORDER BY p.\"minPrice\" ASC NULLS LAST"));
   }

   @Test
   void detalhePorIdDeveFiltrarPorIdEVisibilidade() {
      SqlQuery query = builder.detailById("prop-001");

      assertTrue(query.sql().contains(PropertySqlBuilder.VISIBLE));
      assertTrue(query.sql().endsWith("p.id = ?"));
      assertEquals(List.of("prop-001"),query.params());
   }

   @Test
   void detalhePorCodigoDeveIgnorarCaixa() {
      SqlQuery query = builder.detailByCode("imv-0001");

      assertTrue(query.sql().endsWith("upper(p.code) = ?"));
      assertEquals(List.of("IMV-0001"),query.params());
   }

   @Test
   void localidadesSemCidadeDevemAgruparPorCidade() {
      SqlQuery query = builder.locations(null,null);

      assertTrue(query.sql().contains("GROUP BY a.state, a.city ORDER BY"));
      assertTrue(query.params().isEmpty());
   }

   @Test
   void localidadesComEstadoDevemFiltrarPorEstado() {
      SqlQuery query = builder.locations("PR",null);

      assertEquals(List.of("PR"),query.params());
      assertPlaceholdersMatch(query);
   }

   @Test
   void localidadesComCidadeDevemAgruparPorBairro() {
      SqlQuery query = builder.locations("PR","Curitiba");

      assertTrue(query.sql().contains("GROUP BY a.state, a.city, a.neighborhood"));
      assertEquals(List.of("%Curitiba%","PR"),query.params());
      assertPlaceholdersMatch(query);
   }

   @Test
   void estatisticasDevemAgruparPorTipo() {
      SqlQuery query = builder.priceStats(PropertyFilter.builder()
                                                        .city("Londrina")
                                                        .purpose(Purpose.SELL)
                                                        .build());

      assertTrue(query.sql().endsWith("GROUP BY p.type ORDER BY p.type"));
      assertFalse(query.sql().contains("LIMIT"));
      assertEquals(List.of("SELL","%Londrina%"),query.params());
      assertPlaceholdersMatch(query);
   }

   @Test
   void padraoContemDeveEscaparCuringas() {
      assertEquals("%50\\%\\_a\\\\b%",
                   PropertySqlBuilder.containsPattern("50%_a\\b"));
   }
}
