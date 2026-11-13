package br.edu.ifpr.mcp_server.repository;

/** Falha de acesso aos dados (banco fora do ar, erro de SQL etc.). */
public class RepositoryException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public RepositoryException(String message, Throwable cause) {
      super(message,cause);
   }
}
