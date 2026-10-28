package org.example.projetofinalfruki.infrastructure;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dim_cliente_limpa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEntity {

    @Id
    @Column(name = "cod_cliente")
    private String codCliente;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "uf")
    private String uf;

    @Column(name = "cod_hierarquia")
    private String codHierarquia;
}