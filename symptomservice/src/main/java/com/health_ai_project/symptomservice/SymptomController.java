package com.health_ai_project.symptomservice;

import com.health_ai_project.symptomservice.dto.DiagnosisResponseDTO;
import com.health_ai_project.symptomservice.dto.SymptomRequestDTO;
import com.health_ai_project.symptomservice.service.SymptomService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    public DiagnosisResponseDTO process(@RequestBody @Valid SymptomRequestDTO dto, Authentication authentication) {
        if (!dto.getUserId().toString().equals(authentication.getName())) throw new AccessDeniedException("Acesso negado");
        return service.processSymptoms(dto);
    }
}
