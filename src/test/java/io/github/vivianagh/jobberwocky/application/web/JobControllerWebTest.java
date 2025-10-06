package io.github.vivianagh.jobberwocky.application.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.exception.GlobalExceptionHandler;
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

        @Bean
        GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JobService jobService; // es el mock registrado como @Bean


    @Test
    void postCreateReturn201AndBody() throws Exception {
        //Given
        Job created = Job.builder()
                .id(10L).title("Backend Eng").company("Acme").country("USA")
                .salary(new BigDecimal("120000")).skills(Set.of("Java"))
                .source("INTERNAL").createdAt(LocalDateTime.now()).build();

        //When
        when(jobService.create(any())).thenReturn(created);

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
    void getSearchReturns200List() throws Exception {
        when(jobService.search(any())).thenReturn(List.of(
                Job.builder().id(1L).title("Dev").company("Acme").country("USA")
                        .salary(new BigDecimal("100000")).skills(Set.of("Java"))
                        .source("INTERNAL").build()
        ));

        mvc.perform(get("/api/jobs").param("title","Dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dev"));
    }
}
