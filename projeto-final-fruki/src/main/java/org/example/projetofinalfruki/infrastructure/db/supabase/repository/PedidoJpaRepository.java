package org.example.projetofinalfruki.infrastructure.db.supabase.repository;

import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByCodCliente(Long codCliente);
    List<PedidoEntity> findByCodClienteOrderByDataVendaDesc(Long codCliente, Pageable pageable);
}
