package io.github.vivianagh.jobberwocky.infrastructure.source;

import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "sources.internal.enabled=true",
        "sources.external.enabled=true",
        "sources.prefer-internal=true" // si tu composite usa esta bandera
})
public class WiringCompositeIntegrationTest {

    @TestConfiguration
    static class StubExternal {
        @Bean
        @Primary // asegura que este bean reemplace al real en el contexto
        ExternalJobSource externalJobSource() {
            return Mockito.mock(ExternalJobSource.class);
        }
    }

    // Context beans reales
    private final ApplicationContext ctx;
    private final JobService jobService;
    private final JobRepository jobRepository;
    // Nuestro mock (inyectado desde la @TestConfiguration)
    private final ExternalJobSource externalJobSource;

    WiringCompositeIntegrationTest(
            ApplicationContext ctx,
            JobService jobService,
            JobRepository jobRepository,
            ExternalJobSource externalJobSource
    ) {
        this.ctx = ctx;
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.externalJobSource = externalJobSource;
    }

    @BeforeEach
    void clean() {
        jobRepository.deleteAll();
        reset(externalJobSource);
    }

    @Test
    void contextHasCompositeAndItAggregatesInternalPlusExternal() {
        // --- Arrange ---
        // 1) Persistimos un job interno
        Job internal = Job.builder()
                .title("Backend Eng")
                .description("APIs")
                .company("Acme")
                .country("USA")
                .city("NYC")
                .salary(new BigDecimal("120000"))
                .skills(Set.of("Java"))
                .source("INTERNAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        jobRepository.save(internal);

        // 2) Stub de la fuente externa mockeada
        Job external = Job.builder()
                .title("Data Eng")
                .company("Globex")
                .country("Spain")
                .salary(new BigDecimal("90000"))
                .skills(Set.of("Python"))
                .source("EXTERNAL_API")
                .externalId("EXT_ES_90K")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(externalJobSource.searchJobs(any(JobSearchCriteria.class)))
                .thenReturn(List.of(external));

        // --- Act ---
        var out = jobService.search(JobSearchCriteria.builder().build());

        // --- Assert ---
        // A) El contexto tiene un JobSource, y debe ser el Composite (por @Primary)
        JobSource jobSourceBean = ctx.getBean(JobSource.class);
        assertThat(jobSourceBean.getClass().getSimpleName())
                .isEqualTo("CompositeJobSource");

        // B) Agrega resultados de interno + externo
        assertThat(out).hasSize(2);
        assertThat(out).extracting(Job::getTitle)
                .containsExactlyInAnyOrder("Backend Eng", "Data Eng");

        // C) Verifica que se llamó a la externa
        verify(externalJobSource, times(1)).searchJobs(any(JobSearchCriteria.class));
    }

    @Test
    void whenExternalDisabledOnlyInternalIsReturned() {
        // Este test demuestra el wiring lógico; para realmente deshabilitar la externa
        // en runtime, haríamos otra clase de test con properties diferentes.
        // Aquí simulamos que la externa no devuelve nada.
        when(externalJobSource.searchJobs(any())).thenReturn(List.of());

        Job internal = Job.builder()
                .title("Only Internal")
                .company("Acme")
                .country("USA")
                .salary(new BigDecimal("100000"))
                .skills(Set.of("Java"))
                .source("INTERNAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        jobRepository.save(internal);

        var out = jobService.search(JobSearchCriteria.builder().build());

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getTitle()).isEqualTo("Only Internal");
        verify(externalJobSource, times(1)).searchJobs(any()); // fue invocada, pero sin resultados
    }
}
