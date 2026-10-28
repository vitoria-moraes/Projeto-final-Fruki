package org.example.projetofinalfruki.infrastructure.db.supabase.repository;

import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, String> {
    Optional<ClienteEntity> findByCnpj(String cnpj);
}