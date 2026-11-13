package br.edu.ifpr.mcp_server.repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.edu.ifpr.mcp_server.domain.Address;
import br.edu.ifpr.mcp_server.domain.AvailabilityStatus;
import br.edu.ifpr.mcp_server.domain.LocationCount;
import br.edu.ifpr.mcp_server.domain.PriceStats;
import br.edu.ifpr.mcp_server.domain.PropertyDetail;
import br.edu.ifpr.mcp_server.domain.PropertySummary;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.domain.UsageType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converte linhas de {@link ResultSet} nos objetos de domínio. Os nomes das
 * colunas correspondem aos aliases definidos em {@link PropertySqlBuilder}.
 */
public final class PropertyRowMapper {
   private static final TypeReference<Map<String, Object>> JSON_OBJECT =
                                                 new TypeReference<>() {};

   private final JsonMapper json;

   public PropertyRowMapper(JsonMapper json) {
      this.json = Objects.requireNonNull(json,"json");
   }

   public PropertySummary toSummary(ResultSet rs) throws SQLException {
      return new PropertySummary(rs.getString("id"),
                                 rs.getString("code"),
                                 rs.getString("title"),
                                 getEnum(rs,"type",PropertyType.class),
                                 getEnum(rs,"purpose",Purpose.class),
                                 getEnum(rs,"usageType",UsageType.class),
                                 rs.getBigDecimal("minPrice"),
                                 rs.getBigDecimal("maxPrice"),
                                 rs.getBigDecimal("minArea"),
                                 rs.getBigDecimal("maxArea"),
                                 getInteger(rs,"bedrooms"),
                                 getInteger(rs,"parkingSpots"),
                                 rs.getBoolean("isFeatured"),
                                 rs.getString("neighborhood"),
                                 rs.getString("city"),
                                 rs.getString("state"));
   }

   public PropertyDetail toDetail(ResultSet rs) throws SQLException {
      return new PropertyDetail(rs.getString("id"),
                                rs.getString("code"),
                                rs.getString("title"),
                                rs.getString("description"),
                                getEnum(rs,"type",PropertyType.class),
                                getEnum(rs,"purpose",Purpose.class),
                                getEnum(rs,"status",AvailabilityStatus.class),
                                getEnum(rs,"usageType",UsageType.class),
                                rs.getBoolean("isFeatured"),
                                rs.getBigDecimal("minPrice"),
                                rs.getBigDecimal("maxPrice"),
                                rs.getBigDecimal("minArea"),
                                rs.getBigDecimal("maxArea"),
                                parseJsonObject(rs.getString("extraInfo")),
                                rs.getString("websiteLink"),
                                rs.getString("videoUrl"),
                                getTextArray(rs,"publishedOn"),
                                rs.getObject("createdAt",LocalDateTime.class),
                                rs.getObject("updatedAt",LocalDateTime.class),
                                toAddress(rs),
                                rs.getString("ownerName"),
                                rs.getString("condominiumName"));
   }

   public LocationCount toLocation(ResultSet rs) throws SQLException {
      return new LocationCount(rs.getString("state"),
                               rs.getString("city"),
                               rs.getString("neighborhood"),
                               rs.getLong("total"));
   }

   public PriceStats toPriceStats(ResultSet rs) throws SQLException {
      return new PriceStats(getEnum(rs,"type",PropertyType.class),
                            rs.getLong("total"),
                            rs.getBigDecimal("minPrice"),
                            rs.getBigDecimal("avgPrice"),
                            rs.getBigDecimal("maxPrice"),
                            rs.getBigDecimal("avgArea"));
   }

   /** Endereço da linha; nulo quando o imóvel não possui endereço. */
   private static Address toAddress(ResultSet rs) throws SQLException {
      if (rs.getString("street") == null)
         return null;
      return new Address(rs.getString("street"),
                         rs.getString("number"),
                         rs.getString("addressExtraInfo"),
                         rs.getString("neighborhood"),
                         rs.getString("city"),
                         rs.getString("state"),
                         rs.getString("postalCode"),
                         getDouble(rs,"latitude"),
                         getDouble(rs,"longitude"));
   }

   /** Converte o JSON (jsonb como texto) em mapa; vazio se nulo. */
   Map<String, Object> parseJsonObject(String text) {
      if (text == null || text.isBlank())
         return Map.of();
      try {
         return json.readValue(text,JSON_OBJECT);
      }
      catch (JacksonException ex) {
         throw new RepositoryException("extraInfo com JSON inválido",ex);
      }
   }

   private static <E extends Enum<E>> E getEnum(ResultSet rs, String column,
                                                Class<E> type)
                 throws SQLException {
      String value = rs.getString(column);

      return value == null ? null : Enum.valueOf(type,value);
   }

   private static Integer getInteger(ResultSet rs, String column)
                 throws SQLException {
      int value = rs.getInt(column);

      return rs.wasNull() ? null : value;
   }

   private static Double getDouble(ResultSet rs, String column)
                 throws SQLException {
      double value = rs.getDouble(column);

      return rs.wasNull() ? null : value;
   }

   private static List<String> getTextArray(ResultSet rs, String column)
                 throws SQLException {
      Array array = rs.getArray(column);

      if (array == null)
         return List.of();
      try {
         return List.of((String[]) array.getArray());
      }
      finally {
         array.free();
      }
   }
}
