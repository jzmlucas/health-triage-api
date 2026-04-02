package com.health_ai_project.symptomservice;

import com.health_ai_project.symptomservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.symptomservice.dto.SymptomRequestDTO;
import com.health_ai_project.symptomservice.service.SymptomService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/symptoms")
public class SymptomController {

    private final SymptomService service;

    public SymptomController(SymptomService service) {
        this.service = service;
    }

    @PostMapping
    public DiagnosisResponseDTO process(@RequestBody @Valid SymptomRequestDTO dto) {
        return service.processSymptoms(dto);
    }
}
