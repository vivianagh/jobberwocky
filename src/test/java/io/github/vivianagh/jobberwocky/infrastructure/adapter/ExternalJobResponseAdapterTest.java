package io.github.vivianagh.jobberwocky.infrastructure.adapter;


/*
*
* {
  "USA": [
    ["Cloud Engineer", 65000, "<skills><skill>AWS</skill></skills>"],
    ["DevOps Engineer", 60000, "<skills><skill>CI/CD</skill></skills>"]
  ],
  "Spain": [
    ["ML Engineer", 75000, "<skills><skill>Python</skill></skills>"]
  ]
}
* Keys = country
* values = Jobs arrays
* every job [title,salary,skillsXml
* */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExternalJobResponseAdapterTest {

    @Mock
    private XmlSkillsParser xmlSkillsParser;

    private ExternalJobResponseAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        adapter = new ExternalJobResponseAdapter(objectMapper, xmlSkillsParser);
    }

    @Test
    void shouldAdaptSingleJobFRomSingleCountry() {
        //Given
        String json = """
                {
                    "USA": [
                        ["Cloude Engineer", 65000, "<skills><skill>AWS</skill></skills>"]
                    ]
                }
                """;
        when(xmlSkillsParser.parseSkills(anyString()))
                .thenReturn(Set.of("AWS"));

        //When
        List<Job> jobs = adapter.adapt(json);

        //Then
        assertThat(jobs).hasSize(1);

        Job job = jobs.get(0);
        assertThat(job.getTitle()).isEqualTo("Cloude Engineer");
        assertThat(job.getCountry()).isEqualTo("USA");
        assertThat(job.getSalary()).isEqualByComparingTo(new BigDecimal("65000"));
        assertThat(job.getSkills()).containsExactly("AWS");
        assertThat(job.getSource()).isEqualTo("EXTERNAL_API");
        assertThat(job.getExternalId()).isNotNull();
        assertThat(job.getCompany()).isNotNull();
    }

    @Test
    void shouldAdaptMultipleJobsFromSingleCountry() {
        // Given
        String json = """
            {
              "USA": [
                ["Cloud Engineer", 65000, "<skills><skill>AWS</skill></skills>"],
                ["DevOps Engineer", 60000, "<skills><skill>Docker</skill></skills>"]
              ]
            }
            """;

        when(xmlSkillsParser.parseSkills(anyString()))
                .thenReturn(Set.of("AWS"))
                .thenReturn(Set.of("Docker"));

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then
        assertThat(jobs).hasSize(2);
        assertThat(jobs).extracting(Job::getTitle)
                .containsExactlyInAnyOrder("Cloud Engineer", "DevOps Engineer");
        assertThat(jobs).allMatch(job -> job.getCountry().equals("USA"));
    }

    @Test
    void shouldAdaptJobsFromMultipleCountries() {
        // Given
        String json = """
            {
              "USA": [
                ["Backend Developer", 100000, "<skills><skill>Java</skill></skills>"]
              ],
              "Spain": [
                ["Frontend Developer", 60000, "<skills><skill>React</skill></skills>"]
              ],
              "Germany": [
                ["DevOps Engineer", 80000, "<skills><skill>Kubernetes</skill></skills>"]
              ]
            }
            """;

        when(xmlSkillsParser.parseSkills(anyString()))
                .thenReturn(Set.of("Java"))
                .thenReturn(Set.of("React"))
                .thenReturn(Set.of("Kubernetes"));

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then
        assertThat(jobs).hasSize(3);
        assertThat(jobs).extracting(Job::getCountry)
                .containsExactlyInAnyOrder("USA", "Spain", "Germany");
    }

    @Test
    void shouldGenerateUniqueExternalIds() {
        // Given
        String json = """
            {
              "USA": [
                ["Engineer", 100000, "<skills></skills>"],
                ["Engineer", 100000, "<skills></skills>"]
              ]
            }
            """;

        when(xmlSkillsParser.parseSkills(anyString()))
                .thenReturn(Set.of());

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then
        assertThat(jobs).hasSize(2);
        // External IDs should be the same (for deduplication)
        assertThat(jobs.get(0).getExternalId())
                .isEqualTo(jobs.get(1).getExternalId());
    }

    @Test
    void shouldHandleEmptyResponse() {
        // Given
        String json = "{}";

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then
        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldHandleCountryWithNoJobs() {
        // Given
        String json = """
            {
              "USA": []
            }
            """;

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then
        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldHandleInvalidJsonGracefully() {
        // Given
        String json = "{ invalid json }";

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then - should return empty list instead of throwing
        assertThat(jobs).isNotNull();
        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldHandleRealWorldComplexExample() {
        // Given - real example similar to external API
        String json = """
            {
              "USA": [
                ["Cloud Engineer", 65000, "<skills><skill>AWS</skill><skill>Azure</skill><skill>Docker</skill></skills>"],
                ["DevOps Engineer", 60000, "<skills><skill>CI/CD</skill><skill>Docker</skill><skill>Kubernetes</skill></skills>"]
              ],
              "Spain": [
                ["Machine Learning Engineer", 75000, "<skills><skill>Python</skill><skill>TensorFlow</skill><skill>Deep Learning</skill></skills>"]
              ]
            }
            """;

        when(xmlSkillsParser.parseSkills(anyString()))
                .thenReturn(Set.of("AWS", "Azure", "Docker"))
                .thenReturn(Set.of("CI/CD", "Docker", "Kubernetes"))
                .thenReturn(Set.of("Python", "TensorFlow", "Deep Learning"));

        // When
        List<Job> jobs = adapter.adapt(json);

        // Then
        assertThat(jobs).hasSize(3);

        // Verify USA jobs
        List<Job> usaJobs = jobs.stream()
                .filter(j -> j.getCountry().equals("USA"))
                .toList();
        assertThat(usaJobs).hasSize(2);

        // Verify Spain jobs
        List<Job> spainJobs = jobs.stream()
                .filter(j -> j.getCountry().equals("Spain"))
                .toList();
        assertThat(spainJobs).hasSize(1);
        assertThat(spainJobs.get(0).getTitle()).isEqualTo("Machine Learning Engineer");
        assertThat(spainJobs.get(0).getSalary()).isEqualByComparingTo(new BigDecimal("75000"));

        // All should have EXTERNAL_API source
        assertThat(jobs).allMatch(job -> "EXTERNAL_API".equals(job.getSource()));
    }

}
