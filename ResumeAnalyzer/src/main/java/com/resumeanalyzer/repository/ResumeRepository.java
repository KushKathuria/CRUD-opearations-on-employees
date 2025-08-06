package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUsername(String username);
    List<Resume> findByNameContainingIgnoreCaseOrSkillsContainingIgnoreCase(String name, String skill);

}