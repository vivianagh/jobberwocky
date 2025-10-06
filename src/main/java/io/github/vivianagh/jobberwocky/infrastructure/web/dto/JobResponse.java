package io.github.vivianagh.jobberwocky.infrastructure.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Value
@Builder
public class JobResponse {
    Long id;
    String title;
    String description;
    String company;
    String country;
    String city;
    BigDecimal salary;
    Set<String> skills;
    String source;
    LocalDateTime createdAt;
}
