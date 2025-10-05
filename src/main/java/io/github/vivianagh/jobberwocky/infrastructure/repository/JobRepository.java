package io.github.vivianagh.jobberwocky.infrastructure.repository;

import io.github.vivianagh.jobberwocky.domain.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
}
