package br.edu.ifpr.mcp_server.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.ifpr.mcp_server.db.ConnectionFactory;
import br.edu.ifpr.mcp_server.domain.LocationCount;
import br.edu.ifpr.mcp_server.domain.PriceStats;
import br.edu.ifpr.mcp_server.domain.PropertyDetail;
import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertySummary;

/** Implementação de {@link PropertyRepository} com JDBC (PostgreSQL). */
public final class JdbcPropertyRepository implements PropertyRepository {
   /** Tempo máximo, em segundos, de execução de cada consulta. */
   static final int QUERY_TIMEOUT_SECONDS = 15;

   /** Lê uma linha do ResultSet (referência a método do mapper). */
   @FunctionalInterface
   private interface RowReader<T> {
      T read(ResultSet rs) throws SQLException;
   }

   private final ConnectionFactory  connections;
   private final PropertySqlBuilder sql;
   private final PropertyRowMapper  mapper;

   public JdbcPropertyRepository(ConnectionFactory connections,
                                 PropertySqlBuilder sql,
                                 PropertyRowMapper mapper) {
      this.connections = Objects.requireNonNull(connections,"connections");
      this.sql         = Objects.requireNonNull(sql,"sql");
      this.mapper      = Objects.requireNonNull(mapper,"mapper");
   }

   @Override
   public List<PropertySummary> search(PropertyFilter filter) {
      return list(sql.search(filter),mapper::toSummary);
   }

   @Override
   public Optional<PropertyDetail> findById(String id) {
      return first(sql.detailById(id),mapper::toDetail);
   }

   @Override
   public Optional<PropertyDetail> findByCode(String code) {
      return first(sql.detailByCode(code),mapper::toDetail);
   }

   @Override
   public List<LocationCount> listLocations(String state, String city) {
      return list(sql.locations(state,city),mapper::toLocation);
   }

   @Override
   public List<PriceStats> priceStats(PropertyFilter filter) {
      return list(sql.priceStats(filter),mapper::toPriceStats);
   }

   private <T> Optional<T> first(SqlQuery query, RowReader<T> reader) {
      return list(query,reader).stream().findFirst();
   }

   private <T> List<T> list(SqlQuery query, RowReader<T> reader) {
      try (Connection        connection = connections.open();
           PreparedStatement statement  =
                                  connection.prepareStatement(query.sql())) {
         // as ferramentas apenas consultam dados: conexão somente leitura
         connection.setReadOnly(true);
         bind(statement,query);
         return readAll(statement,reader);
      }
      catch (SQLException ex) {
         throw new RepositoryException("Falha ao consultar o banco de dados " +
                                       "de imóveis: " + ex.getMessage(),ex);
      }
   }

   private static void bind(PreparedStatement statement, SqlQuery query)
                 throws SQLException {
      statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
      for (int i = 0; i < query.params().size(); i++)
         statement.setObject(i + 1,query.params().get(i));
   }

   private static <T> List<T> readAll(PreparedStatement statement,
                                      RowReader<T> reader)
                 throws SQLException {
      try (ResultSet rs = statement.executeQuery()) {
         List<T> rows = new ArrayList<>();

         while (rs.next())
            rows.add(reader.read(rs));
         return rows;
      }
   }
}
