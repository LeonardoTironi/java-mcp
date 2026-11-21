package br.edu.ifpr.mcp_server.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_server.mcp.utils.McpPromptDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.McpResourceDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import tools.jackson.databind.json.JsonMapper;

public class McpServer01SyncStdio {
   private static final Logger LOG =
                            LoggerFactory.getLogger(McpServer01SyncStdio.class);

   private final McpSyncServer server;

   private void registerTools(
                          SyncSpecification<SingleSessionSyncSpecification> mcp,
                          ServerFeatures features)
                throws Exception {
      // para cada ferramenta disponível no servidor, executa seu registro para
      // posterior execução
      for (McpToolDefinition tool : features.tools())
         tool.register(mcp);
   }

   private void registerPrompts(
                          SyncSpecification<SingleSessionSyncSpecification> mcp,
                          ServerFeatures features) {
      // para cada modelo de prompt disponível no servidor, executa seu registro
      for (McpPromptDefinition prompt : features.prompts())
         prompt.register(mcp);
   }

   private void registerResources(
                          SyncSpecification<SingleSessionSyncSpecification> mcp,
                          ServerFeatures features) {
      // para cada recurso (URI fixa ou modelo de URI) disponível no servidor,
      // executa seu registro
      for (McpResourceDefinition resource : features.resources())
         resource.register(mcp);
   }

   private SyncSpecification<SingleSessionSyncSpecification> configMcpServer() {
      // cria o objeto para comunicação entre servidor e cliente
      JacksonMcpJsonMapper         jsonMapper        =
                                     new JacksonMcpJsonMapper(new JsonMapper());
      // estabelece que a comunicação usará o transporte STDIO
      StdioServerTransportProvider transportProvider =
                                   new StdioServerTransportProvider(jsonMapper);
      // identifica quais "capacidades" o servidor disponibiliza, neste caso,
      // ferramentas, prompts, recursos e 'logging'
      /* capacidades, o que o servidor disponibiliza, do servidor
      completions  presente se o servidor suporta sugestões para auto completar
                   argumentos
      experimental capacidades não padronizadas que o servidor suporta!!!
      logging      presente se o servidor suporta envio de mensagen ('logging')
                   ao cliente
      prompts      presente se o servidor oferece modelos ('templates') de
                   prompts
      resources    presente se o servidor oferece recursos que podem ser lidos
      tools        presente se o servidor oferece ferramentas
       */
      ServerCapabilities capabilities    = ServerCapabilities.builder()
                                                             .tools(true)
                                                             .prompts(true)
                                                             // sem 'subscribe'
                                                             // e 'listChanged'
                                                             .resources(false,
                                                                        false)
                                                             .logging()
                                                             .build();
      // identificação do servidor
      Implementation      implementation = Implementation.builder("MCP Server",
                                                                  "1.0.0")
                                                         .build();

      // configura o servidor com transporte STDIO
      return McpServer.sync(transportProvider)
                      .serverInfo(implementation)
                      .capabilities(capabilities);
   }

   public McpServer01SyncStdio(ServerFeatures features) throws Exception {
      // configura/inicializa o servidor
      SyncSpecification<SingleSessionSyncSpecification> srv = configMcpServer();

      // registra as ferramentas, os prompts e os recursos deste servidor
      registerTools(srv,features);
      registerPrompts(srv,features);
      registerResources(srv,features);
      // cria o servidor
      this.server = srv.build();

      LOG.info("MCP Server 01 iniciado via STDIO...");
   }

   /** Encerra o servidor, liberando o transporte STDIO. */
   public void close() {
      server.closeGracefully();
   }

   public static void main(String[] args) throws Exception {
      McpServer01SyncStdio app =
                     new McpServer01SyncStdio(ServerFeatures.fromEnvironment());

      // ao encerrar o processo (fim da entrada padrão ou Ctrl+C), fecha o
      // servidor de forma ordenada
      Runtime.getRuntime().addShutdownHook(new Thread(app::close));
   }
}
