package org.example.projetofinalfruki.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garante que o contexto JPA sobe sem depender do Supabase.
 * Com H2 + Replace.ANY, o teste é hermético: não precisa de variáveis de ambiente.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class FatoVendasRepositoryTest {

    @Test
    @DisplayName("Deve carregar o contexto JPA com o banco em memória")
    public void testContextoJpaCarregado() {
        assertTrue(true, "O contexto JPA deve iniciar corretamente com H2");
    }
}
