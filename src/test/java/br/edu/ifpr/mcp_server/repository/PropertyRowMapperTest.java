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

   @Test
   void deveConverterJsonEmMapa() {
      Map<String, Object> map = mapper.parseJsonObject(
                   "{\"bedrooms\":3,\"amenities\":[\"piscina\"],\"x\":null}");

      assertEquals(3,((Number) map.get("bedrooms")).intValue());
      assertEquals(List.of("piscina"),map.get("amenities"));
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
