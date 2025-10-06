package io.github.vivianagh.jobberwocky.application.servicio;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import io.github.vivianagh.jobberwocky.domain.model.JobSearchCriteria;
import io.github.vivianagh.jobberwocky.domain.port.JobSource;
import io.github.vivianagh.jobberwocky.infrastructure.repository.JobRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobSource jobSource;

    @Transactional
    public Job create(Job job) {
        job.setSource("INTERNAL");
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<Job> search(JobSearchCriteria criteria) {
        if (criteria == null) {
            criteria = JobSearchCriteria.builder().build();
        }
        criteria.validate();
        return jobSource.searchJobs(criteria);
    }


}
