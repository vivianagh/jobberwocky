package io.github.vivianagh.jobberwocky.application.web;

import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobRequest;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        Job toSave = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .country(request.getCompany())
                .city(request.getCity())
                .salary(request.getSalary())
                .skills(request.getSkills())
                .build();
        Job saved = jobService.create(toSave);

        JobResponse body = JobResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .company(saved.getCompany())
                .country(saved.getCountry())
                .city(saved.getCity())
                .salary(saved.getSalary())
                .skills(saved.getSkills())
                .source(saved.getSource())
                .createdAt(saved.getCreatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) String skill
    ) {
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title(title)
                .country(country)
                .minSalary(minSalary)
                .maxSalary(maxSalary)
                .skill(skill)
                .build();

        List<Job> jobs = jobService.search(criteria);

        List<JobResponse> out = jobs.stream().map(j ->
                JobResponse.builder()
                        .id(j.getId())
                        .title(j.getTitle())
                        .description(j.getDescription())
                        .company(j.getCompany())
                        .country(j.getCountry())
                        .city(j.getCity())
                        .salary(j.getSalary())
                        .skills(j.getSkills())
                        .source(j.getSource())
                        .createdAt(j.getCreatedAt())
                        .build()
        ).toList();

        return ResponseEntity.ok(out);
    }
}
