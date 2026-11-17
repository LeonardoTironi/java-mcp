package br.edu.ifpr.mcp_server.repository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import br.edu.ifpr.mcp_server.db.ConnectionFactory;
import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Testa o tratamento de falhas de conexão sem precisar de um banco: a fábrica
 * de conexões falsa sempre falha. As consultas reais são cobertas pelos
 * testes de integração.
 */
class JdbcPropertyRepositoryTest {
   // fábrica de conexões escrita como lambda: toda tentativa de conectar
   // lança SQLException, como se o banco estivesse fora do ar
   private static final ConnectionFactory OFFLINE =
                    () -> { throw new SQLException("banco fora do ar"); };

   private final JdbcPropertyRepository repository =
         new JdbcPropertyRepository(OFFLINE,new PropertySqlBuilder(),
                                    new PropertyRowMapper(new JsonMapper()));

   private static void assertWrapsSqlException(Executable call) {
      // a SQLException (verificada) deve chegar como RepositoryException,
      // guardando a original como causa e repetindo a sua mensagem
      RepositoryException ex = assertThrows(RepositoryException.class,call);

      assertInstanceOf(SQLException.class,ex.getCause());
      assertTrue(ex.getMessage().contains("banco fora do ar"));
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam, para cada consulta do
   // repositório:
   // - a falha de conexão vira RepositoryException com a causa original?
   // - dependências nulas são rejeitadas no construtor?
   // ==========================================================================

   @Test
   void buscaDeveConverterFalhaDeConexao() {
      assertWrapsSqlException(
                   () -> repository.search(PropertyFilter.builder().build()));
   }

   @Test
   void detalhePorIdDeveConverterFalhaDeConexao() {
      assertWrapsSqlException(() -> repository.findById("prop-001"));
   }

   @Test
   void detalhePorCodigoDeveConverterFalhaDeConexao() {
      assertWrapsSqlException(() -> repository.findByCode("IMV-0001"));
   }

   @Test
   void localidadesDevemConverterFalhaDeConexao() {
      assertWrapsSqlException(() -> repository.listLocations(null,null));
   }

   @Test
   void estatisticasDevemConverterFalhaDeConexao() {
      assertWrapsSqlException(
               () -> repository.priceStats(PropertyFilter.builder().build()));
   }

   @Test
   void construtorDeveRejeitarDependenciasNulas() {
      assertThrows(NullPointerException.class,
                   () -> new JdbcPropertyRepository(null,
                                                    new PropertySqlBuilder(),
                                                    new PropertyRowMapper(
                                                          new JsonMapper())));
   }
}
