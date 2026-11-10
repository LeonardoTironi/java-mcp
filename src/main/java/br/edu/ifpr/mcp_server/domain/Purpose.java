package br.edu.ifpr.mcp_server.domain;

/** Finalidade do anúncio (espelha o enum "Purpose" do banco). */
public enum Purpose implements Labeled {
   SELL("venda"),
   RENT("locação"),
   BOTH("venda e locação");

   private final String label;

   Purpose(String label) {
      this.label = label;
   }

   @Override
   public String label() {
      return label;
   }
}
