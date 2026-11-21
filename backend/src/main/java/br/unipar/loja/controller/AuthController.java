package br.unipar.loja.controller;

import br.unipar.loja.dto.*;
import br.unipar.loja.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/cadastro")
    @Operation(summary = "Criar uma conta com perfil COMUM")
    @SecurityRequirements
    public ResponseEntity<UsuarioResponse> cadastro(@Valid @RequestBody CadastroRequest request) {
        return ResponseEntity.created(URI.create("/api/auth/me")).body(service.cadastrar(request));
    }
    @PostMapping("/login")
    @Operation(summary = "Autenticar e receber um token JWT")
    @SecurityRequirements
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }

    @GetMapping("/me")
    @Operation(summary = "Consultar o usuário autenticado")
    public UsuarioResponse atual(@AuthenticationPrincipal Jwt jwt) { return service.atual(jwt); }
}
