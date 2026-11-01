package org.example.projetofinalfruki.application.dtos;

import java.time.LocalDate;

public record FeriadoDTO(
        LocalDate date,
        String name,
        String type,        // "NATIONAL", "STATE", "MUNICIPAL"
        String estado,      // "RS", "SC" ou "BR"
        String cidade       // Nome da cidade ou "Todas"
) {}