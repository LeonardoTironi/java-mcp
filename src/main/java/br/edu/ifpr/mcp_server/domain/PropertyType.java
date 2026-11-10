package br.edu.ifpr.mcp_server.domain;

/** Tipo do imóvel (espelha o enum "PropertyType" do banco). */
public enum PropertyType implements Labeled {
   HOUSE("casa"),
   APARTMENT("apartamento"),
   LAND("terreno"),
   COMMERCIAL("imóvel comercial"),
   FARM("sítio/fazenda");

   private final String label;

   PropertyType(String label) {
      this.label = label;
   }

   @Override
   public String label() {
      return label;
   }
}
