package br.edu.ifpr.mcp_server.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.domain.SortOrder;

/**
 * Monta os comandos SQL das consultas de imóveis.
 * <p>
 * Regras de segurança: todo valor vindo do usuário vira parâmetro
 * ({@code ?}); apenas trechos fixos, definidos nesta classe, compõem o texto
 * SQL (inclusive o ORDER BY, escolhido a partir de {@link SortOrder}).
 */
public final class PropertySqlBuilder {
   /** Imóvel não excluído e publicado no site. */
   static final String VISIBLE   =
                         "p.\"deletedAt\" IS NULL AND p.\"showOnWebsite\"";
   /** Imóvel visível e disponível para negociação. */
   static final String AVAILABLE = VISIBLE + " AND p.status = 'AVAILABLE'";

   private static final String FROM =
          " FROM \"Property\" p" +
          " LEFT JOIN \"Address\" a ON a.id = p.\"addressId\"";

   private static final String BEDROOMS      = jsonInteger("bedrooms");
   private static final String PARKING_SPOTS = jsonInteger("parkingSpots");
   /**
    * Valor do aluguel de imóveis "venda e locação" (BOTH). A expressão
    * regular evita o caractere '?' para não confundir os parâmetros JDBC.
    */
   private static final String RENT_PRICE    =
          "CASE WHEN p.\"extraInfo\"->>'rentPrice' ~ '^[0-9]+([.][0-9]+){0,1}$'" +
          " THEN (p.\"extraInfo\"->>'rentPrice')::numeric(14,2) END";

   // -------------------------------------------------------------------------
   // Consultas
   // -------------------------------------------------------------------------

   /** Busca de imóveis disponíveis conforme o filtro. */
   public SqlQuery search(PropertyFilter filter) {
      List<Object>  params = new ArrayList<>();
      StringBuilder sql    = new StringBuilder("SELECT ")
                                   .append(summaryColumns(filter.purpose()))
                                   .append(FROM)
                                   .append(" WHERE ").append(AVAILABLE);

      appendFilters(sql,params,filter);
      sql.append(" ORDER BY ")
         .append(orderBy(filter.sortOrder(),filter.purpose()))
         .append(" LIMIT ?");
      params.add(filter.limit());
      return new SqlQuery(sql.toString(),params);
   }

   /** Detalhes de um imóvel visível pelo seu id. */
   public SqlQuery detailById(String id) {
      return detail("p.id",id);
   }

   /** Detalhes de um imóvel visível pelo seu código (ex.: IMV-0001). */
   public SqlQuery detailByCode(String code) {
      return detail("upper(p.code)",code.toUpperCase(Locale.ROOT));
   }

   /**
    * Quantidade de imóveis disponíveis por localidade: por bairro, quando a
    * cidade é informada; caso contrário, por cidade.
    */
   public SqlQuery locations(String state, String city) {
      return city == null ? citiesOf(state) : neighborhoodsOf(state,city);
   }

   /** Estatísticas de preço e área, agrupadas por tipo de imóvel. */
   public SqlQuery priceStats(PropertyFilter filter) {
      String        minPrice = minPriceExpression(filter.purpose());
      String        maxPrice = maxPriceExpression(filter.purpose());
      List<Object>  params   = new ArrayList<>();
      StringBuilder sql      = new StringBuilder("SELECT p.type, count(*) AS total, ")
            .append("min(").append(minPrice).append(") AS \"minPrice\", ")
            .append("round(avg((").append(minPrice).append(" + ")
            .append(maxPrice).append(") / 2), 2) AS \"avgPrice\", ")
            .append("max(").append(maxPrice).append(") AS \"maxPrice\", ")
            .append("round(avg((p.\"minArea\" + p.\"maxArea\") / 2), 2)")
            .append(" AS \"avgArea\"")
            .append(FROM)
            .append(" WHERE ").append(AVAILABLE);

      appendFilters(sql,params,filter);
      sql.append(" GROUP BY p.type ORDER BY p.type");
      return new SqlQuery(sql.toString(),params);
   }

