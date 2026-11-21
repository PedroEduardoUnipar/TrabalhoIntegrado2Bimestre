package br.unipar.loja.repository;

import br.unipar.loja.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeNormalizado(String nome);
    boolean existsByNomeNormalizadoAndIdNot(String nome, Long id);
}
