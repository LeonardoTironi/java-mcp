package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ListPromptsResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.databind.json.JsonMapper;

class McpServer01IT {
   private static final Logger LOG =
                                   LoggerFactory.getLogger(McpServer01IT.class);
   // nome e caminho para o servidor MCP
   private static final String SERVER_PATH = "server/mcp_server01_server.jar";

   private static McpSyncClient client;

   @BeforeAll
   static void setUpBeforeClass() throws Exception {
      // antes que a classe de testes seja carregada, este método providencia
      // alguma inicialização ou alocação de recursos, neste caso, o servidor
      // MCP é executado e o cliente se conecta a ele
      File serverJar = new File(SERVER_PATH);
      assertTrue(serverJar.isFile(),"JAR do servidor não encontrado: " +
                                    serverJar.getAbsolutePath());

      String           jarPath = serverJar.getAbsolutePath();
      ServerParameters params  = ServerParameters.builder("java")
    			                                 .args("-jar",jarPath)
    			                                 .build();
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(
    			                                              new JsonMapper());
      StdioClientTransport transport  = new StdioClientTransport(params,
    			                                                 jsonMapper);

      transport.setStdErrorHandler(line -> LOG.info("[SERVER STDERR] " + line));

      client = McpClient.sync(transport).build();
      client.initialize();
   }

   @AfterAll
   static void tearDownAfterClass() throws Exception {
      // ao terminar todos os testes, todos os métodos nesta classe foram
      // executados, o cliente se desconecta do servidor
      if (client != null)
         client.closeGracefully();
   }

   // ==========================================================================
   // Os métodos de teste à seguir simulam a aplicação cliente:
   // - há ferramentas (tools) disponíveis???
   // - a ferramenta 'obter_documento' existe???
   // - a ferramenta 'obter_documento' executou com sucesso???
   // ==========================================================================

   @Test
   void deveDisponibilizarFerramentas() {
      ListToolsResult result = client.listTools();

      assertNotNull(result,"O servidor não retornou o resultado.");
      assertNotNull(result.tools(),"A lista de ferramentas não pode ser null.");
      assertFalse(result.tools().isEmpty(),"O servidor deveria disponibilizar" +
                                           " pelo menos uma ferramenta." );
   }

   @Test
   void deveDisponibilizarObterDocumento() {
      ListToolsResult result = client.listTools();
                                      // retorna a lista de ferramentas
      Tool            tool   = result.tools()
                                     // cria um fluxo para percorrer a lista
    			                     .stream()
    			                     // filtra a lista para um elemento
    			                     .filter(item ->
    			                          "obter_documento".equals(item.name()))
    			                     // verifica se há alguma elemento...
    			                     .findFirst()
    			                     // ...caso contrário, retorna "null"
    			                     .orElse(null);

      assertNotNull(tool,"A ferramenta obter_documento não foi encontrada.");
      assertNotNull(tool.description(),"A ferramenta deveria possuir " +
                                       "descrição.");
      assertNotNull(tool.inputSchema(),"A ferramenta deveria possuir " +
                                       "'inputSchema'.");
   }

   @Test
   void deveExecutarObterDocumento() {
      CallToolRequest request = CallToolRequest.builder("obter_documento")
    			                               .arguments(Map.of("id",123))
    			                               .build();
      CallToolResult  result  = client.callTool(request);

      assertNotNull(result,"O servidor não retornou resultado.");
      assertFalse(result.isError(),"A execução da ferramenta não deveria " +
                                   "resultar em erro.");
      assertNotNull(result.content(),"O resultado deveria possuir 'content'.");
      assertFalse(result.content().isEmpty(),"O resultado deveria possuir " +
                                             "pelo menos um 'content'." );
   }

   // ==========================================================================
   // Os métodos de teste a seguir simulam a aplicação cliente usando as
   // ferramentas, prompts e recursos de imóveis:
   // - as ferramentas de imóveis estão disponíveis?
   // - os prompts e os recursos estão disponíveis?
   // - argumentos inválidos são rejeitados antes de consultar o banco?
   // - as buscas, os detalhes e as estatísticas retornam os imóveis certos?
   //
   // Os testes marcados com @EnabledIfEnvironmentVariable precisam do banco
   // (docker compose up -d postgres) e só executam se DB_URL estiver definida;
   // o processo do servidor herda essa variável do processo dos testes.
   // ==========================================================================

