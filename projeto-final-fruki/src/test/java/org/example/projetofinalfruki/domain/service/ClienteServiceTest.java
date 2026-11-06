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
        ClienteRequestDTO dto = new ClienteRequestDTO("41", "11.222.333/0001-81",
                " Mercado Central ", "Porto Alegre", "rs", null);
        when(repository.findByCnpj("11222333000181")).thenReturn(Optional.empty());
        when(repository.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponseDTO resposta = service.criar(dto);

        assertEquals("41", resposta.codCliente());
        assertEquals("11222333000181", resposta.cnpj());
        assertEquals("Mercado Central", resposta.nomeFantasia());
        assertEquals("RS", resposta.uf());
    }

    @Test
    @DisplayName("rejeita CNPJ inválido")
    void rejeitaCnpjInvalido() {
        ClienteRequestDTO dto = new ClienteRequestDTO("42", "123", "Loja", "Lajeado", "RS", null);
        assertThrows(IllegalArgumentException.class, () -> service.criar(dto));
    }

    @Test
    @DisplayName("409 quando o CNPJ já existe")
    void rejeitaDuplicado() {
        ClienteRequestDTO dto = new ClienteRequestDTO("41", "11222333000181",
                "Mercado", "Porto Alegre", "RS", null);
        when(repository.findByCnpj("11222333000181")).thenReturn(Optional.of(new ClienteEntity()));

        assertThrows(ConflitoException.class, () -> service.criar(dto));
    }
}
