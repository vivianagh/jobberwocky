package io.github.vivianagh.jobberwocky.application.web;

import io.github.vivianagh.jobberwocky.application.servicio.JobService;
import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobRequest;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobResponse;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.PageResponse;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.SearchParams;
import io.github.vivianagh.jobberwocky.infrastructure.web.mapper.JobMapper;
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
    private final JobMapper jobMapper;

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        Job saved = jobService.create(toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(jobMapper.toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> search(SearchParams params) {
        var criteria = toCriteria(params);

        var page = params.page();
        var size = params.size();
        var sort = params.sort();

        var pageResult = jobService.searchPaged(criteria, page, size, sort);

        var mapped = pageResult.content().stream()
                .map(jobMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                new PageResponse<>(mapped, pageResult.page(), pageResult.size(), pageResult.total())
        );
    }

    private JobSearchCriteria toCriteria(SearchParams p) {
        return JobSearchCriteria.builder()
                .title(p.title())
                .country(p.country())
                .minSalary(parseBigDecimal(p.minSalary()))
                .maxSalary(parseBigDecimal(p.maxSalary()))
                .skill(p.skill())
                .build();
    }

    private BigDecimal parseBigDecimal(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return new BigDecimal(raw.trim());
    }

    private Job toDomain(JobRequest request) {
        return Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .country(request.getCountry())
                .city(request.getCity())
                .salary(request.getSalary())
                .skills(request.getSkills())
                .build();
    }

}
