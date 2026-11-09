package br.edu.ifpr.mcp_server.config;

import java.util.Map;

/**
 * Parâmetros de conexão com o banco de dados de imóveis.
 * <p>
 * Os valores são lidos das variáveis de ambiente {@value #ENV_URL},
 * {@value #ENV_USER} e {@value #ENV_PASSWORD}; na ausência delas são usados
 * valores padrão adequados ao ambiente de desenvolvimento (compose.yaml).
 */
public record DatabaseConfig(String url, String user, String password) {
   public static final String ENV_URL      = "DB_URL";
   public static final String ENV_USER     = "DB_USER";
   public static final String ENV_PASSWORD = "DB_PASSWORD";

   public static final String DEFAULT_URL      =
                                     "jdbc:postgresql://localhost:5432/imoveis";
   public static final String DEFAULT_USER     = "postgres";
   public static final String DEFAULT_PASSWORD = "postgres";

   public DatabaseConfig {
      requireNonBlank(url,"url");
      requireNonBlank(user,"user");
      if (password == null)
         throw new IllegalArgumentException("password não pode ser nulo");
   }

   /** Cria a configuração a partir das variáveis de ambiente do processo. */
   public static DatabaseConfig fromEnvironment() {
      return fromMap(System.getenv());
   }

   /** Cria a configuração a partir de um mapa (facilita os testes). */
   public static DatabaseConfig fromMap(Map<String, String> env) {
      return new DatabaseConfig(valueOrDefault(env,ENV_URL,DEFAULT_URL),
                                valueOrDefault(env,ENV_USER,DEFAULT_USER),
                                valueOrDefault(env,ENV_PASSWORD,
                                               DEFAULT_PASSWORD));
   }

   private static String valueOrDefault(Map<String, String> env, String key,
                                        String defaultValue) {
      String value = env.get(key);

      return value == null || value.isBlank() ? defaultValue : value.trim();
   }

   private static void requireNonBlank(String value, String name) {
      if (value == null || value.isBlank())
         throw new IllegalArgumentException(name + " não pode ser vazio");
   }

   /** Nunca expõe a senha em logs. */
   @Override
   public String toString() {
      return "DatabaseConfig[url=" + url + ", user=" + user +
             ", password=****]";
   }
}
