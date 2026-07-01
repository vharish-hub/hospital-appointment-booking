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
    public CommandLineRunner bootstrapData(
            UserRepository userRepository,
            com.medicare.repository.DepartmentRepository departmentRepository,
            com.medicare.repository.DoctorRepository doctorRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=================================================");
            System.out.println("[BOOTSTRAP] RUNNING DATABASE WORKER...");
            System.out.println("=================================================");

            // 1. Seed Departments if empty
            if (departmentRepository.count() == 0) {
                com.medicare.model.Department cardiology = new com.medicare.model.Department();
                cardiology.setDepartmentName("Cardiology");
                cardiology.setDescription("Heart and cardiovascular care services.");
                departmentRepository.save(cardiology);

                com.medicare.model.Department neurology = new com.medicare.model.Department();
                neurology.setDepartmentName("Neurology");
                neurology.setDescription("Brain, spinal cord, and nerve disorders.");
                departmentRepository.save(neurology);

                com.medicare.model.Department orthopedics = new com.medicare.model.Department();
                orthopedics.setDepartmentName("Orthopedics");
                orthopedics.setDescription("Bone, joint, and musculoskeletal care.");
                departmentRepository.save(orthopedics);

                System.out.println("[BOOTSTRAP] Seeded default departments: Cardiology, Neurology, Orthopedics");
            }

            // 2. Ensure default departments are fetched for seeding doctors
            com.medicare.model.Department cardiology = departmentRepository.findAll().stream()
                    .filter(d -> d.getDepartmentName().equalsIgnoreCase("Cardiology"))
                    .findFirst().orElse(null);
            com.medicare.model.Department neurology = departmentRepository.findAll().stream()
                    .filter(d -> d.getDepartmentName().equalsIgnoreCase("Neurology"))
                    .findFirst().orElse(null);
            com.medicare.model.Department orthopedics = departmentRepository.findAll().stream()
                    .filter(d -> d.getDepartmentName().equalsIgnoreCase("Orthopedics"))
                    .findFirst().orElse(null);

            // 3. Seed Doctor profiles if empty
            if (doctorRepository.count() == 0) {
                // Dr. Mithilesh
                com.medicare.model.Doctor mithilesh = new com.medicare.model.Doctor();
                mithilesh.setName("Dr. Mithilesh");
                mithilesh.setEmail("mithilesh@medicare.com");
                mithilesh.setQualification("MD, FACC");
                mithilesh.setSpecialization("Interventional Cardiology");
                mithilesh.setExperience(12);
                mithilesh.setConsultationFee(500.0);
                mithilesh.setAvailability("Monday, Wednesday, Friday (09:00 AM - 04:00 PM)");
                mithilesh.setProfileImage("/images/mithilesh.jpeg");
                mithilesh.setDepartment(cardiology);
                doctorRepository.save(mithilesh);

                // Dr. Hemanth
                com.medicare.model.Doctor hemanth = new com.medicare.model.Doctor();
                hemanth.setName("Dr. Hemanth");
                hemanth.setEmail("hemanth@medicare.com");
                hemanth.setQualification("MD, DM (Neurology)");
                hemanth.setSpecialization("Stroke and Epilepsy Management");
                hemanth.setExperience(10);
                hemanth.setConsultationFee(500.0);
                hemanth.setAvailability("Tuesday, Thursday (10:00 AM - 05:00 PM)");
                hemanth.setProfileImage("/images/hemant.jpeg");
                hemanth.setDepartment(neurology);
                doctorRepository.save(hemanth);

                // Dr. Rahul
                com.medicare.model.Doctor rahul = new com.medicare.model.Doctor();
                rahul.setName("Dr. Rahul");
                rahul.setEmail("rahul@medicare.com");
                rahul.setQualification("MS (Ortho), MCh");
                rahul.setSpecialization("Joint Replacement Surgery");
                rahul.setExperience(8);
                rahul.setConsultationFee(500.0);
                rahul.setAvailability("Wednesday, Friday (09:00 AM - 02:00 PM)");
                rahul.setProfileImage("/images/rahul.jpeg");
                rahul.setDepartment(orthopedics);
                doctorRepository.save(rahul);

                System.out.println("[BOOTSTRAP] Seeded default doctors");
            }

            // 4. Ensure Admin exists and has correct password hash
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

            // 5. Ensure default patient exists and has correct password hash
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

            // 6. Ensure doctor logins exist and have correct password hashes
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