   // -------------------------------------------------------------------------
   // Partes das consultas
   // -------------------------------------------------------------------------

   private static String summaryColumns(Purpose purpose) {
      return "p.id, p.code, p.title, p.type, p.purpose, p.\"usageType\", " +
             minPriceExpression(purpose) + " AS \"minPrice\", " +
             maxPriceExpression(purpose) + " AS \"maxPrice\", " +
             "p.\"minArea\", p.\"maxArea\", " +
             BEDROOMS + " AS bedrooms, " +
             PARKING_SPOTS + " AS \"parkingSpots\", " +
             "p.\"isFeatured\", a.neighborhood, a.city, a.state";
   }

   private static void appendFilters(StringBuilder sql, List<Object> params,
                                     PropertyFilter filter) {
      if (filter.text() != null) {
         String pattern = containsPattern(filter.text());

         // extraInfo inclui comodidades, ex.: "piscina", "churrasqueira"
         sql.append(" AND (p.title ILIKE ? OR p.description ILIKE ?" +
                    " OR a.neighborhood ILIKE ? OR p.\"extraInfo\"::text ILIKE ?)");
         params.addAll(List.of(pattern,pattern,pattern,pattern));
      }
      if (filter.type() != null)
         add(sql,params," AND p.type = CAST(? AS \"PropertyType\")",
             filter.type().name());
      if (filter.purpose() != null)
         add(sql,params," AND p.purpose IN (CAST(? AS \"Purpose\"), 'BOTH')",
             filter.purpose().name());
      if (filter.usageType() != null)
         add(sql,params," AND p.\"usageType\" = CAST(? AS \"UsageType\")",
             filter.usageType().name());
      if (filter.city() != null)
         add(sql,params," AND a.city ILIKE ?",containsPattern(filter.city()));
      if (filter.neighborhood() != null)
         add(sql,params," AND a.neighborhood ILIKE ?",
             containsPattern(filter.neighborhood()));
      if (filter.state() != null)
         add(sql,params," AND a.state = ?",filter.state());
      // faixas: o imóvel atende se sua faixa [mín, máx] intercepta a pedida
      if (filter.minPrice() != null)
         add(sql,params," AND " + maxPriceExpression(filter.purpose()) +
                        " >= ?",filter.minPrice());
      if (filter.maxPrice() != null)
         add(sql,params," AND " + minPriceExpression(filter.purpose()) +
                        " <= ?",filter.maxPrice());
      if (filter.minArea() != null)
         add(sql,params," AND p.\"maxArea\" >= ?",filter.minArea());
      if (filter.maxArea() != null)
         add(sql,params," AND p.\"minArea\" <= ?",filter.maxArea());
      if (filter.minBedrooms() != null)
         add(sql,params," AND coalesce(" + BEDROOMS + ", 0) >= ?",
             filter.minBedrooms());
      if (filter.minParkingSpots() != null)
         add(sql,params," AND coalesce(" + PARKING_SPOTS + ", 0) >= ?",
             filter.minParkingSpots());
      if (filter.featured() != null)
         add(sql,params," AND p.\"isFeatured\" = ?",filter.featured());
   }

   private static void add(StringBuilder sql, List<Object> params,
                           String condition, Object value) {
      sql.append(condition);
      params.add(value);
   }

   private static String orderBy(SortOrder order, Purpose purpose) {
      String criteria = switch (order) {
         case FEATURED   -> "p.\"isFeatured\" DESC, p.\"createdAt\" DESC";
         case NEWEST     -> "p.\"createdAt\" DESC";
         case PRICE_ASC  -> minPriceExpression(purpose) + " ASC NULLS LAST";
         case PRICE_DESC -> maxPriceExpression(purpose) + " DESC NULLS LAST";
         case AREA_DESC  -> "p.\"maxArea\" DESC NULLS LAST";
      };
      // desempate estável
      return criteria + ", p.code";
   }

