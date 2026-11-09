package br.edu.ifpr.mcp_server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

   @Test
   void deveUsarValoresPadraoQuandoAmbienteVazio() {
      DatabaseConfig config = DatabaseConfig.fromMap(Map.of());

      assertEquals(DatabaseConfig.DEFAULT_URL,config.url());
      assertEquals(DatabaseConfig.DEFAULT_USER,config.user());
      assertEquals(DatabaseConfig.DEFAULT_PASSWORD,config.password());
   }

   @Test
   void deveUsarValoresDoAmbiente() {
      DatabaseConfig config = DatabaseConfig.fromMap(Map.of(
                         DatabaseConfig.ENV_URL,"jdbc:postgresql://db:5432/x",
                         DatabaseConfig.ENV_USER,"app",
                         DatabaseConfig.ENV_PASSWORD,"segredo"));

      assertEquals("jdbc:postgresql://db:5432/x",config.url());
      assertEquals("app",config.user());
      assertEquals("segredo",config.password());
   }

   @Test
   void deveUsarPadraoQuandoVariavelEmBranco() {
      DatabaseConfig config = DatabaseConfig.fromMap(Map.of(
                                               DatabaseConfig.ENV_URL,"   "));

      assertEquals(DatabaseConfig.DEFAULT_URL,config.url());
   }

   @Test
   void naoDeveExporSenhaNoToString() {
      DatabaseConfig config = new DatabaseConfig("jdbc:x","u","segredo");

      assertFalse(config.toString().contains("segredo"));
      assertTrue(config.toString().contains("****"));
   }

   @Test
   void deveRejeitarUrlVazia() {
      assertThrows(IllegalArgumentException.class,
                   () -> new DatabaseConfig(" ","u","p"));
   }

   @Test
   void deveRejeitarUsuarioNulo() {
      assertThrows(IllegalArgumentException.class,
                   () -> new DatabaseConfig("jdbc:x",null,"p"));
   }

   @Test
   void deveRejeitarSenhaNula() {
      assertThrows(IllegalArgumentException.class,
                   () -> new DatabaseConfig("jdbc:x","u",null));
   }
}
