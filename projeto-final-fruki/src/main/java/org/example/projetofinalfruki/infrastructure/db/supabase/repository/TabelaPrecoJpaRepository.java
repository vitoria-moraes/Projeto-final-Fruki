package org.example.projetofinalfruki.infrastructure.db.supabase.repository;

import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TabelaPrecoJpaRepository extends JpaRepository<TabelaPrecoEntity, String> {
    Optional<TabelaPrecoEntity> findByCodProduto(String codProduto);
}