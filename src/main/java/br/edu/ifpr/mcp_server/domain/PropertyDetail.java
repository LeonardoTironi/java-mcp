package br.edu.ifpr.mcp_server.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dados completos de um imóvel. */
public record PropertyDetail(String id,
                             String code,
                             String title,
                             String description,
                             PropertyType type,
                             Purpose purpose,
                             AvailabilityStatus status,
                             UsageType usageType,
                             boolean featured,
                             BigDecimal minPrice,
                             BigDecimal maxPrice,
                             BigDecimal minArea,
                             BigDecimal maxArea,
                             Map<String, Object> extraInfo,
                             String websiteLink,
                             String videoUrl,
                             List<String> publishedOn,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt,
                             Address address,
                             String ownerName,
                             String condominiumName) {

   public PropertyDetail {
      // JSON pode conter valores nulos, que Map.copyOf() não aceita
      extraInfo   = extraInfo == null
                       ? Map.of()
                       : Collections.unmodifiableMap(
                                                 new LinkedHashMap<>(extraInfo));
      publishedOn = publishedOn == null ? List.of() : List.copyOf(publishedOn);
   }
}
