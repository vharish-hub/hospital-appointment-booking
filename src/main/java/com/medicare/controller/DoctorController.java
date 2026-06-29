package com.medicare.controller;

import com.medicare.model.Appointment;
import com.medicare.model.Doctor;
import com.medicare.service.AppointmentService;
import com.medicare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String email = principal.getName();
        Doctor doctor = doctorService.findByEmail(email);
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(email);

        LocalDate today = LocalDate.now();
        List<Appointment> todayApps = appointments.stream()
                .filter(a -> a.getAppointmentDate().equals(today))
                .toList();
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getAppointmentDate().isAfter(today) && !"CANCELLED".equals(a.getStatus()))
                .toList();
        List<Appointment> completed = appointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .toList();

        model.addAttribute("doctor", doctor);
        model.addAttribute("todayApps", todayApps);
        model.addAttribute("upcoming", upcoming);
        model.addAttribute("completed", completed);

        return "doctor/dashboard";
    }

    @PostMapping("/appointments/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, Principal principal) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        Doctor doctor = doctorService.findByEmail(principal.getName());
        
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/dashboard?error=unauthorized";
        }
        
        appointmentService.updateAppointmentStatus(id, status);
        return "redirect:/doctor/dashboard?success=status";
    }

    @PostMapping("/availability/update")
    public String updateAvailability(@RequestParam String availability, Principal principal) {
        try {
            Doctor doctor = doctorService.findByEmail(principal.getName());
            doctor.setAvailability(availability);
            doctorService.saveDoctor(doctor);
            return "redirect:/doctor/dashboard?success=availability";
        } catch (Exception e) {
            return "redirect:/doctor/dashboard?error=" + e.getMessage();
        }
    }
}
