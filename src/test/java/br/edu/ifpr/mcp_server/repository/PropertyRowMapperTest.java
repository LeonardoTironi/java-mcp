package br.edu.ifpr.mcp_server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class PropertyRowMapperTest {
   private final PropertyRowMapper mapper =
                                  new PropertyRowMapper(new JsonMapper());

   // ==========================================================================
   // Os métodos de teste a seguir verificam a conversão das colunas JSON do
   // banco (as características do imóvel) em mapas Java:
   // - números, listas e nulos são convertidos?
   // - JSON nulo ou em branco vira mapa vazio?
   // - JSON inválido gera RepositoryException?
   // ==========================================================================

   @Test
   void deveConverterJsonEmMapa() {
      Map<String, Object> map = mapper.parseJsonObject(
                   "{\"bedrooms\":3,\"amenities\":[\"piscina\"],\"x\":null}");

      // o tipo exato do número depende do Jackson: compara pelo valor
      assertEquals(3,((Number) map.get("bedrooms")).intValue());
      assertEquals(List.of("piscina"),map.get("amenities"));
      // a chave com valor null é mantida no mapa
      assertTrue(map.containsKey("x"));
   }

   @Test
   void jsonNuloDeveVirarMapaVazio() {
      assertTrue(mapper.parseJsonObject(null).isEmpty());
   }

   @Test
   void jsonEmBrancoDeveVirarMapaVazio() {
      assertTrue(mapper.parseJsonObject("  ").isEmpty());
   }

   @Test
   void jsonInvalidoDeveLancarRepositoryException() {
      assertThrows(RepositoryException.class,
                   () -> mapper.parseJsonObject("{nao e json"));
   }
}
