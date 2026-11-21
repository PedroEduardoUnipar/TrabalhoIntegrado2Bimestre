package br.unipar.loja.dto;

import br.unipar.loja.model.Perfil;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil) {}
