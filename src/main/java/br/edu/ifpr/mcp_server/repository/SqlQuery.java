package br.edu.ifpr.mcp_server.repository;

import java.util.List;
import java.util.Objects;

/**
 * Comando SQL com parâmetros posicionais ({@code ?}), sempre executado via
 * PreparedStatement: valores informados pelo usuário nunca são concatenados
 * ao texto SQL.
 */
public record SqlQuery(String sql, List<Object> params) {

   public SqlQuery {
      Objects.requireNonNull(sql,"sql");
      params = List.copyOf(params);
   }
}
