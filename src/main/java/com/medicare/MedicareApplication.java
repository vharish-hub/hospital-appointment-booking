package com.medicare;

import com.medicare.model.User;
import com.medicare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MedicareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicareApplication.class, args);
    }

    @Bean
    public CommandLineRunner bootstrapData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=================================================");
            System.out.println("[BOOTSTRAP] RUNNING DATABASE WORKER...");
            System.out.println("=================================================");

            // 1. Ensure Admin exists and has correct password hash
            userRepository.findByEmail("admin@medicare.com").ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("password"));
                    userRepository.save(admin);
                    System.out.println("[BOOTSTRAP] Refreshed password for admin@medicare.com");
                },
                () -> {
                    User admin = new User();
                    admin.setFullName("System Admin");
                    admin.setEmail("admin@medicare.com");
                    admin.setPhone("+1234567890");
                    admin.setGender("Male");
                    admin.setDob(LocalDate.of(1985, 6, 15));
                    admin.setAddress("123 Health Ave, Medical City");
                    admin.setPassword(passwordEncoder.encode("password"));
                    admin.setRole("ADMIN");
                    userRepository.save(admin);
                    System.out.println("[BOOTSTRAP] Created admin@medicare.com");
                }
            );

            // 2. Ensure default patient exists and has correct password hash
            userRepository.findByEmail("john.doe@gmail.com").ifPresentOrElse(
                patient -> {
                    patient.setPassword(passwordEncoder.encode("password"));
                    userRepository.save(patient);
                    System.out.println("[BOOTSTRAP] Refreshed password for john.doe@gmail.com");
                },
                () -> {
                    User patient = new User();
                    patient.setFullName("John Doe");
                    patient.setEmail("john.doe@gmail.com");
                    patient.setPhone("+1987654321");
                    patient.setGender("Male");
                    patient.setDob(LocalDate.of(1990, 10, 10));
                    patient.setAddress("12 Main Street, Springfield");
                    patient.setPassword(passwordEncoder.encode("password"));
                    patient.setRole("PATIENT");
                    userRepository.save(patient);
                    System.out.println("[BOOTSTRAP] Created john.doe@gmail.com");
                }
            );

            // 3. Ensure doctor logins exist and have correct password hashes
            String[] doctorEmails = {
                "mithilesh@medicare.com",
                "hemanth@medicare.com",
                "rahul@medicare.com"
            };
            String[] doctorNames = {
                "Dr. Mithilesh",
                "Dr. Hemanth",
                "Dr. Rahul"
            };

            for (int i = 0; i < doctorEmails.length; i++) {
                final int index = i;
                final String email = doctorEmails[i];
                final String name = doctorNames[i];
                userRepository.findByEmail(email).ifPresentOrElse(
                    docUser -> {
                        docUser.setPassword(passwordEncoder.encode("password"));
                        userRepository.save(docUser);
                        System.out.println("[BOOTSTRAP] Refreshed password for " + email);
                    },
                    () -> {
                        User docUser = new User();
                        docUser.setFullName(name);
                        docUser.setEmail(email);
                        docUser.setPhone("+10000000" + index);
                        docUser.setGender("Other");
                        docUser.setDob(LocalDate.of(1975, 1, 1));
                        docUser.setAddress("Hospital Clinic Office");
                        docUser.setPassword(passwordEncoder.encode("password"));
                        docUser.setRole("DOCTOR");
                        userRepository.save(docUser);
                        System.out.println("[BOOTSTRAP] Created login user for " + email);
                    }
                );
            }

            System.out.println("=================================================");
            System.out.println("[BOOTSTRAP] DATABASE WORKER COMPLETE.");
            System.out.println("=================================================");
        };
    }
}
