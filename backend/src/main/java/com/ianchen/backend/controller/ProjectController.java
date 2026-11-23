package com.ianchen.backend.controller;

import com.ianchen.backend.entity.Project;
import com.ianchen.backend.repository.ProjectRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectController {

    private final ProjectRepository repository;

    public ProjectController(ProjectRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/project")
    public Project project() {
        return repository.findById(1L).orElse(null);
    }
}