package br.edu.ifpr.mcp_server.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Localiza o JAR do servidor MCP que o cliente executa.
 * <p>
 * Ordem de procura:
 * <ol>
 * <li>a variável de ambiente {@value #ENV_VAR}, se definida (caminho absoluto
 * ou relativo ao diretório de trabalho);</li>
 * <li>{@code server/mcp_server01_server.jar}, quando o cliente é executado a
 * partir da raiz do projeto (por exemplo, pelo Eclipse IDE);</li>
 * <li>{@code ../../server/mcp_server01_server.jar}, quando o cliente é
 * executado a partir de {@code target/dist-client}.</li>
 * </ol>
 */
public final class ServerJarLocator {
   public static final String ENV_VAR  = "MCP_SERVER_JAR";
   public static final String JAR_NAME = "mcp_server01_server.jar";

   static final List<String> CANDIDATES = List.of("server/" + JAR_NAME,
                                                  "../../server/" + JAR_NAME);

   private final Path   workDir;
   private final String override;

   /**
    * @param workDir  diretório a partir do qual os caminhos relativos são
    *                 resolvidos
    * @param override caminho informado pelo usuário; {@code null} ou vazio
    *                 para usar os caminhos padrão
    */
   public ServerJarLocator(Path workDir, String override) {
      this.workDir  = Objects.requireNonNull(workDir,"workDir");
      this.override = override;
   }

   /** Usa o diretório de trabalho atual e a variável {@value #ENV_VAR}. */
   public static ServerJarLocator fromEnvironment() {
      return new ServerJarLocator(Path.of(""),System.getenv(ENV_VAR));
   }

   /**
    * @return caminho absoluto do JAR do servidor
    * @throws IllegalStateException se o JAR não for encontrado
    */
   public Path locate() {
      if (override != null && !override.isBlank()) {
         Path jar = resolve(override.strip());

         if (Files.isRegularFile(jar))
            return jar;
         throw new IllegalStateException("A variável " + ENV_VAR + " aponta " +
                                         "para um arquivo inexistente: " + jar);
      }
      List<Path> tried = CANDIDATES.stream().map(this::resolve).toList();

      return tried.stream()
                  .filter(Files::isRegularFile)
                  .findFirst()
                  .orElseThrow(() -> new IllegalStateException(
                        "JAR do servidor não encontrado. Gere-o com " +
                        "'mvn clean package -Pserver -DskipTests' ou informe " +
                        "o caminho na variável " + ENV_VAR + ". Caminhos " +
                        "procurados: " + tried));
   }

   private Path resolve(String path) {
      return workDir.resolve(path).toAbsolutePath().normalize();
   }
}
