package com.medicare.service;

import com.medicare.model.Doctor;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.ReviewRepository;
import com.medicare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    @Override
    public Doctor findByEmail(String email) {
        return doctorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor profile not found for email: " + email));
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Doctor> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorRepository.searchDoctors(query);
    }

    @Override
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        reviewRepository.deleteByDoctorId(id);
        appointmentRepository.deleteByDoctorId(id);
        doctorRepository.deleteById(id);
    }

    @Override
    public Double getAverageRating(Long doctorId) {
        return reviewRepository.getAverageRatingByDoctorId(doctorId);
    }
}
