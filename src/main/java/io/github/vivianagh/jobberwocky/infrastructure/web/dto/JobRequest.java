package io.github.vivianagh.jobberwocky.infrastructure.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class JobRequest {
    @NotBlank @Size(min=3, max=200) private String title;
    @Size(max=2000) private String description;
    @NotBlank private String company;
    @NotBlank private String country;
    private String city;
    @NotNull @DecimalMin(value="0.0", inclusive=false) private BigDecimal salary;
    @NotEmpty private Set<@NotBlank String> skills;
}
