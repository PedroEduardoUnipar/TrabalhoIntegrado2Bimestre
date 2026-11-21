package br.unipar.loja.dto;

import java.math.BigDecimal;

public record ItemPedidoResponse(Long id, Long produtoId, String nomeProduto,
    Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {}
