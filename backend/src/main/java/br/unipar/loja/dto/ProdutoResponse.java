package br.unipar.loja.dto;

import java.math.BigDecimal;

public record ProdutoResponse(Long id, String nome, String descricao, String cor,
    BigDecimal preco, Integer estoque, Boolean ativo, Long categoriaId, String categoriaNome) {}
