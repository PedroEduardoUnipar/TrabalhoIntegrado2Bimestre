package br.unipar.loja.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record PedidoRequest(@NotEmpty @Size(max = 50) List<@NotNull @Valid ItemPedidoRequest> itens) {}
