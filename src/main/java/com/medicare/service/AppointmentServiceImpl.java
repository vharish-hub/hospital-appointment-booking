package com.medicare.service;

import com.medicare.dto.AppointmentBookingRequest;
import com.medicare.dto.DashboardStatsDto;
import com.medicare.model.Appointment;
import com.medicare.model.Doctor;
import com.medicare.model.User;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final List<String> ALL_SLOTS = List.of(
            "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "12:00 PM", "12:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM",
            "04:00 PM", "04:30 PM"
    );

    @Override
    @Transactional
    public Appointment bookAppointment(AppointmentBookingRequest request) {
        // Resolve patient by email, or create a guest profile
        User patient = userRepository.findByEmail(request.getEmail()).orElseGet(() -> {
            User guest = new User();
            guest.setFullName(request.getFullName());
            guest.setEmail(request.getEmail());
            guest.setPhone(request.getPhone());
            guest.setGender("Other");
            guest.setDob(LocalDate.now().minusYears(25));
            guest.setAddress("Anonymous Guest Address");
            guest.setPassword(passwordEncoder.encode("password"));
            guest.setRole("PATIENT");
            return userRepository.save(guest);
        });

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // 1. Availability check
        boolean isTaken = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getId(), request.getAppointmentDate(), request.getAppointmentTime(), "CANCELLED"
        );
        if (isTaken) {
            throw new IllegalArgumentException("Selected time slot is already booked");
        }

        // 2. Prevent duplicate booking by same patient with same doctor on same day/time
        boolean hasDuplicate = appointmentRepository.findAll().stream().anyMatch(a ->
                a.getPatient().getId().equals(patient.getId()) &&
                a.getDoctor().getId().equals(doctor.getId()) &&
                a.getAppointmentDate().equals(request.getAppointmentDate()) &&
                !a.getStatus().equals("CANCELLED")
        );
        if (hasDuplicate) {
            throw new IllegalArgumentException("An active appointment already exists for this patient email with this doctor on this day");
        }

        // 3. Compute token number
        List<Appointment> existing = appointmentRepository.findByDoctorIdAndAppointmentDate(
                doctor.getId(), request.getAppointmentDate()
        );
        int tokenNumber = existing.size() + 1;

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus("PENDING");
        appointment.setTokenNumber(tokenNumber);

        Appointment saved = appointmentRepository.save(appointment);

        // Send Email notification asynchronously
        try {
            emailService.sendAppointmentCreatedEmail(saved);
        } catch (Exception e) {
            // Log error but do not break transaction
            System.err.println("Failed to send booking email: " + e.getMessage());
        }

        // Broadcast queue update via WebSocket
        broadcastQueueUpdate(doctor.getId(), request.getAppointmentDate());

        return saved;
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        return appointmentRepository.findByPatientId(user.getId());
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(String email) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        return appointmentRepository.findByDoctorId(doctor.getId());
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
    }

    @Override
    @Transactional
    public Appointment updateAppointmentStatus(Long id, String status) {
        Appointment appointment = getAppointmentById(id);
        String oldStatus = appointment.getStatus();
        appointment.setStatus(status.toUpperCase());
        Appointment saved = appointmentRepository.save(appointment);

        try {
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                emailService.sendAppointmentConfirmedEmail(saved);
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                emailService.sendAppointmentCancelledEmail(saved);
            }
        } catch (Exception e) {
            System.err.println("Failed to send status update email: " + e.getMessage());
        }

        // Broadcast queue update
        broadcastQueueUpdate(appointment.getDoctor().getId(), appointment.getAppointmentDate());

        return saved;
    }

    @Override
    public List<String> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndDateAndStatusIn(
                doctorId, date, List.of("PENDING", "CONFIRMED", "COMPLETED")
        );
        Set<String> bookedSlots = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        return ALL_SLOTS.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id, String patientEmail) {
        Appointment appointment = getAppointmentById(id);
        if (!appointment.getPatient().getEmail().equalsIgnoreCase(patientEmail)) {
            throw new IllegalStateException("You are not authorized to cancel this appointment");
        }
        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);

        try {
            emailService.sendAppointmentCancelledEmail(appointment);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }

        broadcastQueueUpdate(appointment.getDoctor().getId(), appointment.getAppointmentDate());
    }

    @Override
    public DashboardStatsDto getAdminDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalDoctors(doctorRepository.count());
        stats.setTotalPatients(userRepository.countByRole("PATIENT"));
        stats.setTotalAppointments(appointmentRepository.count());
        stats.setTodaysAppointments(appointmentRepository.countByAppointmentDate(LocalDate.now()));
        
        Double revenue = appointmentRepository.calculateTotalRevenue();
        stats.setTotalRevenue(revenue != null ? revenue : 0.0);

        // Generate Chart datasets (Last 6 Months)
        List<String> months = new ArrayList<>();
        List<Long> bookings = new ArrayList<>();
        List<Double> monthlyRev = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate targetDate = today.minusMonths(i);
            Month month = targetDate.getMonth();
            int year = targetDate.getYear();
            
            String label = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;
            months.add(label);

            bookings.add(appointmentRepository.countByMonthAndYear(month.getValue(), year));
            monthlyRev.add(appointmentRepository.calculateRevenueByMonthAndYear(month.getValue(), year));
        }
        stats.setMonthlyLabels(months);
        stats.setMonthlyBookings(bookings);
        stats.setMonthlyRevenue(monthlyRev);

        // Department-wise analysis
        List<Object[]> deptStats = appointmentRepository.countAppointmentsByDepartment();
        List<String> depts = new ArrayList<>();
        List<Long> deptBookings = new ArrayList<>();
        for (Object[] row : deptStats) {
            depts.add((String) row[0]);
            deptBookings.add((Long) row[1]);
        }
        stats.setDepartmentLabels(depts);
        stats.setDepartmentBookings(deptBookings);

        return stats;
    }

    @Override
    public List<Appointment> searchAppointments(LocalDate date, Long doctorId, Long departmentId, String status) {
        return appointmentRepository.findAll().stream()
                .filter(a -> date == null || a.getAppointmentDate().equals(date))
                .filter(a -> doctorId == null || a.getDoctor().getId().equals(doctorId))
                .filter(a -> departmentId == null || a.getDoctor().getDepartment().getId().equals(departmentId))
                .filter(a -> status == null || status.trim().isEmpty() || a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    private void broadcastQueueUpdate(Long doctorId, LocalDate date) {
        if (!date.equals(LocalDate.now())) {
            return; // Only broadcast queue status for today
        }

        List<Appointment> todayApps = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
        
        // Find current token being served (the first CONFIRMED/COMPLETED appointment of today)
        // If an appointment is COMPLETED, maybe the queue has advanced. Let's find the current active token:
        // Current token is either the first PENDING/CONFIRMED or the last COMPLETED.
        Optional<Appointment> activeApp = todayApps.stream()
                .filter(a -> "CONFIRMED".equalsIgnoreCase(a.getStatus()) || "PENDING".equalsIgnoreCase(a.getStatus()))
                .min(Comparator.comparing(Appointment::getTokenNumber));

        int currentToken = activeApp.map(Appointment::getTokenNumber).orElse(0);
        if (currentToken == 0) {
            // If no pending or confirmed, check if there are completed ones, set to last completed token + 1
            currentToken = todayApps.stream()
                    .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                    .map(Appointment::getTokenNumber)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
        }

        Map<String, Object> queueData = new HashMap<>();
        queueData.put("doctorId", doctorId);
        queueData.put("currentToken", currentToken);
        messagingTemplate.convertAndSend("/topic/queue/" + doctorId, queueData);
    }
}
