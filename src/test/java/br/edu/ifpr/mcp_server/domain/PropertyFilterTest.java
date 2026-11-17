package br.edu.ifpr.mcp_server.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PropertyFilterTest {

   // ==========================================================================
   // Os métodos de teste a seguir verificam o filtro de busca de imóveis:
   // - sem critérios, o limite e a ordenação padrão são aplicados?
   // - o limite aceita as bordas (1 e o máximo) e rejeita o que fica fora?
   // - o estado é normalizado para maiúsculas e validado?
   // - faixas de preço e de área com mínimo maior que o máximo são
   //   rejeitadas?
   // ==========================================================================

   @Test
   void deveAplicarPadroesQuandoVazio() {
      PropertyFilter filter = PropertyFilter.builder().build();

      assertEquals(PropertyFilter.DEFAULT_LIMIT,filter.limit());
      assertEquals(SortOrder.FEATURED,filter.sortOrder());
      assertNull(filter.city());
      assertNull(filter.state());
   }

   @Test
   void deveManterLimitePadraoQuandoNulo() {
      // null significa "não informado" (o argumento opcional da ferramenta)
      PropertyFilter filter = PropertyFilter.builder().limit(null).build();

      assertEquals(PropertyFilter.DEFAULT_LIMIT,filter.limit());
   }

   @Test
   void deveAceitarLimitesNasBordas() {
      assertEquals(1,PropertyFilter.builder().limit(1).build().limit());
      assertEquals(PropertyFilter.MAX_LIMIT,
                   PropertyFilter.builder()
                                 .limit(PropertyFilter.MAX_LIMIT)
                                 .build()
                                 .limit());
   }

   @Test
   void deveRejeitarLimiteZero() {
      assertThrows(IllegalArgumentException.class,
                   () -> PropertyFilter.builder().limit(0).build());
   }

   @Test
   void deveRejeitarLimiteAcimaDoMaximo() {
      assertThrows(IllegalArgumentException.class,
                   () -> PropertyFilter.builder()
                                       .limit(PropertyFilter.MAX_LIMIT + 1)
                                       .build());
   }

   @Test
   void deveNormalizarEstadoParaMaiusculas() {
      assertEquals("PR",PropertyFilter.builder().state("pr").build().state());
   }

   @Test
   void deveRejeitarEstadoInvalido() {
      // o estado deve ser a sigla com duas letras, não o nome
      assertThrows(IllegalArgumentException.class,
                   () -> PropertyFilter.builder().state("Paraná").build());
   }

   @Test
   void deveRejeitarPrecoMinimoMaiorQueMaximo() {
      assertThrows(IllegalArgumentException.class,
                   () -> PropertyFilter.builder()
                                       .minPrice(new BigDecimal("500000"))
                                       .maxPrice(new BigDecimal("100000"))
                                       .build());
   }

   @Test
   void deveRejeitarAreaMinimaMaiorQueMaxima() {
      assertThrows(IllegalArgumentException.class,
                   () -> PropertyFilter.builder()
                                       .minArea(new BigDecimal("200"))
                                       .maxArea(new BigDecimal("100"))
                                       .build());
   }

   @Test
   void deveAceitarFaixaComMinimoIgualAoMaximo() {
      // mínimo igual ao máximo é válido: busca um preço exato
      BigDecimal value = new BigDecimal("300000");

      assertDoesNotThrow(() -> PropertyFilter.builder()
                                             .minPrice(value)
                                             .maxPrice(value)
                                             .build());
   }
}
