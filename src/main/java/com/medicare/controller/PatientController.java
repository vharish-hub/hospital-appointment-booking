package com.medicare.controller;

import com.medicare.model.Appointment;
import com.medicare.model.User;
import com.medicare.service.AppointmentService;
import com.medicare.service.PdfGeneratorService;
import com.medicare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String email = principal.getName();
        User patient = userService.findByEmail(email);
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(email);

        List<Appointment> upcoming = appointments.stream()
                .filter(a -> "PENDING".equals(a.getStatus()) || "CONFIRMED".equals(a.getStatus()))
                .toList();
        List<Appointment> completed = appointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .toList();
        List<Appointment> cancelled = appointments.stream()
                .filter(a -> "CANCELLED".equals(a.getStatus()))
                .toList();

        model.addAttribute("patient", patient);
        model.addAttribute("upcoming", upcoming);
        model.addAttribute("completed", completed);
        model.addAttribute("cancelled", cancelled);
        
        return "patient/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String gender,
            @RequestParam String address,
            Principal principal,
            Model model) {
        
        try {
            userService.updateProfile(principal.getName(), fullName, phone, gender, address);
            return "redirect:/patient/dashboard?success=profile";
        } catch (Exception e) {
            return "redirect:/patient/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/profile/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Principal principal) {
        try {
            userService.changePassword(principal.getName(), currentPassword, newPassword);
            return "redirect:/patient/dashboard?success=password";
        } catch (Exception e) {
            return "redirect:/patient/dashboard?error=" + e.getMessage();
        }
    }

    @GetMapping("/appointments/slip/{id}")
    public ResponseEntity<InputStreamResource> downloadSlip(@PathVariable Long id, Principal principal) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        if (!appointment.getPatient().getEmail().equalsIgnoreCase(principal.getName())) {
            return ResponseEntity.status(403).body(null);
        }
        
        ByteArrayInputStream bis = pdfGeneratorService.generateAppointmentSlip(appointment);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=appointment-slip-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
