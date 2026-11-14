package br.edu.ifpr.mcp_server.repository;

import java.util.List;
import java.util.Optional;

import br.edu.ifpr.mcp_server.domain.LocationCount;
import br.edu.ifpr.mcp_server.domain.PriceStats;
import br.edu.ifpr.mcp_server.domain.PropertyDetail;
import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertySummary;

/**
 * Consultas de imóveis. As ferramentas MCP dependem desta abstração, e não da
 * implementação JDBC, o que permite testá-las com uma implementação falsa.
 * <p>
 * Todos os métodos lançam {@link RepositoryException} em falhas de acesso.
 */
public interface PropertyRepository {

   /** Imóveis disponíveis que atendem ao filtro. */
   List<PropertySummary> search(PropertyFilter filter);

   /** Imóvel visível com o id informado. */
   Optional<PropertyDetail> findById(String id);

   /** Imóvel visível com o código informado (ignora maiúsculas). */
   Optional<PropertyDetail> findByCode(String code);

   /**
    * Quantidade de imóveis disponíveis por cidade ou, se {@code city} for
    * informada, por bairro dessa cidade. Parâmetros nulos não filtram.
    */
   List<LocationCount> listLocations(String state, String city);

   /** Estatísticas de preço e área por tipo, conforme o filtro. */
   List<PriceStats> priceStats(PropertyFilter filter);
}
