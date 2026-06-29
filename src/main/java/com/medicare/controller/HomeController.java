package com.medicare.controller;

import com.medicare.model.Department;
import com.medicare.model.Doctor;
import com.medicare.model.Review;
import com.medicare.repository.ReviewRepository;
import com.medicare.service.DepartmentService;
import com.medicare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<Doctor> featuredDoctors = doctorService.getAllDoctors().stream().limit(3).toList();
        List<Department> departments = departmentService.getAllDepartments();
        
        model.addAttribute("featuredDoctors", featuredDoctors);
        model.addAttribute("departments", departments);
        model.addAttribute("totalDoctors", (long) doctorService.getAllDoctors().size());
        model.addAttribute("totalDepartments", (long) departments.size());
        model.addAttribute("totalPatients", 150L); // Mock stats
        model.addAttribute("successfulTreatments", 9500L); // Mock stats
        
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/doctors")
    public String doctorDirectory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long department,
            Model model) {
        
        List<Doctor> doctors;
        if (search != null && !search.trim().isEmpty()) {
            doctors = doctorService.searchDoctors(search);
        } else if (department != null) {
            doctors = doctorService.getDoctorsByDepartment(department);
        } else {
            doctors = doctorService.getAllDoctors();
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("selectedDept", department);
        model.addAttribute("searchQuery", search);
        
        return "doctors";
    }

    @GetMapping("/doctors/{id}")
    public String doctorProfile(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        
        List<Review> reviews = reviewRepository.findByDoctorId(id);
        Double avgRating = doctorService.getAverageRating(id);

        model.addAttribute("doctor", doctor);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0.0);
        
        return "doctor-profile";
    }

    @GetMapping("/book-appointment")
    public String bookAppointmentPage(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "book-appointment";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
