package br.unipar.loja.controller;

import br.unipar.loja.dto.*;
import br.unipar.loja.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos")
@Validated
public class ProdutoController {
    private final ProdutoService service;
    public ProdutoController(ProdutoService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Listar produtos combinando nome, cor, categoria, faixa de preço e situação")
    public List<ProdutoResponse> listar(
            @RequestParam(required = false) @Size(max = 100) String nome,
            @RequestParam(required = false) @Size(max = 40) String cor,
            @RequestParam(required = false) @Positive Long categoriaId,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal precoMin,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal precoMax,
            @RequestParam(required = false) Boolean ativo) {
        return service.listar(nome, cor, categoriaId, precoMin, precoMax, ativo);
    }
    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable @Positive Long id) { return service.buscar(id); }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse response = service.criar(request);
        return ResponseEntity.created(URI.create("/api/produtos/" + response.id())).body(response);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProdutoResponse atualizar(@PathVariable @Positive Long id, @Valid @RequestBody ProdutoRequest request) { return service.atualizar(id, request); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
}
