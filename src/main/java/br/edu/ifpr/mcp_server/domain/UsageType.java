package br.edu.ifpr.mcp_server.domain;

/** Uso do imóvel: para morar ou para trabalhar (enum "UsageType"). */
public enum UsageType implements Labeled {
   RESIDENTIAL("residencial"),
   COMMERCIAL("comercial"),
   OTHER("outro");

   private final String label;

   UsageType(String label) {
      this.label = label;
   }

   @Override
   public String label() {
      return label;
   }
}
