package org.example.projetofinalfruki;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SUPABASE_JDBC_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "JWT_SECRET", matches = ".{32,}")
class ProjetoFinalFrukiApplicationTests {

    @Test
    void contextLoads() {
    }

}
