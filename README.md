<!--
Markdown Cheatsheet
https://github.com/adam-p/markdown-here/wiki/markdown-cheatsheet
-->

## MCP Server: Busca de Imóveis
Servidor MCP ([Model Context Protocol](https://modelcontextprotocol.io/docs/2026-07-28/getting-started/intro)) escrito em Java com o [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) que permite a um assistente de IA (Claude Desktop, MCP Inspector ou o cliente Java deste projeto) **pesquisar imóveis** — casas, apartamentos, terrenos, salas comerciais e sítios — armazenados em um banco **PostgreSQL**.

Com ele, o usuário pode conversar em linguagem natural, por exemplo:

> *"Quero comprar uma casa em Curitiba com 3 quartos até 900 mil."*
> *"Quanto custa, em média, alugar um apartamento em Londrina?"*
> *"Compare os imóveis IMV-0001 e IMV-0004."*

e a IA decide quais ferramentas do servidor chamar e com quais parâmetros.

O MCP define três primitivas básicas: *tools* (ferramentas), *resources* (recursos) e *prompts*. Este servidor oferece as três: **ferramentas**, **recursos** e **prompts**. A comunicação entre cliente e servidor usa o transporte **STDIO**: o cliente executa o servidor como um processo e troca mensagens JSON-RPC pela entrada e saída padrão.

### Demonstração em vídeo
Veja o servidor em ação no YouTube: o assistente de IA consulta, filtra e compara imóveis usando as ferramentas, recursos e prompts do MCP.

[![Demonstração do MCP Server: Busca de Imóveis](https://img.youtube.com/vi/aIlMGlaIaN0/hqdefault.jpg)](https://youtu.be/aIlMGlaIaN0 "Assistir à demonstração no YouTube")

*Clique na imagem para assistir ao vídeo no YouTube.*

### Sumário
- [O que é o MCP](#o-que-é-o-mcp)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Banco de dados](#banco-de-dados)
- [Ferramentas (*tools*)](#ferramentas-tools)
- [Recursos (*resources*)](#recursos-resources)
- [Prompts](#prompts)
- [Gerar e executar a aplicação](#gerar-e-executar-a-aplicação)
- [MCP Inspector no Docker Desktop](#mcp-inspector-no-docker-desktop)
- [Claude Desktop](#instalar-o-servidor-mcp-java-local-no-claude-desktop)
- [Claude Code](#claude-code)
- [Testes](#testes)
- [Referências](#referências)

---

### O que é o MCP
O **Model Context Protocol** é um protocolo aberto que padroniza como aplicações de IA se conectam a fontes de dados e a sistemas externos. Sem ele, cada assistente (Claude, Gemini, Cursor...) precisaria de uma integração própria com cada sistema; com ele, um servidor escrito uma vez funciona em qualquer aplicação compatível.

#### Participantes
| Participante | O que é | Neste projeto |
|---|---|---|
| **Host** | a aplicação de IA que o usuário usa; conversa com o modelo de linguagem e decide quando usar os servidores | Claude Desktop, Claude Code, MCP Inspector |
| **Cliente MCP** | componente dentro do *host* que mantém **uma** conexão com **um** servidor | o cliente embutido de cada *host* e o `McpClient01SyncStdio` (cliente Java interativo) |
| **Servidor MCP** | programa que oferece dados e ações por meio do protocolo | `McpServer01SyncStdio` (este servidor de imóveis) |

```
+------------ Host (ex.: Claude Desktop) ------------+
|  modelo de IA  <-->  Cliente MCP                   |
+---------------------------+------------------------+
                            | JSON-RPC 2.0 sobre STDIO
                            v
              Servidor MCP (java -jar ...)  --JDBC-->  PostgreSQL
```

#### Camadas: mensagens e transporte
- **Mensagens:** todas seguem o **JSON-RPC 2.0**. Há *requisições* (com `id`, que exigem resposta), *respostas* (com o mesmo `id` e `result` ou `error`) e *notificações* (sem `id`, sem resposta).
- **Transporte:** este servidor usa **STDIO**. O *host* executa o servidor como um processo filho (`java -jar mcp_server01_server.jar`) e troca mensagens pela entrada padrão (cliente -> servidor) e saída padrão (servidor -> cliente), **uma mensagem JSON por linha**. Por isso nada além do protocolo pode ser escrito em `System.out`: os *logs* vão para a saída de erro (`<target>System.err</target>` no `logback.xml`), que o *host* exibe ou grava em arquivo. O outro transporte padrão, **Streamable HTTP**, serve para servidores remotos e não é usado aqui.

#### Ciclo de vida de uma sessão
1. **Inicialização** — o cliente envia `initialize` com a versão do protocolo e suas capacidades; o servidor responde com a versão acordada, seu nome e **suas capacidades** (aqui: `tools`, `prompts`, `resources` e `logging`). O cliente confirma com a notificação `notifications/initialized`.
2. **Descoberta** — o cliente pergunta o que existe: `tools/list`, `resources/list`, `resources/templates/list` e `prompts/list`. Nomes, descrições e esquemas retornados são o que a IA "lê" para decidir o que usar; por isso as descrições deste servidor são detalhadas e em português.
3. **Uso** — `tools/call`, `resources/read` e `prompts/get`. O servidor também pode enviar notificações, como `notifications/message` (*logging*), usada pela ferramenta `obter_documento`.
4. **Encerramento** — o cliente fecha a entrada padrão do processo; o servidor termina de forma ordenada (*shutdown hook* em `McpServer01SyncStdio`).

Exemplo real (resumido) de uma chamada de ferramenta:

```
-> {"jsonrpc":"2.0","id":6,"method":"tools/call",
   "params":{"name":"buscar_imoveis",
             "arguments":{"tipo":"HOUSE","finalidade":"SELL","cidade":"Curitiba","precoMax":900000}}}

<- {"jsonrpc":"2.0","id":6,"result":{"isError":false,
   "content":[{"type":"text","text":"1 imóvel encontrado.\n\n[ { \"code\" : \"IMV-0001\", ... } ]"}]}}
```

#### As três opções para os agentes
O que diferencia é **quem decide usá-las**:

| Opção | Quem controla | Para que serve | Métodos | Neste servidor |
|---|---|---|---|---|
| **Tools** (ferramentas) | o **modelo de IA**, que escolhe a ferramenta e os argumentos durante a conversa | executar ações ou consultas com parâmetros | `tools/list`, `tools/call` | `buscar_imoveis`, `detalhar_imovel`, `listar_localidades`, `estatisticas_precos`, `obter_documento` |
| **Resources** (recursos) | a **aplicação** ou o usuário, que escolhe o que anexar como contexto | fornecer dados somente leitura identificados por uma URI | `resources/list`, `resources/templates/list`, `resources/read` | `imoveis://localidades`, `imoveis://precos/{cidade}` |
| **Prompts** | o **usuário**, que escolhe um modelo pronto (no Claude Desktop, botão **+**) | mensagens-modelo com argumentos que orientam a IA em uma tarefa | `prompts/list`, `prompts/get` | `buscar_imovel_ideal`, `comparar_imoveis`, `analisar_mercado` |

Cada ferramenta declara seus parâmetros em um **JSON Schema** (`inputSchema`): tipos, valores aceitos (`enum`), limites e obrigatoriedade. O cliente e o SDK usam esse esquema para validar a chamada, e a IA, para montar os argumentos.

#### Dois tipos de erro
- **Erro de execução de ferramenta** — o resultado volta normalmente, com `isError: true` e uma mensagem. Ele é entregue **à IA**, que pode corrigir os argumentos e tentar de novo ou explicar o problema ao usuário (ex.: `tipo = castelo`, banco fora do ar).
- **Erro de protocolo (JSON-RPC)** — a resposta traz `error` com um código padrão: `-32602` parâmetros inválidos, `-32002` recurso não encontrado, `-32603` erro interno. É usado por recursos e prompts, que não têm "resultado de erro".

#### Como este projeto implementa o MCP
- `McpServer01SyncStdio` cria o servidor com o MCP Java SDK (`McpServer.sync(...)` + `StdioServerTransportProvider`), declara as capacidades e registra as primitivas.
- `ServerFeatures` é o ponto único de composição: cria cada ferramenta, recurso e prompt com suas dependências.
- As interfaces `McpToolDefinition`, `McpResourceDefinition` e `McpPromptDefinition` definem o contrato de cada primitiva; as classes base `PropertyTool` e `PropertyResource` concentram o registro e a conversão de exceções nos dois tipos de erro acima.

---

### Tecnologias
| Item | Versão | Uso |
|---|---|---|
| Java (JDK) | 25 | linguagem |
| Maven | 3.9+ | *build* |
| MCP Java SDK | 2.0.1 | protocolo MCP (servidor e cliente) |
| Jackson | 3 | JSON |
| PostgreSQL JDBC Driver | 42.7.4 | acesso ao banco |
| PostgreSQL | 17 (Docker) | banco de dados |
| JUnit | 6 | testes unitários e de integração |
| Docker Desktop | — | PostgreSQL e MCP Inspector |

Não são usados Spring, Lombok nem geradores de código. Os testes unitários usam implementações falsas escritas à mão (sem Mockito).

---

### Arquitetura
O código fica no pacote `br.edu.ifpr.mcp_server`, dividido por responsabilidade:

| Pacote | Classes | Responsabilidade |
|---|---|---|
| `main` | `McpServer01SyncStdio`, `ServerFeatures`, `McpClient01SyncStdio`, `ServerJarLocator` | inicia o servidor (transporte STDIO), monta ferramentas, recursos e prompts com suas dependências; cliente interativo e localização do JAR do servidor que ele executa |
| `config` | `DatabaseConfig` | lê as variáveis de ambiente `DB_URL`, `DB_USER`, `DB_PASSWORD` |
| `db` | `ConnectionFactory`, `DriverManagerConnectionFactory` | abre conexões JDBC (com *timeout*) |
| `domain` | `PropertyFilter`, `PropertySummary`, `PropertyDetail`, `Address`, `LocationCount`, `PriceStats`, enums `PropertyType`, `Purpose`, `UsageType`, `AvailabilityStatus`, `SortOrder` | dados (records e enums), sem lógica de acesso |
| `repository` | `PropertyRepository`, `JdbcPropertyRepository`, `PropertySqlBuilder`, `PropertyRowMapper`, `SqlQuery`, `RepositoryException` | SQL e conversão de linhas em objetos |
| `mcp.utils` | `McpToolDefinition`, `McpResourceDefinition`, `McpPromptDefinition`, `ToolArguments`, `ToolResults`, `JsonSchema` | contratos e utilitários comuns às ferramentas, recursos e prompts |
| `mcp.tools` | `PropertyTool` (base), `PropertySearchTool`, `PropertyDetailTool`, `LocationListTool`, `PriceStatsTool`, `SimpleReportTool` | ferramentas MCP |
| `mcp.resources` | `PropertyResource` (base), `LocationsResource`, `CityPricesResource` | recursos MCP (dados em JSON lidos por URI) |
| `mcp.prompts` | `IdealPropertyPrompt`, `ComparePropertiesPrompt`, `MarketAnalysisPrompt` | modelos de prompt |

Fluxo de uma chamada de ferramenta:

```
cliente MCP --JSON-RPC/STDIO--> PropertyTool.run()
                                  +- ToolArguments  (lê e valida os parâmetros)
                                  +- PropertyRepository.search()/findByCode()/...
                                  |     +- PropertySqlBuilder -> PreparedStatement -> PropertyRowMapper
                                  +- ToolResults    (resumo + JSON, ou isError = true)
```

Decisões de projeto:
- **Classe base por primitiva:** `PropertyTool` e `PropertyResource` concentram o registro no servidor e o tratamento de erros; cada subclasse define apenas sua descrição e sua lógica.
- **Inversão de dependência:** as ferramentas e os recursos recebem a interface `PropertyRepository` pelo construtor; nos testes unitários ela é substituída por um repositório em memória.
- **Sem SQL *injection*:** todo valor informado pelo usuário vai como parâmetro de `PreparedStatement`. A ordenação só aceita os valores do enum `SortOrder`, que mapeiam para trechos de SQL fixos.
- **Erros como resultado:** parâmetro inválido ou banco fora do ar não derrubam o servidor; a ferramenta retorna `isError = true` com uma mensagem em português, para que a IA possa corrigir a chamada ou avisar o usuário.
- **Inicialização sem banco:** a conexão só é aberta quando uma ferramenta é executada, então o servidor inicia e lista suas ferramentas mesmo com o banco desligado.
- **Somente leitura:** as ferramentas são anotadas como `readOnlyHint = true` (não alteram dados); os recursos, por definição, apenas fornecem dados.

---

### Banco de dados
Os scripts ficam em `src/main/resources/db/`:
- `01_schema.sql` — tabelas `"Property"`, `"Address"`, `"PropertyOwner"` e `"Condominium"` e os enums `"PropertyType"`, `"Purpose"`, `"AvailabilityStatus"` e `"UsageType"`, no padrão de nomes do Prisma (entre aspas, em *camelCase*). Quartos, banheiros, suítes e vagas ficam na coluna JSON `"extraInfo"`.
- `02_seed.sql` — 68 imóveis de exemplo em Curitiba, Londrina, Paranaguá, Maringá, Ponta Grossa, Foz do Iguaçu e Cascavel (PR). Quatro deles **não** devem aparecer nas buscas e servem para comprovar a filtragem: um rascunho (`DRAFT`), um vendido (`SOLD`), um excluído (`deletedAt` preenchido) e um oculto do site (`showOnWebsite = false`).

As buscas só retornam imóveis com `status = 'AVAILABLE'`, `"deletedAt" IS NULL` e `"showOnWebsite" = true`.

#### Subir o banco
Com o Docker Desktop em execução, na raiz do projeto:

```
docker compose up -d postgres
```

Na **primeira** criação do contêiner, o PostgreSQL executa automaticamente os dois scripts. Para conferir (no PowerShell e no Git Bash/Linux/macOS):

```
'select status, count(*) from "Property" group by status;' | docker compose exec -T postgres psql -U postgres -d imoveis
```

A consulta é enviada pela entrada padrão do `psql` porque o nome da tabela exige aspas duplas, que o PowerShell não repassa corretamente quando vêm escapadas com `\"` dentro do argumento `-c`. Resultado esperado: `AVAILABLE 66`, `DRAFT 1`, `SOLD 1`.

Também é possível abrir o `psql` de forma interativa e digitar as consultas:

```
docker compose exec postgres psql -U postgres -d imoveis
```

Para apagar o banco e recriá-lo do zero (por exemplo, após alterar os scripts):

```
docker compose down -v
docker compose up -d postgres
```

#### Variáveis de ambiente
O servidor lê a conexão destas variáveis; se ausentes, usa os valores padrão, que correspondem ao `compose.yaml`:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/imoveis` | URL JDBC do banco |
| `DB_USER` | `postgres` | usuário |
| `DB_PASSWORD` | `postgres` | senha (nunca é escrita nos *logs*) |

---

### Ferramentas (*tools*)
Todos os parâmetros são opcionais, exceto quando indicado. Os resultados de sucesso trazem uma linha de resumo seguida dos dados em JSON (preços em R$, áreas em m²).

#### `buscar_imoveis`
Busca imóveis disponíveis conforme os filtros.

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `texto` | texto | palavras no título, descrição, bairro ou características (ex.: `piscina`) |
| `tipo` | `HOUSE` (casa), `APARTMENT` (apartamento), `LAND` (terreno), `COMMERCIAL` (comercial), `FARM` (sítio/fazenda) | tipo do imóvel |
| `finalidade` | `SELL` (comprar), `RENT` (alugar) | imóveis anunciados para "venda e locação" aparecem nos dois casos; em `RENT`, o preço considerado é o do aluguel |
| `uso` | `RESIDENTIAL`, `COMMERCIAL`, `OTHER` | uso do imóvel |
| `cidade`, `bairro` | texto | nome ou parte do nome (sem diferenciar maiúsculas) |
| `estado` | texto | sigla da UF, ex.: `PR` |
| `precoMin`, `precoMax` | número ≥ 0 | faixa de preço (R$); para aluguel, valor mensal |
| `areaMin`, `areaMax` | número ≥ 0 | faixa de área (m²) |
| `quartosMin`, `vagasMin` | inteiro 0–20 | mínimo de quartos / vagas de garagem |
| `destaque` | booleano | `true` = somente imóveis em destaque |
| `ordenarPor` | `destaques` (padrão), `recentes`, `preco_asc`, `preco_desc`, `area_desc` | ordenação |
| `limite` | inteiro 1–50 | quantidade máxima de resultados (padrão 10) |

Retorno: quantidade encontrada + lista com id, código, título, tipo, finalidade, preços, áreas, quartos, vagas, bairro, cidade e UF. Sem resultados: mensagem sugerindo ampliar a busca (não é erro).

Exemplo — `{"tipo": "HOUSE", "finalidade": "SELL", "cidade": "Curitiba", "precoMax": 900000}`:
```
1 imóvel encontrado.

[ {
  "id" : "prop-001",
  "code" : "IMV-0001",
  "title" : "Casa com piscina no Batel",
  "type" : "HOUSE",
  "purpose" : "SELL",
  "minPrice" : 850000.00,
  ...
} ]
```

#### `detalhar_imovel`
Retorna todos os dados de um imóvel: descrição, preços, áreas, características (`extraInfo`), endereço, proprietário, condomínio, links e datas.

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `codigo` | texto | código do imóvel, ex.: `IMV-0001` (preferencial; não diferencia maiúsculas) |
| `id` | texto | identificador interno, retornado por `buscar_imoveis` |

É obrigatório informar um dos dois; se vierem ambos, vale o código. Imóvel inexistente ou indisponível -> erro.

#### `listar_localidades`
Lista as cidades com imóveis disponíveis ou, se `cidade` for informada, os bairros dessa cidade, com a quantidade de imóveis em cada um. Ajuda a IA a usar os nomes exatos antes de buscar.

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `estado` | texto | sigla da UF, ex.: `PR` |
| `cidade` | texto | se informada, lista os bairros |

#### `estatisticas_precos`
Calcula, por tipo de imóvel, a quantidade de imóveis disponíveis, os preços mínimo, médio e máximo e a área média. Responde perguntas como "quanto custa uma casa em Curitiba?".

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `cidade` | texto | nome ou parte do nome |
| `tipo` | mesmo de `buscar_imoveis` | tipo do imóvel |
| `finalidade` | `SELL`, `RENT` | recomendado, para não misturar preços de venda e de aluguel |

#### `obter_documento`
Ferramenta original do exemplo da disciplina, mantida para referência (`id` inteiro, obrigatório).

#### Erros
| Situação | Resultado |
|---|---|
| Parâmetro com valor inválido (ex.: `tipo = castelo`, `limite = 500`, `precoMin` maior que `precoMax`) | `isError = true` com a mensagem citando o parâmetro. Clientes que validam o esquema, como o próprio SDK, podem rejeitar a chamada antes dela chegar à ferramenta. |
| `detalhar_imovel` sem `codigo` nem `id`, ou imóvel não encontrado | `isError = true` |
| Banco de dados fora do ar | `isError = true`: *"Não foi possível consultar o banco de dados de imóveis..."* (detalhes técnicos só no *log* do servidor) |
| Nenhum imóvel encontrado | sucesso (`isError = false`) com mensagem orientando a ampliar a busca |

---

### Recursos (*resources*)
Recursos são dados que o cliente **lê** por meio de uma URI, sem executar uma ação: enquanto as ferramentas são chamadas pela IA, os recursos normalmente são escolhidos pelo usuário ou pelo aplicativo e anexados à conversa como contexto (no Claude Desktop, pelo botão **+** -> nome do servidor). O conteúdo é sempre JSON (`application/json`).

| URI | Tipo | Conteúdo |
|---|---|---|
| `imoveis://localidades` | recurso fixo | cidades (com UF) que têm imóveis disponíveis e a quantidade de imóveis em cada uma |
| `imoveis://precos/{cidade}` | modelo de URI (*resource template*) | preços mínimo, médio e máximo (R$) e área média (m²) dos imóveis disponíveis na cidade, por tipo, separados em venda (`sell`) e aluguel mensal (`rent`) |

No modelo, `{cidade}` é o nome da cidade (ou parte dele, até 100 caracteres), codificado para URL. Exemplos: `imoveis://precos/Curitiba`, `imoveis://precos/S%C3%A3o%20Jos%C3%A9%20dos%20Pinhais`.

Exemplo — `imoveis://precos/Curitiba`:
```
{
  "city" : "Curitiba",
  "sell" : [ { "type" : "HOUSE", "total" : ..., "minPrice" : ..., "avgPrice" : ..., "maxPrice" : ..., "avgArea" : ... } ],
  "rent" : [ ... ]
}
```

Diferente das ferramentas, recursos não têm um resultado "de erro": as falhas são devolvidas como erro JSON-RPC.

| Situação | Erro |
|---|---|
| URI fora do modelo, cidade vazia ou com mais de 100 caracteres | `-32602` (*Invalid params*) |
| Cidade sem imóveis disponíveis | `-32002` (*Resource not found*), sugerindo consultar `imoveis://localidades` |
| Banco de dados fora do ar | `-32603` (*Internal error*): *"Não foi possível consultar o banco de dados de imóveis..."* |

---

### Prompts
Prompts são modelos de mensagem que o cliente oferece ao usuário (no Claude Desktop, pelo botão **+** -> nome do servidor). O servidor preenche o modelo com os argumentos e o devolve; a IA então segue as instruções usando as ferramentas.

| Prompt | Argumentos | O que faz |
|---|---|---|
| `buscar_imovel_ideal` | `perfil` (opcional, até 500 caracteres): o que o usuário procura, em texto livre | orienta a IA a perguntar os critérios que faltam (cidade, finalidade, tipo, orçamento, quartos), confirmar nomes com `listar_localidades`, mostrar a faixa de preços com `estatisticas_precos`, buscar com `buscar_imoveis` (ampliando aos poucos se não houver resultado) e apresentar até 3 opções em uma tabela, sem inventar dados |
| `comparar_imoveis` | `codigos` (obrigatório): 2 a 5 códigos separados por vírgula, ex.: `IMV-0001, IMV-0004` | orienta a IA a chamar `detalhar_imovel` para cada código e montar uma tabela comparativa (preço, área, preço por m², quartos, vagas, condomínio...) com uma recomendação |
| `analisar_mercado` | `cidade` (obrigatório, até 100 caracteres); `finalidade` (opcional): `comprar` ou `alugar` | orienta a IA a confirmar a cidade com `listar_localidades`, listar os bairros com mais ofertas, obter a faixa de preços por tipo com `estatisticas_precos` (venda e aluguel separados, se a finalidade não for informada) e mostrar os imóveis mais baratos e mais caros com `buscar_imoveis`, terminando com um resumo do mercado |

Argumentos inválidos (ex.: apenas 1 código, ou código com caracteres como `'` e `;`) geram o erro JSON-RPC `-32602` (*Invalid params*) com a mensagem em português.

---

### Gerar e executar a aplicação
Este é um projeto único para duas aplicações: servidor e cliente. O servidor é executado a partir da classe `br.edu.ifpr.mcp_server.main.McpServer01SyncStdio`. Já o cliente é executado a partir da classe `br.edu.ifpr.mcp_server.main.McpClient01SyncStdio`.

Inicialmente, é preciso gerar a aplicação servidor e, em seguida, a aplicação cliente, tanto no Eclipse IDE quanto em linha de comando (CLI). Para que as ferramentas retornem dados, o banco deve estar no ar (ver [Subir o banco](#subir-o-banco)).

Além disto, é possível executar o servidor MCP Java em assistentes de IA locais, tais como Claude Desktop, Gemini CLI e Cursor CLI, entre outros, e testá-lo com o MCP Inspector.

#### Eclipse IDE
1. Clicar com o botão direito do mouse no nome do projeto;
2. Selecionar **Run As** > **Maven build...**
3. Na janela **Edit configuration and launch**:
    * Na aba **Main** digitar **clean package -Pserver -DskipTests** no campo **Goals**
    * Na aba **JRE** digitar **--enable-native-access=ALL-UNNAMED** no campo **VM arguments**
4. Clicar no botão **Run**

Os passos anteriores devem ser executados somente na primeira vez. A partir da segunda vez clicar na "pontinha de flecha" ao lado do botão *play*, na barra de botões do Eclipse IDE.

Se o processo de construção da aplicação servidor ocorreu sem erros, ele produzirá o diretório **server** na raiz do projeto. Este diretório contém a aplicação executável do servidor (`mcp_server01_server.jar`) e o subdiretório **libs** com as bibliotecas necessárias, incluindo o driver JDBC do PostgreSQL.

**A aplicação servidor é executada pela aplicação cliente e não precisa de intervenção do programador.**

O cliente também pode ser executado direto no Eclipse IDE, sem gerar o artefato cliente: com o servidor já gerado, clicar com o botão direito em `McpClient01SyncStdio` > **Run As** > **Java Application** e interagir pela aba **Console**.

Para gerar a aplicação cliente, executar os mesmos quatro passos, digitando na aba **Main** **clean package -Pclient -DskipTests**.

Se o processo de construção da aplicação cliente ocorreu sem erros, ele produzirá o diretório **dist-client** dentro de **target**, com a aplicação cliente e o subdiretório **libs**.

#### Linha de Comando (CLI)
Na raiz do projeto:

```
# gerar a aplicação servidor (diretório "server")
mvn clean package -Pserver -DskipTests
```
```
# gerar a aplicação cliente (diretório "target/dist-client")
mvn clean package -Pclient -DskipTests

# executar o cliente MCP Java, que executará o servidor MCP Java
cd target/dist-client
java -jar mcp_server01_client.jar
```

O cliente executa o servidor, lista as ferramentas disponíveis em um menu e, para a ferramenta escolhida, pede o valor de cada parâmetro (exibindo a descrição, que inclui os valores aceitos). Deixe em branco os parâmetros opcionais que não quiser usar. O cliente interativo executa ferramentas; os recursos e os prompts podem ser testados no MCP Inspector e no Claude Desktop.

Se o banco não estiver em `localhost:5432`, defina as variáveis `DB_*` no terminal antes de executar o cliente; o servidor as herda.

O cliente encontra o JAR do servidor (classe `ServerJarLocator`) procurando, nesta ordem:
1. o caminho da variável de ambiente `MCP_SERVER_JAR`, se definida;
2. `server/mcp_server01_server.jar` — cliente executado na raiz do projeto (Eclipse IDE);
3. `../../server/mcp_server01_server.jar` — cliente executado em `target/dist-client`.

Se não encontrar, encerra com uma mensagem indicando os caminhos procurados e como gerar o servidor.

---

**OBSERVAÇÃO IMPORTANTE**
> Sempre que o artefato SERVIDOR é gerado, o artefato CLIENTE é apagado do diretório "target", devido ao objetivo (*goal*) `clean`, e deve ser gerado novamente (mesmo que não tenha havido mudança de código nele).

---

#### MCP Inspector no Docker Desktop
O MCP Inspector, criado pela Anthropic, testa e depura servidores MCP por uma interface web. O `compose.yaml` sobe dois serviços: `postgres` (banco) e `mcp-inspector` (Inspector + Java 25, construído pelo `Dockerfile`), que só inicia depois que o banco estiver pronto.

1. Gerar o servidor (`mvn clean package -Pserver -DskipTests`) — o diretório `server` é montado dentro do contêiner
2. Com o Docker Desktop em execução, na raiz do projeto, executar `docker compose up -d --build`
3. Abrir `http://127.0.0.1:6274` no navegador
4. Conectar ao servidor e usar as abas **Tools** (executar cada ferramenta), **Resources** (ler `imoveis://localidades` e, em *Resource Templates*, `imoveis://precos/{cidade}`) e **Prompts** (gerar `buscar_imovel_ideal`, `comparar_imoveis` e `analisar_mercado`)

Dentro da rede do Docker o banco é acessado pelo nome do serviço, por isso o `compose.yaml` define `DB_URL=jdbc:postgresql://postgres:5432/imoveis` para o Inspector (e não `localhost`).

Para parar tudo: `docker compose down` (os dados do banco são mantidos no volume `postgres-data`).

---

### Instalar o servidor MCP Java Local no [Claude Desktop](https://claude.com/)
- Gerar o servidor e subir o banco (`docker compose up -d postgres`)
- Localizar o diretório `C:\Users\<USUÁRIO_LOGADO>\AppData\Local\Packages\Claude_pzs8sxrjxfjjc\LocalCache\Roaming\Claude`
- Editar o arquivo `claude_desktop_config.json`
- Se não existir, criar a entrada `mcpServers` com o conteúdo abaixo, ajustando o caminho do JAR

```json
  "mcpServers": {
    "imoveis": {
      "command": "java",
      "args": ["-jar", "C:/caminho/absoluto/para/mcp-server01/server/mcp_server01_server.jar"],
      "env": {
        "DB_URL": "jdbc:postgresql://localhost:5432/imoveis",
        "DB_USER": "postgres",
        "DB_PASSWORD": "postgres"
      }
    }
  }
```
- Salvar o arquivo e iniciar ou reiniciar o Claude Desktop (se o Claude Desktop estiver em execução deve ser completamente encerrado e não apenas sua janela fechada).
- Perguntar, por exemplo: *"Quero comprar uma casa em Curitiba com 3 quartos até 900 mil"*. Os prompts e os recursos aparecem no botão **+** da caixa de mensagem.

O diretório `Users` também pode ser escrito como `Usuários`.

O diretório `Claude_pzs8sxrjxfjjc` pode possuir um nome diferente do apresentado, mas o prefixo `Claude_` será o mesmo.

O Java (JDK ou JRE) 25 **deve** estar instalado e configurado. O Claude Desktop executa o `"command"` usando o `PATH` do Windows: se o `java` do `PATH` for de uma versão anterior, o servidor não inicia (`UnsupportedClassVersionError`, visível no arquivo `mcp-server-imoveis.log` da pasta `logs`, ao lado do `claude_desktop_config.json`). Nesse caso, informe o caminho completo do Java 25 em `"command"`, com barras `/`, ex.: `"C:/Program Files/Eclipse Adoptium/jdk-25.0.1.8-hotspot/bin/java.exe"`.

---

### Claude Code
O arquivo `.mcp.json`, na raiz do projeto, registra este servidor no [Claude Code](https://claude.com/claude-code) com o nome `imoveis`. O caminho do JAR é **relativo** (`server/mcp_server01_server.jar`), pois o Claude Code inicia o servidor na raiz do projeto; assim o arquivo funciona em qualquer máquina, sem ajustes.

1. Gerar o servidor e subir o banco (`docker compose up -d postgres`)
2. Abrir o Claude Code na raiz do projeto e aprovar o servidor `imoveis` quando solicitado
3. Conferir com `/mcp` (deve aparecer como *connected*) e perguntar, por exemplo: *"Quanto custa, em média, alugar um apartamento em Londrina?"*

Assim como no Claude Desktop, o `java` do `PATH` deve ser o Java 25.

---

### Testes
O diretório `src/test/java` contém os testes unitários (sufixo **Test**, executados pelo Surefire) e de integração (sufixo **IT**, executados pelo Failsafe).

**Unitários** — não precisam de banco nem de servidor:
- `PropertySqlBuilderTest`: SQL e parâmetros gerados para cada filtro, ordenação e consulta
- `PropertyRowMapperTest`, `JdbcPropertyRepositoryTest`: conversão de linhas e tratamento de falha de conexão
- `PropertyFilterTest`, `SortOrderTest`, `DatabaseConfigTest`: validações do domínio e da configuração
- `ToolArgumentsTest`, `ToolResultsTest`, `JsonSchemaTest`: utilitários MCP
- `PropertySearchToolTest`, `PropertyDetailToolTest`, `LocationListToolTest`, `PriceStatsToolTest`: cada ferramenta com um repositório falso (sucesso, resultado vazio, parâmetro inválido, banco fora do ar)
- `LocationsResourceTest`, `CityPricesResourceTest`: cada recurso com um repositório falso (definição, conteúdo JSON, banco fora do ar; no modelo de URI, também decodificação da cidade, URI inválida e cidade sem imóveis)
- `IdealPropertyPromptTest`, `ComparePropertiesPromptTest`, `MarketAnalysisPromptTest`, `ServerFeaturesTest`: prompts e montagem do servidor
- `ServerJarLocatorTest`: localização do JAR do servidor pelo cliente (raiz do projeto, `target/dist-client`, variável `MCP_SERVER_JAR`, JAR ausente)

```
mvn test
```

**Integração** — `McpServer01IT` executa o JAR do servidor como processo e conversa com ele por STDIO, como um cliente real:
- sem banco: lista de ferramentas, recursos, modelos de recurso e prompts, geração de prompts, validação de argumentos e de URIs
- com banco (somente se `DB_URL` estiver definida): buscas por tipo, cidade, finalidade, faixa de preço e quartos; imóveis em rascunho, excluídos e ocultos não aparecem; tentativa de SQL *injection* (`x' OR 1=1 --`) não retorna imóveis; detalhes com condomínio e proprietário; localidades; estatísticas de preço; leitura dos recursos `imoveis://localidades` e `imoveis://precos/{cidade}` (inclusive cidade inexistente)

```
# sem banco: os testes que dependem do banco são ignorados
mvn clean verify -Pserver
```
```
# com banco (Git Bash / Linux / macOS)
docker compose up -d postgres
DB_URL=jdbc:postgresql://localhost:5432/imoveis mvn clean verify -Pserver
```
```
:: com banco (Prompt de Comando do Windows)
docker compose up -d postgres
set DB_URL=jdbc:postgresql://localhost:5432/imoveis
mvn clean verify -Pserver
```

No Eclipse IDE, usar o *goal* `clean verify -Pserver` e, para os testes com banco, definir `DB_URL` na aba **Environment** da configuração.

---

