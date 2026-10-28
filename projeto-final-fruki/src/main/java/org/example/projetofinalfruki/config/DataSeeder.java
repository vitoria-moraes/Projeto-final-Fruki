package org.example.projetofinalfruki.config;

import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.PedidoJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PedidoJpaRepository pedidoRepository;

    public DataSeeder(PedidoJpaRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (pedidoRepository.count() == 0) {
            System.out.println("🌱 [DataSeeder] Populando histórico fictício de pedidos para demonstração...");

            PedidoEntity p1 = PedidoEntity.builder()
                    .codCliente("0001")
                    .dataPedido(LocalDateTime.now().minusDays(5))
                    .valorTotal(new BigDecimal("1250.00"))
                    .status("ENTREGUE")
                    .resumoItens("20x Guaraná Fruki 2L, 10x Água Mineral 500ml")
                    .build();

            pedidoRepository.saveAll(List.of(p1));
            System.out.println("✅ [DataSeeder] Histórico de testes criado com sucesso!");
        }
    }
}