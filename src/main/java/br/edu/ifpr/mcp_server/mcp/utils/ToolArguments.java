package br.edu.ifpr.mcp_server.mcp.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Leitura tipada e validada dos argumentos recebidos por uma ferramenta MCP.
 * <p>
 * Todos os métodos {@code opt*} retornam {@code null} quando o argumento está
 * ausente (ou é texto em branco) e lançam {@link IllegalArgumentException},
 * com mensagem em português citando o parâmetro, quando o valor é inválido.
 */
public final class ToolArguments {
   private final Map<String, Object> values;

   public ToolArguments(Map<String, Object> values) {
      this.values = values == null ? Map.of() : values;
   }

   /** Texto sem espaços nas extremidades. */
   public String optString(String name) {
      Object value = values.get(name);

      if (value == null)
         return null;
      if (!(value instanceof String text))
         throw invalid(name,"deve ser um texto");
      return text.isBlank() ? null : text.trim();
   }

   /** Constante da enumeração com o mesmo nome (ignora maiúsculas). */
   public <E extends Enum<E>> E optEnum(String name, Class<E> type) {
      return optValue(name,text -> parseEnum(text,type));
   }

   /** Texto convertido pelo {@code parser}, que pode lançar IAE. */
   public <T> T optValue(String name, Function<String, T> parser) {
      String text = optString(name);

      if (text == null)
         return null;
      try {
         return parser.apply(text);
      }
      catch (IllegalArgumentException ex) {
         throw invalid(name,ex.getMessage());
      }
   }

   /** Número decimal não negativo (aceita número JSON ou texto numérico). */
   public BigDecimal optDecimal(String name) {
      BigDecimal number = optNumber(name);

      if (number != null && number.signum() < 0)
         throw invalid(name,"não pode ser negativo");
      return number;
   }

   /** Número inteiro no intervalo [min, max]. */
   public Integer optInteger(String name, int min, int max) {
      BigDecimal number = optNumber(name);

      if (number == null)
         return null;
      if (number.stripTrailingZeros().scale() > 0)
         throw invalid(name,"deve ser um número inteiro");
      if (number.compareTo(BigDecimal.valueOf(min)) < 0 ||
          number.compareTo(BigDecimal.valueOf(max)) > 0)
         throw invalid(name,"deve estar entre " + min + " e " + max);
      return number.intValueExact();
   }

   /** Booleano (aceita true/false JSON ou os textos "true"/"false"). */
   public Boolean optBoolean(String name) {
      Object value = values.get(name);

      if (value == null || value instanceof Boolean)
         return (Boolean) value;
      if (value instanceof String text) {
         if ("true".equalsIgnoreCase(text.trim()))
            return true;
         if ("false".equalsIgnoreCase(text.trim()))
            return false;
         if (text.isBlank())
            return null;
      }
      throw invalid(name,"deve ser true ou false");
   }

   private BigDecimal optNumber(String name) {
      Object value = values.get(name);

      if (value == null)
         return null;
      if (!(value instanceof Number) && !(value instanceof String))
         throw invalid(name,"deve ser um número");
      String text = value.toString().trim();

      if (text.isEmpty())
         return null;
      try {
         return new BigDecimal(text);
      }
      catch (NumberFormatException ex) {
         throw invalid(name,"deve ser um número");
      }
   }

   private static <E extends Enum<E>> E parseEnum(String text, Class<E> type) {
      try {
         return Enum.valueOf(type,text.toUpperCase(Locale.ROOT));
      }
      catch (IllegalArgumentException ex) {
         throw new IllegalArgumentException(
               "valor '" + text + "' inválido. Valores aceitos: " +
               Arrays.toString(type.getEnumConstants()));
      }
   }

   private static IllegalArgumentException invalid(String name,
                                                   String reason) {
      return new IllegalArgumentException("Parâmetro '" + name + "' " +
                                          reason + ".");
   }
}
