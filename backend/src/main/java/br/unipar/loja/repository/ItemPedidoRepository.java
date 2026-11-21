package br.unipar.loja.repository;

import br.unipar.loja.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    boolean existsByProdutoId(Long produtoId);
}
