package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ClienteRequestDTO;
import org.example.projetofinalfruki.application.dtos.ClienteResponseDTO;
import org.example.projetofinalfruki.domain.exception.ConflitoException;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteJpaRepository repository;

    @InjectMocks
    private ClienteService service;

    @Test
    @DisplayName("cadastra cliente com CNPJ válido e normaliza os campos")
    void cadastraCliente() {
        ClienteRequestDTO dto = new ClienteRequestDTO(41L, "11.222.333/0001-81",
                " Mercado Central ", "Porto Alegre", "rs", null);
        when(repository.findByCnpj("11222333000181")).thenReturn(Optional.empty());
        when(repository.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponseDTO resposta = service.criar(dto);

        assertEquals(41L, resposta.codCliente());
        assertEquals("11222333000181", resposta.cnpj());
        assertEquals("Mercado Central", resposta.nomeFantasia());
        assertEquals("RS", resposta.uf());
    }

    @Test
    @DisplayName("rejeita CNPJ inválido")
    void rejeitaCnpjInvalido() {
        ClienteRequestDTO dto = new ClienteRequestDTO(42L, "123", "Loja", "Lajeado", "RS", null);
        assertThrows(IllegalArgumentException.class, () -> service.criar(dto));
    }

    @Test
    @DisplayName("409 quando o CNPJ já existe")
    void rejeitaDuplicado() {
        ClienteRequestDTO dto = new ClienteRequestDTO(41L, "11222333000181",
                "Mercado", "Porto Alegre", "RS", null);
        when(repository.findByCnpj("11222333000181")).thenReturn(Optional.of(new ClienteEntity()));

        assertThrows(ConflitoException.class, () -> service.criar(dto));
    }

    @Test
    @DisplayName("lista clientes para a tela de CNPJs")
    void listaClientes() {
        ClienteEntity entity = new ClienteEntity();
        entity.setCodCliente(2001L);
        entity.setNomeFantasia("Mercado Teste");
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        List<ClienteResponseDTO> lista = service.listar(200);

        assertEquals(1, lista.size());
        assertEquals(2001L, lista.get(0).codCliente());
    }
}
