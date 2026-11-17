package br.edu.ifpr.mcp_server.mcp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.SortOrder;

class ToolArgumentsTest {

   private static ToolArguments args(String name, Object value) {
      // HashMap (e não Map.of) porque Map.of não aceita valores nulos, e
      // vários testes simulam um argumento enviado como null
      Map<String, Object> map = new HashMap<>();

      map.put(name,value);
      return new ToolArguments(map);
   }

   private static void assertInvalid(Runnable call, String expectedParam) {
      // a conversão deve falhar e a mensagem deve citar o parâmetro entre
      // aspas, para que o modelo saiba qual argumento corrigir
      IllegalArgumentException ex =
                 assertThrows(IllegalArgumentException.class,call::run);

      assertTrue(ex.getMessage().contains("'" + expectedParam + "'"),
                 ex.getMessage());
   }

   // ==========================================================================
   // Os métodos de teste a seguir verificam a leitura dos argumentos que o
   // cliente envia às ferramentas (valores JSON sem tipo garantido):
   // - textos são aparados e, em branco, tratados como ausentes?
   // - enumerações ignoram maiúsculas/minúsculas?
   // - números aceitam valor JSON ou texto e respeitam os limites?
   // - booleanos aceitam true/false em JSON ou texto?
   // - valores inválidos geram erro citando o parâmetro?
   // ==========================================================================

   // ---------------------------------------------------------------- texto

   @Test
   void mapaNuloDeveSerTratadoComoVazio() {
      // o cliente pode chamar a ferramenta sem enviar 'arguments'
      assertNull(new ToolArguments(null).optString("x"));
   }

   @Test
   void textoDeveSerAparado() {
      assertEquals("Curitiba",args("cidade","  Curitiba ").optString("cidade"));
   }

   @Test
   void textoAusenteOuEmBrancoDeveSerNulo() {
      assertNull(args("cidade",null).optString("cidade"));
      assertNull(args("cidade","   ").optString("cidade"));
   }

   @Test
   void textoComTipoErradoDeveSerInvalido() {
      assertInvalid(() -> args("cidade",10).optString("cidade"),"cidade");
   }

   // ------------------------------------------------------------- enumeração

   @Test
   void enumDeveIgnorarCaixa() {
      assertEquals(PropertyType.HOUSE,
                   args("tipo","house").optEnum("tipo",PropertyType.class));
   }

   @Test
   void enumAusenteDeveSerNulo() {
      assertNull(args("tipo",null).optEnum("tipo",PropertyType.class));
   }

   @Test
   void enumDesconhecidoDeveListarValoresAceitos() {
      IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class,
                         () -> args("tipo","castelo")
                                     .optEnum("tipo",PropertyType.class));

      assertTrue(ex.getMessage().contains("'tipo'"));
      assertTrue(ex.getMessage().contains("APARTMENT"));
   }

   @Test
   void valorComConversorPersonalizado() {
      // a ordenação usa chaves próprias ("area_desc"), não o nome da enum
      assertEquals(SortOrder.AREA_DESC,
                   args("ordenarPor","area_desc")
                         .optValue("ordenarPor",SortOrder::fromKey));
   }

   @Test
   void conversorPersonalizadoInvalidoDeveCitarParametro() {
      assertInvalid(() -> args("ordenarPor","x")
                                .optValue("ordenarPor",SortOrder::fromKey),
                    "ordenarPor");
   }

   // ---------------------------------------------------------------- decimal

   @Test
   void decimalDeveAceitarNumeroJson() {
      // número com casas decimais chega como Double; compareTo ignora a
      // escala do BigDecimal (850000.5 e 850000.50 são o mesmo valor)
      assertEquals(0,new BigDecimal("850000.5")
                        .compareTo(args("p",850000.5).optDecimal("p")));
      assertEquals(new BigDecimal("900000"),
                   args("p",900000).optDecimal("p"));
   }

   @Test
   void decimalDeveAceitarTextoNumerico() {
      assertEquals(new BigDecimal("1500.75"),
                   args("p","1500.75").optDecimal("p"));
   }

   @Test
   void decimalAusenteOuEmBrancoDeveSerNulo() {
      assertNull(args("p",null).optDecimal("p"));
      assertNull(args("p"," ").optDecimal("p"));
   }

   @Test
   void decimalNegativoDeveSerInvalido() {
      assertInvalid(() -> args("p",-1).optDecimal("p"),"p");
   }

   @Test
   void decimalNaoNumericoDeveSerInvalido() {
      // texto não numérico, "não é um número" (NaN) e lista
      assertInvalid(() -> args("p","mil").optDecimal("p"),"p");
      assertInvalid(() -> args("p",Double.NaN).optDecimal("p"),"p");
      assertInvalid(() -> args("p",List.of(1)).optDecimal("p"),"p");
   }

   // ---------------------------------------------------------------- inteiro

   @Test
   void inteiroDentroDoIntervalo() {
      // o mesmo valor como inteiro, como decimal sem fração e como texto
      assertEquals(3,args("q",3).optInteger("q",0,50));
      assertEquals(3,args("q",3.0).optInteger("q",0,50));
      assertEquals(3,args("q","3").optInteger("q",0,50));
   }

   @Test
   void inteiroNasBordasDoIntervalo() {
      // os limites mínimo e máximo são inclusivos
      assertEquals(1,args("q",1).optInteger("q",1,50));
      assertEquals(50,args("q",50).optInteger("q",1,50));
   }

   @Test
   void inteiroAusenteDeveSerNulo() {
      assertNull(args("q",null).optInteger("q",0,50));
   }

   @Test
   void inteiroForaDoIntervaloDeveSerInvalido() {
      assertInvalid(() -> args("q",0).optInteger("q",1,50),"q");
      assertInvalid(() -> args("q",51).optInteger("q",1,50),"q");
   }

   @Test
   void inteiroComCasasDecimaisDeveSerInvalido() {
      assertInvalid(() -> args("q",2.5).optInteger("q",0,50),"q");
   }

   // --------------------------------------------------------------- booleano

   @Test
   void booleanoJsonETexto() {
      // valor JSON ou texto, ignorando caixa e espaços
      assertTrue(args("d",true).optBoolean("d"));
      assertFalse(args("d","FALSE").optBoolean("d"));
      assertTrue(args("d"," true ").optBoolean("d"));
   }

   @Test
   void booleanoAusenteOuEmBrancoDeveSerNulo() {
      assertNull(args("d",null).optBoolean("d"));
      assertNull(args("d","").optBoolean("d"));
   }

   @Test
   void booleanoInvalidoDeveSerInvalido() {
      assertInvalid(() -> args("d","talvez").optBoolean("d"),"d");
      // 1 não é convertido para true: só true/false são aceitos
      assertInvalid(() -> args("d",1).optBoolean("d"),"d");
   }
}
