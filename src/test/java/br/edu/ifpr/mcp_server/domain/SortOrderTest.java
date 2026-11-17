package br.edu.ifpr.mcp_server.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SortOrderTest {

   // ==========================================================================
   // Os métodos de teste a seguir verificam as ordenações de busca, que o
   // cliente informa por chaves em português ("preco_asc", "recentes"...):
   // - a chave é convertida ignorando maiúsculas/minúsculas?
   // - chave desconhecida ou nula é rejeitada, listando as válidas?
   // - todas as chaves são listadas, começando por "destaques"?
   // ==========================================================================

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

      // a mensagem lista as chaves aceitas
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

      // uma chave para cada valor da enumeração; a primeira é a padrão
      assertEquals(SortOrder.values().length,keys.size());
      assertEquals("destaques",keys.get(0));
   }
}
