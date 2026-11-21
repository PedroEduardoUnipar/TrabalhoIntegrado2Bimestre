package br.unipar.loja.dto;

import jakarta.validation.constraints.*;

public record CadastroRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 8, max = 64) String senha
) {}
