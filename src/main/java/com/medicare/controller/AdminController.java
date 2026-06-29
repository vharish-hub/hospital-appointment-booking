package com.medicare.controller;

import com.medicare.dto.DashboardStatsDto;
import com.medicare.model.Appointment;
import com.medicare.model.Department;
import com.medicare.model.Doctor;
import com.medicare.model.User;
import com.medicare.repository.DepartmentRepository;
import com.medicare.repository.UserRepository;
import com.medicare.service.AppointmentService;
import com.medicare.service.DoctorService;
import com.medicare.service.PdfGeneratorService;
import com.medicare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = appointmentService.getAdminDashboardStats();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    // --- DOCTOR MANAGEMENT ---
    @GetMapping("/doctors")
    public String doctorManagement(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/doctors";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String qualification,
            @RequestParam String specialization,
            @RequestParam Long departmentId,
            @RequestParam Integer experience,
            @RequestParam Double consultationFee,
            @RequestParam String availability,
            @RequestParam(required = false) String profileImage) {
        
        try {
            // 1. Create Login User profile for the Doctor
            User user = new User();
            user.setFullName(name);
            user.setEmail(email);
            user.setPhone("+100000000");
            user.setGender("Other");
            user.setDob(LocalDate.now().minusYears(30));
            user.setAddress("Hospital Clinic Office");
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("DOCTOR");
            userRepository.save(user);

            // 2. Create Doctor profile
            Doctor doctor = new Doctor();
            doctor.setEmail(email);
            doctor.setName(name);
            doctor.setQualification(qualification);
            doctor.setSpecialization(specialization);
            
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            doctor.setDepartment(department);
            
            doctor.setExperience(experience);
            doctor.setConsultationFee(consultationFee);
            doctor.setAvailability(availability);
            doctor.setProfileImage(profileImage == null || profileImage.trim().isEmpty() ? "default-doctor.jpg" : profileImage);
            
            doctorService.saveDoctor(doctor);
            return "redirect:/admin/doctors?success=added";
        } catch (Exception e) {
            return "redirect:/admin/doctors?error=" + e.getMessage();
        }
    }

    @PostMapping("/doctors/edit/{id}")
    public String editDoctor(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String qualification,
            @RequestParam String specialization,
            @RequestParam Long departmentId,
            @RequestParam Integer experience,
            @RequestParam Double consultationFee,
            @RequestParam String availability,
            @RequestParam(required = false) String profileImage) {
        
        try {
            Doctor doctor = doctorService.getDoctorById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
            doctor.setName(name);
            doctor.setQualification(qualification);
            doctor.setSpecialization(specialization);
            
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            doctor.setDepartment(department);
            
            doctor.setExperience(experience);
            doctor.setConsultationFee(consultationFee);
            doctor.setAvailability(availability);
            if (profileImage != null && !profileImage.trim().isEmpty()) {
                doctor.setProfileImage(profileImage);
            }
            
            doctorService.saveDoctor(doctor);
            return "redirect:/admin/doctors?success=updated";
        } catch (Exception e) {
            return "redirect:/admin/doctors?error=" + e.getMessage();
        }
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
            
            // Delete user record first if exists
            userRepository.findByEmail(doctor.getEmail()).ifPresent(u -> userRepository.delete(u));
            doctorService.deleteDoctor(id);
            
            return "redirect:/admin/doctors?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/doctors?error=" + e.getMessage();
        }
    }

    // --- PATIENT MANAGEMENT ---
    @GetMapping("/patients")
    public String patientManagement(Model model) {
        model.addAttribute("patients", userService.getAllPatients());
        return "admin/patients";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        try {
            userService.deletePatient(id);
            return "redirect:/admin/patients?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/patients?error=" + e.getMessage();
        }
    }

    // --- APPOINTMENT MANAGEMENT ---
    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            Model model) {
        
        List<Appointment> filtered = appointmentService.searchAppointments(date, doctorId, departmentId, status);
        model.addAttribute("appointments", filtered);
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedDoctor", doctorId);
        model.addAttribute("selectedDept", departmentId);
        model.addAttribute("selectedStatus", status);
        
        return "admin/appointments";
    }

    @PostMapping("/appointments/{id}/status")
    public String updateAppointmentStatus(@PathVariable Long id, @RequestParam String status) {
        appointmentService.updateAppointmentStatus(id, status);
        return "redirect:/admin/appointments?success=status";
    }

    // --- REPORTS AND EXPORTS ---
    @GetMapping("/reports")
    public String reports(Model model) {
        return "admin/reports";
    }

    @GetMapping("/reports/pdf")
    public ResponseEntity<InputStreamResource> downloadPdfReport(@RequestParam(required = false) String type) {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        String reportTitle = "All-Time Hospital System Audit";

        if ("today".equalsIgnoreCase(type)) {
            appointments = appointments.stream().filter(a -> a.getAppointmentDate().equals(LocalDate.now())).toList();
            reportTitle = "Daily Report: " + LocalDate.now();
        } else if ("month".equalsIgnoreCase(type)) {
            appointments = appointments.stream().filter(a -> a.getAppointmentDate().getMonth() == LocalDate.now().getMonth()).toList();
            reportTitle = "Monthly Report: " + LocalDate.now().getMonth();
        }

        ByteArrayInputStream bis = pdfGeneratorService.generateAppointmentsPdfReport(appointments, reportTitle);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=medicare-report-" + type + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<InputStreamResource> downloadExcelReport() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        ByteArrayInputStream bis = pdfGeneratorService.generateAppointmentsExcelReport(appointments);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=medicare-report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}
