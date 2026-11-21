package br.unipar.loja.controller;

import br.unipar.loja.dto.*;
import br.unipar.loja.service.*;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorias")
@Validated
public class CategoriaController {
    private final CategoriaService service;
    private final ProdutoService produtos;
    public CategoriaController(CategoriaService service, ProdutoService produtos) { this.service = service; this.produtos = produtos; }

    @GetMapping
    public List<CategoriaResponse> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable @Positive Long id) { return service.buscar(id); }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = service.criar(request);
        return ResponseEntity.created(URI.create("/api/categorias/" + response.id())).body(response);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponse atualizar(@PathVariable @Positive Long id, @Valid @RequestBody CategoriaRequest request) { return service.atualizar(id, request); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) { service.excluir(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/{id}/produtos")
    @Operation(summary = "Filtro no path: listar produtos de uma categoria")
    public List<ProdutoResponse> produtos(@PathVariable @Positive Long id,
            @RequestParam(required = false) @Size(max = 100) String nome,
            @RequestParam(required = false) @Size(max = 40) String cor,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal precoMin,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal precoMax,
            @RequestParam(required = false) Boolean ativo) {
        return produtos.listar(nome, cor, id, precoMin, precoMax, ativo);
    }
}
