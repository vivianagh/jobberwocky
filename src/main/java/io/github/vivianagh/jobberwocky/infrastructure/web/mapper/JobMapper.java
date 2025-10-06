package io.github.vivianagh.jobberwocky.infrastructure.web.mapper;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.infrastructure.web.dto.JobResponse;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {
    public JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getCompany(),
                job.getCountry(),
                job.getCity(),
                job.getSalary(),
                job.getSkills(),
                job.getSource(),
                job.getCreatedAt()
        );
    }
}
