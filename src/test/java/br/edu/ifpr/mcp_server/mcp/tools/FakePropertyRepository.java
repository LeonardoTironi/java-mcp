package br.edu.ifpr.mcp_server.mcp.tools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifpr.mcp_server.domain.Address;
import br.edu.ifpr.mcp_server.domain.AvailabilityStatus;
import br.edu.ifpr.mcp_server.domain.LocationCount;
import br.edu.ifpr.mcp_server.domain.PriceStats;
import br.edu.ifpr.mcp_server.domain.PropertyDetail;
import br.edu.ifpr.mcp_server.domain.PropertyFilter;
import br.edu.ifpr.mcp_server.domain.PropertySummary;
import br.edu.ifpr.mcp_server.domain.PropertyType;
import br.edu.ifpr.mcp_server.domain.Purpose;
import br.edu.ifpr.mcp_server.domain.UsageType;
import br.edu.ifpr.mcp_server.repository.PropertyRepository;
import br.edu.ifpr.mcp_server.repository.RepositoryException;

/**
 * Repositório em memória para os testes das ferramentas e recursos: devolve os
 * dados configurados, registra os parâmetros recebidos e, se {@link #offline},
 * simula o banco fora do ar.
 */
public class FakePropertyRepository implements PropertyRepository {
   // dados de exemplo: um único imóvel, a casa IMV-0001 em Curitiba
   public static final PropertySummary SUMMARY =
         new PropertySummary("prop-001","IMV-0001","Casa no Batel",
                             PropertyType.HOUSE,Purpose.SELL,
                             UsageType.RESIDENTIAL,new BigDecimal("850000.00"),
                             new BigDecimal("850000.00"),
                             new BigDecimal("180.00"),new BigDecimal("180.00"),
                             3,2,true,"Batel","Curitiba","PR");

   public static final PropertyDetail DETAIL =
         new PropertyDetail("prop-001","IMV-0001","Casa no Batel",
                            "Casa ampla com piscina",PropertyType.HOUSE,
                            Purpose.SELL,AvailabilityStatus.AVAILABLE,
                            UsageType.RESIDENTIAL,true,
                            new BigDecimal("850000.00"),
                            new BigDecimal("850000.00"),
                            new BigDecimal("180.00"),new BigDecimal("180.00"),
                            Map.of("bedrooms",3),null,null,List.of(),null,null,
                            new Address("Rua A","10",null,"Batel","Curitiba",
                                        "PR","80000-000",null,null),
                            "Maria",null);

   // o que cada consulta devolve; os campos são públicos para que cada
   // teste troque o retorno (por exemplo, por uma lista vazia)
   public List<PropertySummary> summaries = List.of(SUMMARY);
   public List<LocationCount>   locations = List.of(
                         new LocationCount("PR","Curitiba",null,5));
   public List<PriceStats>      stats     = List.of(
         new PriceStats(PropertyType.HOUSE,2,new BigDecimal("500000.00"),
                        new BigDecimal("675000.00"),new BigDecimal("850000.00"),
                        new BigDecimal("150.00")));
   // verdadeiro: toda consulta falha como se o banco estivesse fora do ar
   public boolean               offline;

   // parâmetros de cada consulta recebida, na ordem: o filtro, "id:...",
   // "code:..." ou "estado/cidade"; vazio indica que nada foi consultado
   public final List<Object> calls = new ArrayList<>();

   @Override
   public List<PropertySummary> search(PropertyFilter filter) {
      record(filter);
      return summaries;
   }

   @Override
   public Optional<PropertyDetail> findById(String id) {
      record("id:" + id);
      // só encontra o imóvel de exemplo; qualquer outro id fica vazio
      return Optional.of(DETAIL).filter(d -> d.id().equals(id));
   }

   @Override
   public Optional<PropertyDetail> findByCode(String code) {
      record("code:" + code);
      // como o banco, compara o código ignorando maiúsculas/minúsculas
      return Optional.of(DETAIL).filter(d -> d.code().equalsIgnoreCase(code));
   }

   @Override
   public List<LocationCount> listLocations(String state, String city) {
      record(state + "/" + city);
      return locations;
   }

   @Override
   public List<PriceStats> priceStats(PropertyFilter filter) {
      record(filter);
      return stats;
   }

   private void record(Object call) {
      // a falha é lançada como o repositório JDBC faria: RepositoryException
      // envolvendo a SQLException original
      if (offline)
         throw new RepositoryException("falha simulada",
                                       new SQLException("banco fora do ar"));
      calls.add(call);
   }
}
