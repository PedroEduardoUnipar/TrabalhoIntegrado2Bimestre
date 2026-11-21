package br.unipar.loja.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequest(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 500) String descricao,
    @NotBlank @Size(max = 40) String cor,
    @NotNull @DecimalMin("0.01") @Digits(integer = 7, fraction = 2) BigDecimal preco,
    @NotNull @Min(0) @Max(1000000) Integer estoque,
    @NotNull Boolean ativo,
    @NotNull @Positive Long categoriaId
) {}
