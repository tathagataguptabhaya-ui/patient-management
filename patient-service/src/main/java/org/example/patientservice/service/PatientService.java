package org.example.patientservice.service;

import billing.BillingServiceGrpc;
import org.example.patientservice.dto.PatientRequestDTO;
import org.example.patientservice.dto.PatientResponseDTO;
import org.example.patientservice.exeption.EmailAlreadyExistsException;
import org.example.patientservice.exeption.PatientNotFoundException;
import org.example.patientservice.grpc.BilingServiceGRPCClient;
import org.example.patientservice.mapper.PatientMapper;
import org.example.patientservice.model.Patient;
import org.example.patientservice.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository repo;
    private final BilingServiceGRPCClient bilingServiceGRPCClient;

    public PatientService(PatientRepository repo, BilingServiceGRPCClient bilingServiceGRPCClient){
        this.repo=repo;
        this.bilingServiceGRPCClient = bilingServiceGRPCClient;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = repo.findAll();

        return patients.stream()
                .map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO reqp)
    {
        if(repo.existsByEmail(reqp.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email"+
                    "already exists"+ reqp.getEmail());
        }
        Patient newPatient = repo.save(
                PatientMapper.toModel(reqp));

        bilingServiceGRPCClient.createBillingAccount(newPatient.getId().toString(),
                newPatient.getName(),newPatient.getEmail());
        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id,
                                            PatientRequestDTO preqDTO){
        Patient patient = repo.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: "+ id));
        if(repo.existsByEmailAndIdNot(preqDTO.getEmail(),id )){
            throw new EmailAlreadyExistsException("A patient with this email already exists"+ preqDTO.getEmail());
        }
        patient.setName(preqDTO.getName());
        patient.setAddress(preqDTO.getAddress());
        patient.setEmail(preqDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(preqDTO.getDateOfBirth()));

        Patient updatedPatient = repo.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }
    @DeleteMapping
    public void deletePatient(UUID id){
        repo.deleteById(id);
    }
}
