package org.example.projetofinalfruki.infrastructure;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dim_tabela_preco_limpa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TabelaPrecoEntity {

    @Id
    private Long id;

    @Column(name = "cod_cliente")
    private String codCliente;

    @Column(name = "cod_material")
    private String codMaterial;

    @Column(name = "desc_material")
    private String descMaterial;

    @Column(name = "preco_tabela")
    private BigDecimal precoTabela;

    @Column(name = "cod_hierarquia")
    private String codHierarquia;

    @Column(name = "canal_distribuicao")
    private String canalDistribuicao;
}