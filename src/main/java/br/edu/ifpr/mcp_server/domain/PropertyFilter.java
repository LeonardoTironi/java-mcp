package br.edu.ifpr.mcp_server.domain;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Critérios de busca de imóveis. Campos nulos não filtram. Use
 * {@link #builder()} para criar instâncias.
 */
public record PropertyFilter(String text,
                             PropertyType type,
                             Purpose purpose,
                             UsageType usageType,
                             String city,
                             String neighborhood,
                             String state,
                             BigDecimal minPrice,
                             BigDecimal maxPrice,
                             BigDecimal minArea,
                             BigDecimal maxArea,
                             Integer minBedrooms,
                             Integer minParkingSpots,
                             Boolean featured,
                             SortOrder sortOrder,
                             int limit) {
   public static final int DEFAULT_LIMIT = 10;
   public static final int MAX_LIMIT     = 50;

   public PropertyFilter {
      if (limit < 1 || limit > MAX_LIMIT)
         throw new IllegalArgumentException("o limite deve estar entre 1 e " +
                                            MAX_LIMIT);
      requireOrdered(minPrice,maxPrice,"preço");
      requireOrdered(minArea,maxArea,"área");
      state     = normalizeState(state);
      sortOrder = sortOrder == null ? SortOrder.FEATURED : sortOrder;
   }

   public static Builder builder() {
      return new Builder();
   }

   private static void requireOrdered(BigDecimal min, BigDecimal max,
                                      String name) {
      if (min != null && max != null && min.compareTo(max) > 0)
         throw new IllegalArgumentException(name + " mínimo(a) não pode ser " +
                                            "maior que " + name +
                                            " máximo(a)");
   }

   private static String normalizeState(String state) {
      if (state == null)
         return null;
      if (!state.matches("[A-Za-z]{2}"))
         throw new IllegalArgumentException("estado deve ser a sigla da UF " +
                                            "com 2 letras, ex.: PR");
      return state.toUpperCase(Locale.ROOT);
   }

   /** Construtor fluente de {@link PropertyFilter}. */
   public static final class Builder {
      private String       text;
      private PropertyType type;
      private Purpose      purpose;
      private UsageType    usageType;
      private String       city;
      private String       neighborhood;
      private String       state;
      private BigDecimal   minPrice;
      private BigDecimal   maxPrice;
      private BigDecimal   minArea;
      private BigDecimal   maxArea;
      private Integer      minBedrooms;
      private Integer      minParkingSpots;
      private Boolean      featured;
      private SortOrder    sortOrder;
      private int          limit = DEFAULT_LIMIT;

      private Builder() {}

      public Builder text(String value)            { text = value; return this; }
      public Builder type(PropertyType value)      { type = value; return this; }
      public Builder purpose(Purpose value)        { purpose = value; return this; }
      public Builder usageType(UsageType value)    { usageType = value; return this; }
      public Builder city(String value)            { city = value; return this; }
      public Builder neighborhood(String value)    { neighborhood = value; return this; }
      public Builder state(String value)           { state = value; return this; }
      public Builder minPrice(BigDecimal value)    { minPrice = value; return this; }
      public Builder maxPrice(BigDecimal value)    { maxPrice = value; return this; }
      public Builder minArea(BigDecimal value)     { minArea = value; return this; }
      public Builder maxArea(BigDecimal value)     { maxArea = value; return this; }
      public Builder minBedrooms(Integer value)    { minBedrooms = value; return this; }
      public Builder minParkingSpots(Integer value){ minParkingSpots = value; return this; }
      public Builder featured(Boolean value)       { featured = value; return this; }
      public Builder sortOrder(SortOrder value)    { sortOrder = value; return this; }
      /** Nulo mantém o limite padrão ({@value PropertyFilter#DEFAULT_LIMIT}). */
      public Builder limit(Integer value) {
         limit = value == null ? DEFAULT_LIMIT : value;
         return this;
      }

      public PropertyFilter build() {
         return new PropertyFilter(text,type,purpose,usageType,city,
                                   neighborhood,state,minPrice,maxPrice,
                                   minArea,maxArea,minBedrooms,
                                   minParkingSpots,featured,sortOrder,limit);
      }
   }
}
