package org.example.patientservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.example.patientservice.dto.PatientRequestDTO;
import org.example.patientservice.dto.PatientResponseDTO;
import org.example.patientservice.dto.validators.CreatePatientValidationGroup;
import org.example.patientservice.service.PatientService;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ser.jackson.RawSerializer;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients") //http://localhost/4000/patients
@Tag(name = "Patient",description = "API for managing patients")
public class PatientController {
    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients(){
        List<PatientResponseDTO> patients = service.getPatients();
        return ResponseEntity.ok().body(patients);
    }

    @PostMapping
    @Operation(summary = "Create a new Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO preqdto){
        PatientResponseDTO presdto = service.createPatient(preqdto);

        return ResponseEntity.ok().body(presdto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a new Patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,
                                                            @Validated({Default.class}) @RequestBody PatientRequestDTO preqdto){
        PatientResponseDTO presdto = service.updatePatient(id,preqdto);
        return ResponseEntity.ok().body(presdto);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a new Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id){
        service.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
