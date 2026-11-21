package br.unipar.loja.service;

import br.unipar.loja.dto.*;
import br.unipar.loja.model.Categoria;
import br.unipar.loja.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import static br.unipar.loja.exception.RegraNegocioException.*;

@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;
    public CategoriaService(CategoriaRepository categorias, ProdutoRepository produtos) {
        this.categorias = categorias;
        this.produtos = produtos;
    }
    public List<CategoriaResponse> listar() {
        return categorias.findAll(Sort.by("nome")).stream().map(DtoMapper::categoria).toList();
    }
    public CategoriaResponse buscar(Long id) { return DtoMapper.categoria(obterEntidade(id)); }
    public Categoria obterEntidade(Long id) {
        return categorias.findById(id).orElseThrow(() -> naoEncontrado("Categoria"));
    }
    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        if (categorias.existsByNomeNormalizado(normalizar(request.nome()))) {
            throw conflito("Já existe uma categoria com este nome.");
        }
        return DtoMapper.categoria(categorias.save(new Categoria(request.nome(), request.descricao())));
    }
    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obterEntidade(id);
        if (categorias.existsByNomeNormalizadoAndIdNot(normalizar(request.nome()), id)) {
            throw conflito("Já existe uma categoria com este nome.");
        }
        categoria.atualizar(request.nome(), request.descricao());
        return DtoMapper.categoria(categoria);
    }
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = obterEntidade(id);
        if (produtos.existsByCategoriaId(id)) {
            throw conflito("Remova ou mova os produtos desta categoria antes de excluí-la.");
        }
        categorias.delete(categoria);
    }
    private String normalizar(String nome) { return nome.strip().toLowerCase(Locale.ROOT); }
}
