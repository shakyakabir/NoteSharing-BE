package com.example.notesharing.Repository;

import com.example.notesharing.modal.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, UUID> {

    List<GeneratedReport> findByUserEmail(String userEmail);
}
