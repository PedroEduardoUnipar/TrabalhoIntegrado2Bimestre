package br.unipar.loja.dto;

import java.time.Instant;

public record LoginResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) {}
