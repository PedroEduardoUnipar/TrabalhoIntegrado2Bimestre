package br.unipar.loja.dto;

import jakarta.validation.constraints.*;

public record CategoriaRequest(
    @NotBlank @Size(max = 80) String nome,
    @Size(max = 250) String descricao
) {}
