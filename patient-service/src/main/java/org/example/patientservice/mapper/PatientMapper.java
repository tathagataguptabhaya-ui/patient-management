package org.example.patientservice.mapper;

import org.example.patientservice.dto.PatientRequestDTO;
import org.example.patientservice.dto.PatientResponseDTO;
import org.example.patientservice.model.Patient;

import java.time.LocalDate;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient patient){
        PatientResponseDTO patientDTO = new PatientResponseDTO();
        patientDTO.setId(patient.getId().toString());
        patientDTO.setName(patient.getName().toString());
        patientDTO.setAddress(patient.getAddress().toString());
        patientDTO.setEmail(patient.getEmail().toString());
        patientDTO.setDateOfBirth(patient.getDateOfBirth().toString());
        patientDTO.setRegisteredDate(patient.getRegisteredDate().toString());
        return patientDTO;
    }

    public static Patient toModel(PatientRequestDTO patientrespDTO){
        Patient patient = new Patient();
        patient.setName(patientrespDTO.getName());
        patient.setAddress(patientrespDTO.getAddress());
        patient.setEmail(patientrespDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientrespDTO.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientrespDTO.getRegisteredDate()));
        return patient;
    }
}
