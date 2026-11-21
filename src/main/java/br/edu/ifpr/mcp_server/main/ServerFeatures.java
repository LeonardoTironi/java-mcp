package br.edu.ifpr.mcp_server.main;

import java.util.List;
import java.util.Objects;

import br.edu.ifpr.mcp_server.config.DatabaseConfig;
import br.edu.ifpr.mcp_server.db.DriverManagerConnectionFactory;
import br.edu.ifpr.mcp_server.mcp.prompts.ComparePropertiesPrompt;
import br.edu.ifpr.mcp_server.mcp.prompts.IdealPropertyPrompt;
import br.edu.ifpr.mcp_server.mcp.prompts.MarketAnalysisPrompt;
import br.edu.ifpr.mcp_server.mcp.resources.CityPricesResource;
import br.edu.ifpr.mcp_server.mcp.resources.LocationsResource;
import br.edu.ifpr.mcp_server.mcp.tools.LocationListTool;
import br.edu.ifpr.mcp_server.mcp.tools.PriceStatsTool;
import br.edu.ifpr.mcp_server.mcp.tools.PropertyDetailTool;
import br.edu.ifpr.mcp_server.mcp.tools.PropertySearchTool;
import br.edu.ifpr.mcp_server.mcp.tools.SimpleReportTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpPromptDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.McpResourceDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.mcp.utils.ToolResults;
import br.edu.ifpr.mcp_server.repository.JdbcPropertyRepository;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import br.edu.ifpr.mcp_server.repository.PropertyRowMapper;
import br.edu.ifpr.mcp_server.repository.PropertySqlBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Monta as ferramentas, os prompts e os recursos oferecidos pelo servidor,
 * ligando cada
 * um às suas dependências (ponto único de composição da aplicação).
 * <p>
 * Nenhuma conexão é aberta aqui: o banco só é acessado quando uma ferramenta
 * é executada, então o servidor inicia (e lista suas ferramentas) mesmo com
 * o banco fora do ar.
 */
public final class ServerFeatures {
   private final PropertyRepository repository;
   private final ToolResults        results;
   private final JsonMapper         json = new JsonMapper();

   public ServerFeatures(PropertyRepository repository, ToolResults results) {
      this.repository = Objects.requireNonNull(repository,"repository");
      this.results    = Objects.requireNonNull(results,"results");
   }

   /** Componentes reais, com o banco configurado pelas variáveis DB_*. */
   public static ServerFeatures fromEnvironment() {
      JsonMapper     json   = new JsonMapper();
      DatabaseConfig config = DatabaseConfig.fromEnvironment();

      return new ServerFeatures(
            new JdbcPropertyRepository(
                  new DriverManagerConnectionFactory(config),
                  new PropertySqlBuilder(),new PropertyRowMapper(json)),
            new ToolResults(json));
   }

   public List<McpToolDefinition> tools() {
      return List.of(new PropertySearchTool(repository,results),
                     new PropertyDetailTool(repository,results),
                     new LocationListTool(repository,results),
                     new PriceStatsTool(repository,results),
                     // ferramenta original do exemplo da disciplina
                     new SimpleReportTool());
   }

   public List<McpPromptDefinition> prompts() {
      return List.of(new IdealPropertyPrompt(),new ComparePropertiesPrompt(),
                     new MarketAnalysisPrompt());
   }

   public List<McpResourceDefinition> resources() {
      return List.of(new LocationsResource(repository,json),
                     new CityPricesResource(repository,json));
   }
}