   private static CallToolResult callTool(String name,
                                          Map<String, Object> args) {
      // monta a requisição com o nome da ferramenta e seus argumentos e a
      // envia ao servidor, exatamente como faria a aplicação cliente
      CallToolResult result = client.callTool(
                                       CallToolRequest.builder(name)
                                                      .arguments(args)
                                                      .build());

      assertNotNull(result,"O servidor não retornou resultado.");
      return result;
   }

   private static String text(CallToolResult result) {
      // as ferramentas respondem com um único 'content' do tipo texto
      return ((TextContent) result.content().get(0)).text();
   }

   /** Chama a ferramenta, exige sucesso e retorna o texto do resultado. */
   private static String success(String name, Map<String, Object> args) {
      CallToolResult result = callTool(name,args);

      assertFalse(result.isError(),"Não deveria ser erro: " + text(result));
      return text(result);
   }

   @Test
   void deveDisponibilizarFerramentasDeImoveis() {
                                 // retorna a lista de ferramentas...
      List<String> names = client.listTools().tools().stream()
                                 // ...e fica apenas com o nome de cada uma
                                 .map(Tool::name).toList();

      assertTrue(names.containsAll(List.of("buscar_imoveis","detalhar_imovel",
                                           "listar_localidades",
                                           "estatisticas_precos")),
                 "Ferramentas encontradas: " + names);
   }

   @Test
   void deveDisponibilizarPrompts() {
      ListPromptsResult result = client.listPrompts();

      // compara como conjunto: a ordem em que o servidor lista não importa
      assertEquals(Set.of("buscar_imovel_ideal","comparar_imoveis",
                          "analisar_mercado"),
                   result.prompts().stream().map(Prompt::name)
                         .collect(Collectors.toSet()));
   }

   @Test
   void deveGerarPromptDeComparacao() {
      // os códigos chegam em minúsculas e devem voltar normalizados
      GetPromptResult result = client.getPrompt(
            new GetPromptRequest("comparar_imoveis",
                                 Map.of("codigos","imv-0001, IMV-0004")));
      String          text   = ((TextContent) result.messages().get(0)
                                                    .content()).text();

      assertTrue(text.contains("IMV-0001, IMV-0004"));
      assertTrue(text.contains("detalhar_imovel"));
   }

   @Test
   void promptComArgumentoInvalidoDeveFalhar() {
      // comparar exige de 2 a 5 códigos: com apenas um, o servidor responde
      // com erro de protocolo, que o cliente recebe como exceção
      McpError ex = assertThrows(McpError.class,
            () -> client.getPrompt(new GetPromptRequest(
                                      "comparar_imoveis",
                                      Map.of("codigos","IMV-0001"))));

      assertTrue(ex.getMessage().contains("de 2 a 5"),ex.getMessage());
   }

   @Test
   void deveDisponibilizarRecursos() {
      // há um recurso fixo (URI completa) e um modelo de recurso, cuja URI
      // tem um trecho variável: {cidade}
      assertEquals(List.of("imoveis://localidades"),
                   client.listResources().resources().stream()
                         .map(Resource::uri).toList());
      assertEquals(List.of("imoveis://precos/{cidade}"),
                   client.listResourceTemplates().resourceTemplates().stream()
                         .map(ResourceTemplate::uriTemplate).toList());
   }

   @Test
   void recursoComUriInvalidaDeveFalhar() {
      // '%20' é um espaço: o nome da cidade fica em branco; a URI é validada
      // antes de qualquer acesso ao banco, por isso o teste roda sem ele
      McpError ex = assertThrows(McpError.class,
            () -> client.readResource(
                        new ReadResourceRequest("imoveis://precos/%20")));

      assertTrue(ex.getMessage().contains("informe o nome da cidade"),
                 ex.getMessage());
   }

