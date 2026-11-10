package br.edu.ifpr.mcp_server.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Ordenações aceitas na busca de imóveis. A chave ({@link #key()}) é o valor
 * exposto aos clientes MCP.
 */
public enum SortOrder implements Labeled {
   FEATURED("destaques","imóveis em destaque primeiro, depois os mais recentes"),
   NEWEST("recentes","mais recentes primeiro"),
   PRICE_ASC("preco_asc","menor preço primeiro"),
   PRICE_DESC("preco_desc","maior preço primeiro"),
   AREA_DESC("area_desc","maior área primeiro");

   private final String key;
   private final String label;

   SortOrder(String key, String label) {
      this.key   = key;
      this.label = label;
   }

   public String key() {
      return key;
   }

   @Override
   public String label() {
      return label;
   }

   /** Chaves aceitas, na ordem de declaração. */
   public static List<String> keys() {
      return Arrays.stream(values()).map(SortOrder::key).toList();
   }

   /**
    * Converte a chave (ignorando maiúsculas/minúsculas) na ordenação.
    *
    * @throws IllegalArgumentException se a chave não existir
    */
   public static SortOrder fromKey(String key) {
      return Arrays.stream(values())
                   .filter(order -> order.key.equalsIgnoreCase(key))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(
                         "ordenação inválida '" + key + "'. Valores aceitos: " +
                         keys()));
   }
}
