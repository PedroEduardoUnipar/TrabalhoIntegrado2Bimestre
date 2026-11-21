package br.unipar.loja.repository;

import br.unipar.loja.model.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByIdDesc();
    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarParaAtualizacao(@Param("id") Long id);
}
