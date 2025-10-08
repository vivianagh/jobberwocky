package io.github.vivianagh.jobberwocky.application.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.exception.GlobalExceptionHandler;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobResponse;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.PageResponse;
import io.github.vivianagh.jobberwocky.infrastructure.web.mapper.JobMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JobController.class)
@Import(JobControllerWebTest.TestConfig.class)
public class JobControllerWebTest {


    @Configuration
    static class TestConfig {
        @Bean JobService jobService() {
            return Mockito.mock(JobService.class);
        }
        @Bean JobMapper jobMapper() {
            return Mockito.mock(JobMapper.class);
        }
        @Bean
        GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JobService jobService;   // mock
    @Autowired JobMapper jobMapper;


    @Test
    void postCreateReturn201AndBody() throws Exception {
        //Given
        Job created = Job.builder()
                .id(10L).title("Backend Eng").company("Acme").country("USA")
                .salary(new BigDecimal("120000")).skills(Set.of("Java"))
                .source("INTERNAL").createdAt(LocalDateTime.now()).build();

        //When
        when(jobService.create(any())).thenReturn(created);
        when(jobMapper.toResponse(created)).thenReturn(new JobResponse(
                created.getId(), created.getTitle(), created.getDescription(),
                created.getCompany(), created.getCountry(), created.getCity(),
                created.getSalary(), created.getSkills(), created.getSource(),
                created.getCreatedAt()
        ));
        String body = """
        {
          "title":"Backend Eng",
          "description":"APIs",
          "company":"Acme",
          "country":"USA",
          "city":"NYC",
          "salary":120000,
          "skills":["Java","Spring"]
        }
        """;
        //then
        mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Backend Eng"))
                .andExpect(jsonPath("$.source").value("INTERNAL"));

    }

    @Test
    void postCreateInvalidReturn400() throws Exception {

        String bad = """
        {"title":"","company":"Acme","country":"USA"}
        """;

        mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }

    @Test
    void getSearchPagedReturns200Page() throws Exception {
        // domain object -> response mapping
        Job j1 = Job.builder()
                .id(1L).title("Dev").company("Acme").country("USA")
                .salary(new BigDecimal("100000")).skills(Set.of("Java"))
                .source("INTERNAL").createdAt(LocalDateTime.now()).build();

        // service devuelve PageResponse<Job> (dominio)
        when(jobService.searchPaged(any(), eq(0), eq(1), eq("title,asc")))
                .thenReturn(new PageResponse<>(List.of(j1), 0, 1, 3));

        // mapper de dominio -> DTO
        when(jobMapper.toResponse(j1)).thenReturn(new JobResponse(
                j1.getId(), j1.getTitle(), j1.getDescription(),
                j1.getCompany(), j1.getCountry(), j1.getCity(),
                j1.getSalary(), j1.getSkills(), j1.getSource(),
                j1.getCreatedAt()
        ));

        mvc.perform(get("/api/jobs")
                        .param("title","Dev")
                        .param("page","0")
                        .param("size","1")
                        .param("sort","title,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.content[0].title").value("Dev"));
    }
}
