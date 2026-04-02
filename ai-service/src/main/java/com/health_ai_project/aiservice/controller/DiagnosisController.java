package com.health_ai_project.aiservice.controller;

import com.health_ai_project.aiservice.dto.DiagnosisRequestDTO;
import com.health_ai_project.aiservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.aiservice.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/diagnose")
    public DiagnosisResponseDTO diagnose(@RequestBody @Valid DiagnosisRequestDTO request) {
        return diagnosisService.analyzeSymptoms(request);
    }
}
