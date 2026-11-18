package br.edu.ifpr.mcp_server.mcp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.domain.Purpose;

class JsonSchemaTest {

   // ==========================================================================
   // Os métodos de teste a seguir verificam o construtor de JSON Schema usado
   // na definição das ferramentas:
   // - a ordem, os tipos e os parâmetros obrigatórios são mantidos?
   // - sem obrigatórios, a chave 'required' é omitida?
   // - enumerações listam os valores e o significado de cada um?
   // - um parâmetro repetido é rejeitado?
   // ==========================================================================

   @Test
   void deveManterOrdemTiposEObrigatorios() {
      // "b" é o único obrigatório; os demais são opcionais
      Map<String, Object> schema = JsonSchema.object("b")
                                             .string("b","texto")
                                             .number("a","número")
                                             .integer("c","inteiro",1,5)
                                             .bool("d","booleano")
                                             .build();
      Map<?, ?>           props  = (Map<?, ?>) schema.get("properties");

      assertEquals("object",schema.get("type"));
      assertEquals(List.of("b"),schema.get("required"));
      // parâmetros não declarados não são aceitos
      assertEquals(false,schema.get("additionalProperties"));
      // a ordem de declaração é mantida (e não a alfabética)
      assertEquals(List.of("b","a","c","d"),List.copyOf(props.keySet()));
      assertEquals("number",((Map<?, ?>) props.get("a")).get("type"));
      // números não podem ser negativos
      assertEquals(0,((Map<?, ?>) props.get("a")).get("minimum"));
      assertEquals(5,((Map<?, ?>) props.get("c")).get("maximum"));
   }

   @Test
   void semObrigatoriosNaoIncluiRequired() {
      assertFalse(JsonSchema.object().build().containsKey("required"));
   }

   @Test
   void enumeracaoDeveListarValoresERotulos() {
      Map<?, ?> props = (Map<?, ?>) JsonSchema.object()
                                    .enumeration("f","Finalidade.",
                                                 Purpose.SELL,Purpose.RENT)
                                    .build().get("properties");
      Map<?, ?> f     = (Map<?, ?>) props.get("f");

      assertEquals(List.of("SELL","RENT"),f.get("enum"));
      // a descrição ganha o rótulo em português de cada valor
      assertEquals("Finalidade. Valores: SELL = venda, RENT = locação.",
                   f.get("description"));
   }

   @Test
   void parametroRepetidoDeveSerRejeitado() {
      assertThrows(IllegalArgumentException.class,
                   () -> JsonSchema.object().string("x","a").string("x","b"));
   }
}
