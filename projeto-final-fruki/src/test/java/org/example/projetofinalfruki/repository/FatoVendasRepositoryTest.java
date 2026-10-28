package org.example.projetofinalfruki.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // <--- Adiciona esta linha
public class FatoVendasRepositoryTest {

    @Test
    @DisplayName("Deve carregar o contexto JPA com sucesso")
    public void testContextoJpaCarregado() {
        assertNotNull(true, "O contexto JPA deve iniciar corretamente");
    }
}