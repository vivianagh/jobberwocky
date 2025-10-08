package io.github.vivianagh.jobberwocky.infrastructure.web.dto;


import org.springframework.lang.Nullable;

public record SearchParams(
        @Nullable String title,
        @Nullable String country,
        @Nullable String minSalary,
        @Nullable String maxSalary,
        @Nullable String skill,
        Integer page,   // 0-based
        Integer size,   // default 20
        String sort     // e.g. "salary,desc"
) {}