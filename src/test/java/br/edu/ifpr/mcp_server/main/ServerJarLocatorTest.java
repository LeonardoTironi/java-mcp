package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerJarLocatorTest {
   // o JUnit cria uma pasta temporária vazia para cada teste e a apaga no
   // final; ela faz o papel da raiz do projeto
   @TempDir
   Path project;

   private Path createServerJar() throws IOException {
      // cria um JAR vazio em 'server/', onde o build coloca o do servidor;
      // para localizar, basta que o arquivo exista
      Path jar = project.resolve("server").resolve(ServerJarLocator.JAR_NAME);

      Files.createDirectories(jar.getParent());
      return Files.createFile(jar);
   }

   // ==========================================================================
   // Os métodos de teste a seguir simulam o cliente procurando o JAR do
   // servidor (o 2º argumento do construtor é a variável de ambiente):
   // - o JAR é encontrado a partir da raiz do projeto?
   // - o JAR é encontrado a partir de 'target/dist-client'?
   // - o caminho informado na variável tem prioridade?
   // - sem JAR, a mensagem de erro orienta como gerá-lo?
   // ==========================================================================

   @Test
   void deveEncontrarJarAPartirDaRaizDoProjeto() throws IOException {
      Path jar = createServerJar();

      assertEquals(jar.toAbsolutePath(),
                   new ServerJarLocator(project,null).locate());
   }

   @Test
   void deveEncontrarJarAPartirDeDistClient() throws IOException {
      Path jar        = createServerJar();
      Path distClient = Files.createDirectories(project.resolve("target")
                                                       .resolve("dist-client"));

      // a variável em branco é ignorada; de 'target/dist-client' o JAR é
      // procurado em '../../server/'
      assertEquals(jar.toAbsolutePath(),
                   new ServerJarLocator(distClient,"").locate());
   }

   @Test
   void deveUsarCaminhoInformadoNaVariavel() throws IOException {
      Path jar = Files.createFile(project.resolve("outro.jar"));

      assertEquals(jar.toAbsolutePath(),
                   new ServerJarLocator(project,"outro.jar").locate());
   }

   @Test
   void variavelComArquivoInexistenteDeveFalhar() throws IOException {
      // mesmo existindo o JAR padrão, um caminho errado na variável é erro
      // (e não é trocado silenciosamente pelo padrão)
      createServerJar();

      IllegalStateException ex =
            assertThrows(IllegalStateException.class,
                         () -> new ServerJarLocator(project,"nao-existe.jar")
                                     .locate());
      assertTrue(ex.getMessage().contains(ServerJarLocator.ENV_VAR));
   }

   @Test
   void semJarDeveFalharComOrientacao() {
      IllegalStateException ex =
            assertThrows(IllegalStateException.class,
                         () -> new ServerJarLocator(project,null).locate());
      // a mensagem indica o perfil Maven que gera o JAR do servidor
      assertTrue(ex.getMessage().contains("-Pserver"),ex.getMessage());
   }
}
