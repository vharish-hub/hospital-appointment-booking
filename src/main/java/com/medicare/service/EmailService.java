package com.medicare.service;

import com.medicare.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendAppointmentCreatedEmail(Appointment appointment) {
        String subject = "Appointment Booked: " + appointment.getTokenNumber();
        String body = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been successfully booked at MediCare Hospital.\n\n" +
                "Details:\n" +
                "Appointment Number: %d\n" +
                "Doctor: %s\n" +
                "Department: %s\n" +
                "Date: %s\n" +
                "Time Slot: %s\n" +
                "Status: %s\n\n" +
                "You can track your appointment status in real-time or reschedule/cancel it in your Patient Portal:\n" +
                "URL: http://localhost:8081/login\n" +
                "Username: %s\n" +
                "Password: password (we recommend changing this upon your first login)\n\n" +
                "Thank you for choosing MediCare!\n" +
                "Best regards,\n" +
                "MediCare Administration",
                appointment.getPatient().getFullName(),
                appointment.getId(),
                appointment.getDoctor().getName(),
                appointment.getDoctor().getDepartment().getDepartmentName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getPatient().getEmail()
        );

        sendEmail(appointment.getPatient().getEmail(), subject, body);
    }

    @Async
    public void sendAppointmentConfirmedEmail(Appointment appointment) {
        String subject = "Appointment Confirmed - MediCare Hospital";
        String body = String.format(
                "Dear %s,\n\n" +
                "We are pleased to inform you that your appointment has been CONFIRMED by the doctor.\n\n" +
                "Details:\n" +
                "Appointment Number: %d\n" +
                "Doctor: %s\n" +
                "Date: %s\n" +
                "Time Slot: %s\n" +
                "Token Number: %d\n\n" +
                "Please arrive 15 minutes before your scheduled slot.\n\n" +
                "Best regards,\n" +
                "MediCare Administration",
                appointment.getPatient().getFullName(),
                appointment.getId(),
                appointment.getDoctor().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getTokenNumber()
        );

        sendEmail(appointment.getPatient().getEmail(), subject, body);
    }

    @Async
    public void sendAppointmentCancelledEmail(Appointment appointment) {
        String subject = "Appointment Cancelled - MediCare Hospital";
        String body = String.format(
                "Dear %s,\n\n" +
                "This email is to confirm that your appointment (ID: %d) with %s has been CANCELLED.\n\n" +
                "If you wish to reschedule, please visit our online booking portal.\n\n" +
                "Best regards,\n" +
                "MediCare Administration",
                appointment.getPatient().getFullName(),
                appointment.getId(),
                appointment.getDoctor().getName()
        );

        sendEmail(appointment.getPatient().getEmail(), subject, body);
    }

    @Async
    public void sendReminderEmail(Appointment appointment) {
        String subject = "Upcoming Appointment Reminder - MediCare Hospital";
        String body = String.format(
                "Dear %s,\n\n" +
                "This is a friendly reminder that you have an upcoming appointment tomorrow.\n\n" +
                "Details:\n" +
                "Doctor: %s\n" +
                "Date: %s\n" +
                "Time Slot: %s\n" +
                "Token Number: %d\n\n" +
                "We look forward to seeing you.\n\n" +
                "Best regards,\n" +
                "MediCare Administration",
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getTokenNumber()
        );

        sendEmail(appointment.getPatient().getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        System.out.println("----------------------------------------");
        System.out.println("PREPARING EMAIL FOR: " + to);
        System.out.println("SUBJECT: " + subject);
        System.out.println("BODY:\n" + body);
        System.out.println("----------------------------------------");

        if (mailSender == null) {
            System.out.println("JavaMailSender not configured. Email logged to console.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@medicare.com");
            mailSender.send(message);
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("Failed to send email via SMTP: " + e.getMessage() + ". Logged content to console.");
        }
    }
}
