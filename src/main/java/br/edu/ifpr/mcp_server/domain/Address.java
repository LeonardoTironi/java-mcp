package br.edu.ifpr.mcp_server.domain;

/** Endereço de um imóvel. */
public record Address(String street,
                      String number,
                      String extraInfo,
                      String neighborhood,
                      String city,
                      String state,
                      String postalCode,
                      Double latitude,
                      Double longitude) {
}
