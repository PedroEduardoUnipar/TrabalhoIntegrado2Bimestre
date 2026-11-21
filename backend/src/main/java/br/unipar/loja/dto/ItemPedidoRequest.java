package br.unipar.loja.dto;

import jakarta.validation.constraints.*;

public record ItemPedidoRequest(
    @NotNull @Positive Long produtoId,
    @NotNull @Min(1) @Max(9999) Integer quantidade
) {}
