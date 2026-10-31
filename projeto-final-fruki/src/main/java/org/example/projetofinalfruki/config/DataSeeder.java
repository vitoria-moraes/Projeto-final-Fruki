package org.example.projetofinalfruki.config;

import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.PedidoJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Popula pedidos de demonstração. Só roda com o perfil "dev" (--spring.profiles.active=dev) e NUNCA
 * derruba a aplicação: se a tabela fato_vendas não existir no banco, apenas avisa no log.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final PedidoJpaRepository pedidoRepository;

    public DataSeeder(PedidoJpaRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (pedidoRepository.count() > 0) return;

            PedidoEntity p1 = PedidoEntity.builder()
                    .codCliente("0001")
                    .dataPedido(LocalDateTime.now().minusDays(5))
                    .valorTotal(new BigDecimal("1250.00"))
                    .status("ENTREGUE")
                    .resumoItens("20x Guaraná Fruki 2L, 10x Água Mineral 500ml")
                    .build();
            pedidoRepository.saveAll(List.of(p1));
            System.out.println("[DataSeeder] Histórico de teste criado.");
        } catch (Exception e) {
            System.err.println("[DataSeeder] Ignorado (a tabela fato_vendas existe? " + e.getClass().getSimpleName() + ")");
        }
    }
}