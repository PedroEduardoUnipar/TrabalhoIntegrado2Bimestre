package br.unipar.loja.service;

import br.unipar.loja.dto.*;
import br.unipar.loja.model.Produto;
import br.unipar.loja.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import static br.unipar.loja.exception.RegraNegocioException.*;

@Service
@Transactional(readOnly = true)
public class ProdutoService {
    private final ProdutoRepository produtos;
    private final CategoriaService categorias;
    private final ItemPedidoRepository itens;
    public ProdutoService(ProdutoRepository produtos, CategoriaService categorias, ItemPedidoRepository itens) {
        this.produtos = produtos;
        this.categorias = categorias;
        this.itens = itens;
    }
    public List<ProdutoResponse> listar(String nome, String cor, Long categoriaId,
                                        BigDecimal precoMin, BigDecimal precoMax, Boolean ativo) {
        if (precoMin != null && precoMax != null && precoMin.compareTo(precoMax) > 0) {
            throw invalido("O preço mínimo deve ser menor ou igual ao preço máximo.");
        }
        if (categoriaId != null) categorias.obterEntidade(categoriaId);
        return produtos.findAll((root, query, cb) -> {
            root.fetch("categoria");
            List<Predicate> filtros = new ArrayList<>();
            if (nome != null && !nome.isBlank()) filtros.add(cb.like(cb.lower(root.get("nome")), contem(nome), '\\'));
            if (cor != null && !cor.isBlank()) filtros.add(cb.like(cb.lower(root.get("cor")), contem(cor), '\\'));
            if (categoriaId != null) filtros.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            if (precoMin != null) filtros.add(cb.greaterThanOrEqualTo(root.get("preco"), precoMin));
            if (precoMax != null) filtros.add(cb.lessThanOrEqualTo(root.get("preco"), precoMax));
            if (ativo != null) filtros.add(cb.equal(root.get("ativo"), ativo));
            return cb.and(filtros.toArray(Predicate[]::new));
        }, Sort.by("nome").ascending().and(Sort.by("id"))).stream().map(DtoMapper::produto).toList();
    }
    public ProdutoResponse buscar(Long id) {
        return DtoMapper.produto(produtos.findById(id).orElseThrow(() -> naoEncontrado("Produto")));
    }
    @Transactional
    public ProdutoResponse criar(ProdutoRequest r) {
        var categoria = categorias.obterEntidade(r.categoriaId());
        return DtoMapper.produto(produtos.save(new Produto(r.nome(), r.descricao(), r.cor(),
            r.preco(), r.estoque(), r.ativo(), categoria)));
    }
    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest r) {
        // O mesmo bloqueio usado na compra evita sobrescrever uma baixa de estoque concorrente.
        Produto produto = produtos.buscarParaAtualizacao(id).orElseThrow(() -> naoEncontrado("Produto"));
        produto.atualizar(r.nome(), r.descricao(), r.cor(), r.preco(), r.estoque(), r.ativo(), categorias.obterEntidade(r.categoriaId()));
        return DtoMapper.produto(produto);
    }
    @Transactional
    public void excluir(Long id) {
        Produto produto = produtos.buscarParaAtualizacao(id).orElseThrow(() -> naoEncontrado("Produto"));
        if (itens.existsByProdutoId(id)) {
            throw conflito("Este produto possui histórico de pedidos. Desative-o para retirá-lo do catálogo.");
        }
        produtos.delete(produto);
    }
    private String contem(String valor) {
        return "%" + valor.strip().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
