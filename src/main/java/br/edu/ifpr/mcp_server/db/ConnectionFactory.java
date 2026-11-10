package br.edu.ifpr.mcp_server.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fornece conexões com o banco de dados. Abstração que permite trocar a
 * estratégia de conexão (DriverManager, pool...) sem afetar os repositórios.
 */
@FunctionalInterface
public interface ConnectionFactory {
   /** Abre uma nova conexão; quem chama é responsável por fechá-la. */
   Connection open() throws SQLException;
}
