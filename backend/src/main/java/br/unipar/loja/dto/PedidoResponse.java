package br.unipar.loja.dto;

import br.unipar.loja.model.StatusPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(Long id, Instant criadoEm, StatusPedido status,
    BigDecimal total, UsuarioResponse cliente, List<ItemPedidoResponse> itens) {}
