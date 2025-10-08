package io.github.vivianagh.jobberwocky.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record JobResponse(
        Long id,
        String title,
        String description,
        String company,
        String country,
        String city,
        BigDecimal salary,
        Set<String> skills,
        String source,
        LocalDateTime createdAt
) {}