   private static SqlQuery detail(String column, String value) {
      String sql =
            "SELECT p.id, p.code, p.title, p.description," +
            " p.\"extraInfo\"::text AS \"extraInfo\", p.type, p.purpose," +
            " p.status, p.\"usageType\", p.\"isFeatured\"," +
            " p.\"minPrice\", p.\"maxPrice\", p.\"minArea\", p.\"maxArea\"," +
            " p.\"websiteLink\", p.\"videoUrl\", p.\"publishedOn\"," +
            " p.\"createdAt\", p.\"updatedAt\"," +
            " a.street, a.number, a.\"extraInfo\" AS \"addressExtraInfo\"," +
            " a.neighborhood, a.city, a.state, a.\"postalCode\"," +
            " a.latitude, a.longitude," +
            " o.name AS \"ownerName\", c.name AS \"condominiumName\"" +
            FROM +
            " LEFT JOIN \"PropertyOwner\" o ON o.id = p.\"ownerId\"" +
            " LEFT JOIN \"Condominium\" c ON c.id = p.\"condominiumId\"" +
            " WHERE " + VISIBLE + " AND " + column + " = ?";

      return new SqlQuery(sql,List.of(value));
   }

   private static SqlQuery citiesOf(String state) {
      List<Object>  params = new ArrayList<>();
      StringBuilder sql    = new StringBuilder(
            "SELECT a.state, a.city, NULL AS neighborhood, count(*) AS total")
            .append(FROM)
            .append(" WHERE ").append(AVAILABLE)
            .append(" AND a.city IS NOT NULL");

      if (state != null)
         add(sql,params," AND a.state = ?",state);
      sql.append(" GROUP BY a.state, a.city ORDER BY total DESC, a.city");
      return new SqlQuery(sql.toString(),params);
   }

   private static SqlQuery neighborhoodsOf(String state, String city) {
      List<Object>  params = new ArrayList<>();
      StringBuilder sql    = new StringBuilder(
            "SELECT a.state, a.city, a.neighborhood, count(*) AS total")
            .append(FROM)
            .append(" WHERE ").append(AVAILABLE);

      add(sql,params," AND a.city ILIKE ?",containsPattern(city));
      if (state != null)
         add(sql,params," AND a.state = ?",state);
      sql.append(" GROUP BY a.state, a.city, a.neighborhood" +
                 " ORDER BY total DESC, a.city, a.neighborhood");
      return new SqlQuery(sql.toString(),params);
   }

   // -------------------------------------------------------------------------
   // Expressões auxiliares
   // -------------------------------------------------------------------------

   /**
    * Preço mínimo considerado. Em buscas por locação, imóveis "venda e
    * locação" usam o valor do aluguel (extraInfo.rentPrice), não o de venda.
    */
   static String minPriceExpression(Purpose purpose) {
      return priceExpression(purpose,"p.\"minPrice\"");
   }

   static String maxPriceExpression(Purpose purpose) {
      return priceExpression(purpose,"p.\"maxPrice\"");
   }

   private static String priceExpression(Purpose purpose, String column) {
      if (purpose != Purpose.RENT)
         return column;
      return "(CASE WHEN p.purpose = 'BOTH' THEN " + RENT_PRICE +
             " ELSE " + column + " END)";
   }

   /** Inteiro guardado em extraInfo; nulo se ausente ou não numérico. */
   private static String jsonInteger(String key) {
      String value = "p.\"extraInfo\"->>'" + key + "'";

      return "(CASE WHEN " + value + " ~ '^[0-9]+$' THEN (" + value +
             ")::int END)";
   }

   /** Padrão "contém" para ILIKE, escapando os curingas % e _ do usuário. */
   static String containsPattern(String value) {
      String escaped = value.replace("\\","\\\\")
                            .replace("%","\\%")
                            .replace("_","\\_");

      return "%" + escaped + "%";
   }
}
