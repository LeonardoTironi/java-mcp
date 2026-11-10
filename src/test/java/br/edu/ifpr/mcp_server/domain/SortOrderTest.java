package br.edu.ifpr.mcp_server.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SortOrderTest {

   @Test
   void deveConverterChaveIgnorandoCaixa() {
      assertEquals(SortOrder.PRICE_ASC,SortOrder.fromKey("PRECO_ASC"));
      assertEquals(SortOrder.NEWEST,SortOrder.fromKey("recentes"));
   }

   @Test
   void deveRejeitarChaveDesconhecida() {
      IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class,
                         () -> SortOrder.fromKey("barato"));

      assertTrue(ex.getMessage().contains("preco_asc"));
   }

   @Test
   void deveRejeitarChaveNula() {
      assertThrows(IllegalArgumentException.class,
                   () -> SortOrder.fromKey(null));
   }

   @Test
   void deveListarTodasAsChaves() {
      List<String> keys = SortOrder.keys();

      assertEquals(SortOrder.values().length,keys.size());
      assertEquals("destaques",keys.get(0));
   }
}
