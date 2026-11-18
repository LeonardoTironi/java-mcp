package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.ToolArguments;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import br.edu.ifpr.mcp_server.repository.RepositoryException;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

/**
 * Base das ferramentas de imóveis. Concentra o registro no servidor e o
 * tratamento de erros, de modo que cada subclasse define apenas sua descrição
 * ({@link #tool()}) e sua lógica ({@link #execute(ToolArguments)}).
 * <p>
 * Erros nunca são lançados ao cliente: parâmetros inválidos e falhas de banco
 * viram um {@link CallToolResult} com {@code isError = true} e uma mensagem
 * em português, permitindo que a IA corrija a chamada.
 */
public abstract class PropertyTool implements McpToolDefinition {
   private static final Logger LOG = LoggerFactory.getLogger(PropertyTool.class);

   protected final PropertyRepository repository;
   protected final ToolResults        results;

   protected PropertyTool(PropertyRepository repository, ToolResults results) {
      this.repository = Objects.requireNonNull(repository,"repository");
      this.results    = Objects.requireNonNull(results,"results");
   }

   /** Definição (nome, descrição e esquema) exposta aos clientes MCP. */
   public abstract Tool tool();

   /** Executa a ferramenta com os argumentos já encapsulados. */
   protected abstract CallToolResult execute(ToolArguments args);

   /** Anotações comuns: ferramentas apenas consultam o banco. */
   protected static ToolAnnotations readOnly(String title) {
      // descreve ao cliente o comportamento da ferramenta
      return ToolAnnotations.builder()
                            .title(title)
                            // apenas lê dados, não altera o banco
                            .readOnlyHint(true)
                            .destructiveHint(false)
                            // repetir a chamada produz o mesmo resultado
                            .idempotentHint(true)
                            // não acessa sistemas externos, só o banco local
                            .openWorldHint(false)
                            .build();
   }

   @Override
   public void register(
                    SyncSpecification<SingleSessionSyncSpecification> mcp) {
      // vincula a ferramenta (definida pela subclasse em "tool()") à
      // implementação da ferramenta
      SyncToolSpecification toolSpec =
                            SyncToolSpecification.builder()
                                                 .tool(tool())
                                                 // quando a ferramenta for
                                                 // selecionada, seu método
                                                 // "run" (abaixo) será
                                                 // invocado para realizar a
                                                 // tarefa programada
                                                 .callHandler(this::run)
                                                 .build();
      // acrescenta a ferramenta ao servidor
      mcp.tools(toolSpec);
   }

   @Override
   public CallToolResult run(McpSyncServerExchange exchange,
                             CallToolRequest request) {
      try {
         // recupera o(s) parâmetro(s) passados pelo cliente e realiza a
         // tarefa programada pela subclasse
         //
         // OBSERVAÇÃO:
         // "ToolArguments" faz a leitura tipada e a validação dos parâmetros
         // (números vindos de JSON podem ser representados por diferentes
         // implementações de java.lang.Number)
         //
         return execute(new ToolArguments(request.arguments()));
      }
      catch (IllegalArgumentException ex) {
         // parâmetro inválido: retorna um resultado de erro ao cliente, com
         // a mensagem que permite à IA corrigir a chamada
         return results.error("Parâmetros inválidos: " + ex.getMessage());
      }
      catch (RepositoryException ex) {
         // falha no banco: registra os detalhes no log do servidor e retorna
         // ao cliente apenas uma mensagem genérica
         //
         // OBSERVAÇÃO:
         // o log é gravado em System.err (ver logback.xml), pois qualquer
         // saída em System.out "quebraria" o protocolo JSON-RPC
         //
         LOG.error("Erro de banco na ferramenta '{}'",request.name(),ex);
         return results.error("Não foi possível consultar o banco de dados " +
                              "de imóveis. Tente novamente mais tarde.");
      }
   }
}
