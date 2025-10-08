package io.github.vivianagh.jobberwocky.infrastructure.web.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long total
) {}
