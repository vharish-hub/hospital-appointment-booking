package com.medicare.controller;

import com.medicare.dto.AppointmentBookingRequest;
import com.medicare.model.Appointment;
import com.medicare.model.Review;
import com.medicare.model.User;
import com.medicare.repository.ReviewRepository;
import com.medicare.service.AppointmentService;
import com.medicare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentApiController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentBookingRequest bookingRequest) {
        try {
            Appointment appointment = appointmentService.bookAppointment(bookingRequest);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<String> slots = appointmentService.getAvailableTimeSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, Principal principal) {
        try {
            appointmentService.cancelAppointment(id, principal.getName());
            Map<String, String> msg = new HashMap<>();
            msg.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/review")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> addReview(
            @RequestParam Long appointmentId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Principal principal) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            User patient = userService.findByEmail(principal.getName());
            
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "You can only review your own appointments"));
            }

            Review review = new Review();
            review.setPatient(patient);
            review.setDoctor(appointment.getDoctor());
            review.setRating(rating);
            review.setComment(comment);
            
            Review saved = reviewRepository.save(review);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
