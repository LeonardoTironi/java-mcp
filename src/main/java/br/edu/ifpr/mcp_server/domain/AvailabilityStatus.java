package br.edu.ifpr.mcp_server.domain;

/** Situação do anúncio (enum "AvailabilityStatus"). */
public enum AvailabilityStatus implements Labeled {
   DRAFT("rascunho"),
   AVAILABLE("disponível"),
   RESERVED("reservado"),
   SOLD("vendido"),
   RENTED("alugado"),
   INACTIVE("inativo");

   private final String label;

   AvailabilityStatus(String label) {
      this.label = label;
   }

   @Override
   public String label() {
      return label;
   }
}
