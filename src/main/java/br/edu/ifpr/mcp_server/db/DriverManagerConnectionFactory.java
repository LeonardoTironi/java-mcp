package br.edu.ifpr.mcp_server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import br.edu.ifpr.mcp_server.config.DatabaseConfig;

/**
 * Abre conexões via {@link DriverManager}. Cada chamada cria uma conexão nova,
 * suficiente para o volume de chamadas de um servidor MCP via STDIO.
 */
public final class DriverManagerConnectionFactory implements ConnectionFactory {
   /** Tempo máximo, em segundos, para estabelecer a conexão. */
   static final String CONNECT_TIMEOUT_SECONDS = "5";
   /** Tempo máximo, em segundos, de espera por respostas do servidor. */
   static final String SOCKET_TIMEOUT_SECONDS  = "30";

   private final DatabaseConfig config;

   public DriverManagerConnectionFactory(DatabaseConfig config) {
      this.config = Objects.requireNonNull(config,"config");
   }

   @Override
   public Connection open() throws SQLException {
      return DriverManager.getConnection(config.url(),connectionProperties());
   }

   private Properties connectionProperties() {
      Properties properties = new Properties();

      properties.setProperty("user",config.user());
      properties.setProperty("password",config.password());
      // evita que a ferramenta MCP fique "presa" se o banco estiver fora do ar
      properties.setProperty("connectTimeout",CONNECT_TIMEOUT_SECONDS);
      properties.setProperty("socketTimeout",SOCKET_TIMEOUT_SECONDS);
      return properties;
   }
}
