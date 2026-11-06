package org.example.projetofinalfruki.infrastructure.db.supabase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "fato_vendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_cliente")
    private Long codCliente;

    @Column(name = "data_venda")
    private LocalDate dataVenda;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Transient
    private String status;

    @Transient
    private String resumoItens;

    @Transient
    public LocalDateTime getDataPedido() {
        return dataVenda == null ? null : dataVenda.atStartOfDay();
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataVenda = dataPedido == null ? null : dataPedido.toLocalDate();
    }
}
