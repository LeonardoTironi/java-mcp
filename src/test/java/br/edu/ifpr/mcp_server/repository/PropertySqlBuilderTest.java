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
                          // percorre os caracteres do SQL...
      long placeholders = query.sql().chars()
                          // ...e conta apenas os '?'
                          .filter(c -> c == '?').count();

      assertEquals(query.params().size(),placeholders,query.sql());
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam o SQL gerado para as consultas,
   // sem executá-lo (não precisam do banco):
   // - cada filtro informado vira uma condição com parâmetro '?'?
   // - os valores do usuário nunca entram no texto do SQL?
   // - imóveis "venda e locação" (BOTH) entram nas duas finalidades?
   // - toda ordenação tem desempate pelo código?
   // - detalhes, localidades e estatísticas geram o SQL esperado?
   // ==========================================================================

   @Test
   void buscaSemFiltrosDeveTerApenasCondicoesFixasELimite() {
      SqlQuery query = builder.search(PropertyFilter.builder().build());

      // a condição fixa garante que só imóveis visíveis e disponíveis
      // sejam buscados
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
      // o texto é procurado em quatro colunas (um parâmetro para cada);
      // cidade e bairro usam '%' para buscar por trecho (LIKE)
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

      // a tentativa de injeção de SQL fica só nos parâmetros, onde é tratada
      // como um texto qualquer, e não no comando SQL
      assertFalse(query.sql().contains(attack));
      assertTrue(query.params().contains("%" + attack + "%"));
   }

   @Test
   void finalidadeDeveIncluirImoveisVendaELocacao() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .purpose(Purpose.SELL)
                                                    .build());

      // a finalidade pedida ou 'BOTH' (imóvel à venda e para locação)
      assertTrue(query.sql().contains("IN (CAST(? AS \"Purpose\"), 'BOTH')"));
   }

   @Test
   void locacaoDeveUsarValorDoAluguelParaImoveisBoth() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .purpose(Purpose.RENT)
                                                    .maxPrice(BigDecimal.TEN)
                                                    .build());

      // em imóveis BOTH, a faixa de preço usa o valor do aluguel
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

   // teste parametrizado: executa uma vez para cada valor de SortOrder
   @ParameterizedTest
   @EnumSource(SortOrder.class)
   void todaOrdenacaoDeveGerarOrderByComDesempate(SortOrder order) {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .sortOrder(order)
                                                    .build());

      // o desempate pelo código deixa a ordem dos resultados estável
      assertTrue(query.sql().contains(" ORDER BY "));
      assertTrue(query.sql().contains(", p.code LIMIT ?"));
      assertPlaceholdersMatch(query);
   }

   @Test
   void ordenacaoPorMenorPrecoDeveSerAscendente() {
      SqlQuery query = builder.search(PropertyFilter.builder()
                                                    .sortOrder(SortOrder.PRICE_ASC)
                                                    .build());

      // imóveis sem preço informado vão para o final
      assertTrue(query.sql().contains("ORDER BY p.\"minPrice\" ASC NULLS LAST"));
   }

   @Test
   void detalhePorIdDeveFiltrarPorIdEVisibilidade() {
      SqlQuery query = builder.detailById("prop-001");

      // imóveis excluídos ou ocultos do site não podem ser detalhados
      assertTrue(query.sql().contains(PropertySqlBuilder.VISIBLE));
      assertTrue(query.sql().endsWith("p.id = ?"));
      assertEquals(List.of("prop-001"),query.params());
   }

   @Test
   void detalhePorCodigoDeveIgnorarCaixa() {
      SqlQuery query = builder.detailByCode("imv-0001");

      // a coluna e o parâmetro são comparados em maiúsculas
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
      // as estatísticas consideram todos os imóveis, sem limite
      assertFalse(query.sql().contains("LIMIT"));
      assertEquals(List.of("SELL","%Londrina%"),query.params());
      assertPlaceholdersMatch(query);
   }

   @Test
   void padraoContemDeveEscaparCuringas() {
      // '%' e '_' são curingas do LIKE e '\' é o escape: digitados pelo
      // usuário, devem ser procurados literalmente (em Java, "\\" é uma '\')
      assertEquals("%50\\%\\_a\\\\b%",
                   PropertySqlBuilder.containsPattern("50%_a\\b"));
   }
}
