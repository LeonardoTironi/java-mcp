package br.edu.ifpr.mcp_server.mcp.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.edu.ifpr.mcp_server.domain.Labeled;

/**
 * Monta os esquemas JSON (JSON Schema) dos parâmetros das ferramentas MCP.
 * <p>
 * As propriedades são mantidas na ordem de inclusão, que é a ordem em que o
 * cliente interativo solicita os valores ao usuário.
 */
public final class JsonSchema {
   private final Map<String, Object> properties = new LinkedHashMap<>();
   private final List<String>        required;

   private JsonSchema(List<String> required) {
      this.required = List.copyOf(required);
   }

   /** Esquema de objeto sem parâmetros obrigatórios. */
   public static JsonSchema object() {
      return new JsonSchema(List.of());
   }

   /** Esquema de objeto com os parâmetros obrigatórios informados. */
   public static JsonSchema object(String... required) {
      return new JsonSchema(Arrays.asList(required));
   }

   public JsonSchema string(String name, String description) {
      return add(name,property("string",description));
   }

   /**
    * Texto restrito às constantes da enumeração. A descrição recebe o rótulo
    * de cada valor (ex.: {@code HOUSE = casa}) para que a IA converta a
    * linguagem natural do usuário no valor esperado.
    */
   @SafeVarargs
   public final <E extends Enum<E> & Labeled> JsonSchema enumeration(
                                                  String name,
                                                  String description,
                                                  E... values) {
      Map<String, Object> property = property("string",description + " " +
                                              describe(values) + ".");

      property.put("enum",Arrays.stream(values).map(Enum::name).toList());
      return add(name,property);
   }

   /** Texto restrito a uma lista de valores (ex.: chaves de ordenação). */
   public JsonSchema choice(String name, String description,
                            List<String> values) {
      Map<String, Object> property = property("string",description);

      property.put("enum",List.copyOf(values));
      return add(name,property);
   }

   /** Número não negativo. */
   public JsonSchema number(String name, String description) {
      Map<String, Object> property = property("number",description);

      property.put("minimum",0);
      return add(name,property);
   }

   /** Número inteiro no intervalo [min, max]. */
   public JsonSchema integer(String name, String description, int min,
                             int max) {
      Map<String, Object> property = property("integer",description);

      property.put("minimum",min);
      property.put("maximum",max);
      return add(name,property);
   }

   public JsonSchema bool(String name, String description) {
      return add(name,property("boolean",description));
   }

   /** Mapa pronto para {@code Tool.builder(name, inputSchema)}. */
   public Map<String, Object> build() {
      Map<String, Object> schema = new LinkedHashMap<>();

      schema.put("type","object");
      schema.put("properties",new LinkedHashMap<>(properties));
      if (!required.isEmpty())
         schema.put("required",required);
      schema.put("additionalProperties",false);
      return schema;
   }

   private JsonSchema add(String name, Map<String, Object> property) {
      if (properties.putIfAbsent(name,property) != null)
         throw new IllegalArgumentException("parâmetro repetido: " + name);
      return this;
   }

   private static Map<String, Object> property(String type,
                                               String description) {
      Map<String, Object> property = new LinkedHashMap<>();

      property.put("type",type);
      property.put("description",description);
      return property;
   }

   private static String describe(Labeled[] values) {
      return Arrays.stream(values)
                   .map(v -> ((Enum<?>) v).name() + " = " + v.label())
                   .collect(Collectors.joining(", ","Valores: ",""));
   }
}
