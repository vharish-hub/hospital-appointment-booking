package com.medicare.service;

import com.medicare.model.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    List<Doctor> getAllDoctors();
    Optional<Doctor> getDoctorById(Long id);
    Doctor findByEmail(String email);
    List<Doctor> getDoctorsByDepartment(Long departmentId);
    List<Doctor> searchDoctors(String query);
    Doctor saveDoctor(Doctor doctor);
    void deleteDoctor(Long id);
    Double getAverageRating(Long doctorId);
}
