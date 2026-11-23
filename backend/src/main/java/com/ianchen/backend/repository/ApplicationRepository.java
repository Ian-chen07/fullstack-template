package com.ianchen.backend.repository;

import com.ianchen.backend.entity.Application;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByApplicantIdOrderByIdAsc(Long applicantId);

    List<Application> findByProjectIdOrderByIdAsc(Long projectId);
}
