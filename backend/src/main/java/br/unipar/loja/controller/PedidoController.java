package br.unipar.loja.controller;

import br.unipar.loja.dto.*;
import br.unipar.loja.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos")
@Validated
public class PedidoController {
    private final PedidoService service;
    private final AuthService auth;
    public PedidoController(PedidoService service, AuthService auth) { this.service = service; this.auth = auth; }

    @GetMapping
    @Operation(summary = "Listar os próprios pedidos (ADMIN pode consultar todos)")
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt jwt) { return service.listar(auth.usuarioId(jwt)); }
    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable @Positive Long id, @AuthenticationPrincipal Jwt jwt) { return service.buscar(id, auth.usuarioId(jwt)); }
    @PostMapping
    @Operation(summary = "Criar pedido, calcular o total e baixar o estoque")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request, @AuthenticationPrincipal Jwt jwt) {
        PedidoResponse response = service.criar(request, auth.usuarioId(jwt));
        return ResponseEntity.created(URI.create("/api/pedidos/" + response.id())).body(response);
    }
    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar um pedido e devolver os itens ao estoque")
    public PedidoResponse cancelar(@PathVariable @Positive Long id, @AuthenticationPrincipal Jwt jwt) { return service.cancelar(id, auth.usuarioId(jwt)); }
}