   private static String readResource(String uri) {
      // lê o recurso e retorna o texto (JSON) do primeiro conteúdo
      ReadResourceResult result = client.readResource(
                                             new ReadResourceRequest(uri));

      return ((TextResourceContents) result.contents().get(0)).text();
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void deveLerRecursoDeLocalidades() {
      String text = readResource("imoveis://localidades");

      assertTrue(text.contains("Curitiba"),text);
      assertTrue(text.contains("Londrina"),text);
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void deveLerRecursoDePrecosDaCidade() {
      String text = readResource("imoveis://precos/Curitiba");

      assertTrue(text.contains("\"city\" : \"Curitiba\""),text);
      // casas à venda disponíveis em Curitiba: IMV-0001 e IMV-0004
      assertTrue(text.contains("1250000"),text);
      // cidade sem imóveis: o recurso não existe
      assertThrows(McpError.class,
                   () -> readResource("imoveis://precos/Atlantida"));
   }

   @Test
   void detalharSemCodigoNemIdDeveSerErro() {
      // validado antes de qualquer acesso ao banco; o erro de uma ferramenta
      // não é exceção: volta no resultado, com 'isError' verdadeiro
      CallToolResult result = callTool("detalhar_imovel",Map.of());

      assertTrue(result.isError());
      assertTrue(text(result).contains("'codigo' ou 'id'"));
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void deveBuscarCasasEmCuritiba() {
      String text = success("buscar_imoveis",Map.of("tipo","HOUSE",
                                                    "cidade","Curitiba"));

      assertTrue(text.contains("IMV-0001"),text);
      assertTrue(text.contains("IMV-0004"),text);
      // rascunho não aparece; apartamento não é casa
      assertFalse(text.contains("IMV-0015"),text);
      assertFalse(text.contains("IMV-0002"),text);
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void buscaPorLocacaoDeveIncluirVendaELocacao() {
      String text = success("buscar_imoveis",Map.of("finalidade","RENT",
                                                    "cidade","Curitiba",
                                                    "ordenarPor","preco_asc"));

      // IMV-0003 é "venda e locação": aluguel de R$ 1.600, o mais barato
      assertTrue(text.contains("IMV-0003"),text);
      assertTrue(text.indexOf("IMV-0003") < text.indexOf("IMV-0002"),text);
      assertFalse(text.contains("IMV-0001"),text);
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void buscaPorFaixaEQuartos() {
      // limite máximo: o seed tem mais imóveis nessa faixa do que o padrão (10)
      String text = success("buscar_imoveis",Map.of("finalidade","SELL",
                                                    "precoMax",900000,
                                                    "quartosMin",3,
                                                    "limite",50));

      assertTrue(text.contains("IMV-0001"),text);
      assertTrue(text.contains("IMV-0009"),text);
      assertFalse(text.contains("IMV-0004"),text);   // acima do orçamento
      assertFalse(text.contains("IMV-0010"),text);   // 2 quartos
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void tentativaDeInjecaoDeSqlNaoRetornaImoveis() {
      // se os valores fossem concatenados no SQL, "OR 1=1" traria todos os
      // imóveis; como vão como parâmetros, são apenas textos sem resultado
      String text = success("buscar_imoveis",Map.of("texto","x' OR 1=1 --",
                                                    "cidade","' OR ''='"));

      assertTrue(text.startsWith("Nenhum imóvel encontrado"),text);
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void deveDetalharImovelComCondominioEProprietario() {
      String text = success("detalhar_imovel",Map.of("codigo","imv-0008"));

      assertTrue(text.startsWith("Imóvel IMV-0008"),text);
      assertTrue(text.contains("Residencial Parque Ecoville"),text);
      assertTrue(text.contains("Construtora Horizonte"),text);
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void imovelInexistenteOuExcluidoDeveSerErro() {
      // IMV-9999 não existe; IMV-0017 existe no seed, mas foi excluído
      assertTrue(callTool("detalhar_imovel",
                          Map.of("codigo","IMV-9999")).isError());
      assertTrue(callTool("detalhar_imovel",
                          Map.of("codigo","IMV-0017")).isError());
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void deveListarCidadesEBairros() {
      // sem argumentos lista as cidades; informando a cidade, os bairros dela
      String cities        = success("listar_localidades",Map.of());
      String neighborhoods = success("listar_localidades",
                                     Map.of("cidade","Londrina"));

      assertTrue(cities.contains("Curitiba"),cities);
      assertTrue(cities.contains("Paranaguá"),cities);
      assertTrue(neighborhoods.contains("Gleba Palhano"),neighborhoods);
      assertFalse(neighborhoods.contains("Batel"),neighborhoods);
   }

   @Test
   @EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
   void deveCalcularEstatisticasDePreco() {
      String text = success("estatisticas_precos",
                            Map.of("cidade","Curitiba","tipo","HOUSE",
                                   "finalidade","SELL"));

      assertTrue(text.contains("\"type\" : \"HOUSE\""),text);
      // casas à venda disponíveis em Curitiba: IMV-0001 e IMV-0004
      assertTrue(text.contains("\"total\" : 2"),text);
      assertTrue(text.contains("850000"),text);
      assertTrue(text.contains("1250000"),text);
   }
}
