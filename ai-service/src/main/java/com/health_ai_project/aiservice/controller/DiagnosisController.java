package com.health_ai_project.aiservice.controller;

import com.health_ai_project.aiservice.dto.DiagnosisRequestDTO;
import com.health_ai_project.aiservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.aiservice.service.DiagnosisService;
import com.health_ai_project.aiservice.catalog.HealthCodeResponse;
import com.health_ai_project.aiservice.catalog.HealthCodeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final HealthCodeService healthCodeService;

    public DiagnosisController(DiagnosisService diagnosisService, HealthCodeService healthCodeService) {
        this.diagnosisService = diagnosisService;
        this.healthCodeService = healthCodeService;
    }

    @PostMapping("/diagnose")
    public DiagnosisResponseDTO diagnose(@RequestBody @Valid DiagnosisRequestDTO request) {
        return diagnosisService.analyzeSymptoms(request);
    }

    @GetMapping("/cid/{code}")
    public HealthCodeResponse lookupCid10(@PathVariable String code) {
        return healthCodeService.lookup(code);
    }
}
