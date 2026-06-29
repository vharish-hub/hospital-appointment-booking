package com.medicare.service;

import com.medicare.dto.AppointmentBookingRequest;
import com.medicare.dto.DashboardStatsDto;
import com.medicare.model.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(AppointmentBookingRequest request);
    List<Appointment> getAppointmentsByPatient(String email);
    List<Appointment> getAppointmentsByDoctor(String email);
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(Long id);
    Appointment updateAppointmentStatus(Long id, String status);
    List<String> getAvailableTimeSlots(Long doctorId, LocalDate date);
    void cancelAppointment(Long id, String patientEmail);
    DashboardStatsDto getAdminDashboardStats();
    List<Appointment> searchAppointments(LocalDate date, Long doctorId, Long departmentId, String status);
}
