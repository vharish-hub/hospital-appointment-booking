-- Medicare Hospital Appointment Booking System - Seed Data

-- 1. Insert Departments
INSERT INTO departments (department_name, description) VALUES
('Cardiology', 'Expert care for your heart and cardiovascular system, addressing hypertension, coronary disease, and heart failure.'),
('Neurology', 'Diagnosis and treatment of disorders affecting the brain, spinal cord, nerves, and muscles.'),
('Orthopedics', 'Comprehensive care for bones, joints, ligaments, tendons, and muscles, including joint replacements and sports medicine.'),
('Pediatrics', 'Specialized medical care for infants, children, and adolescents, covering growth, development, and immunizations.'),
('Dermatology', 'Advanced treatments for skin, hair, and nail conditions, ranging from acne management to skin cancer screening.'),
('General Medicine', 'Primary healthcare services including preventive checkups, chronic disease management, and general wellness.'),
('ENT', 'Comprehensive diagnostics and treatment for diseases of the ear, nose, throat, and related structures.'),
('Oncology', 'Compassionate, cutting-edge cancer care, including diagnosis, chemotherapy, and multidisciplinary treatment planning.');

-- 2. Insert Users
-- Passwords are BCrypt encrypted for "password"
INSERT INTO users (full_name, email, phone, gender, dob, address, password, role) VALUES
-- Admin
('System Admin', 'admin@medicare.com', '+1234567890', 'Male', '1985-06-15', '123 Health Ave, Medical City', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'ADMIN'),

-- Doctors
('Dr. Mithilesh', 'mithilesh@medicare.com', '+919876543210', 'Male', '1980-04-12', '456 Heartbeat Way, Cardiology Dept', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'DOCTOR'),
('Dr. Hemanth', 'hemanth@medicare.com', '+919876543211', 'Male', '1975-07-22', '789 Synapse Blvd, Neurology Dept', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'DOCTOR'),
('Dr. Rahul', 'rahul@medicare.com', '+919876543212', 'Male', '1978-11-18', '101 Bone Alley, Orthopedics Dept', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'DOCTOR'),

-- Patients
('John Doe', 'john.doe@gmail.com', '+1987654321', 'Male', '1990-10-10', '12 Main Street, Springfield', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'PATIENT'),
('Jane Smith', 'jane.smith@gmail.com', '+1987654322', 'Female', '1995-02-28', '45 Elm Street, Riverdale', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'PATIENT'),
('Bruce Wayne', 'bruce.wayne@gmail.com', '+1987654323', 'Male', '1988-03-30', '1007 Mountain Drive, Gotham', '$2a$10$8.UnVuG9HHgffUDAlk8GPuRyyRIVJbKx6d.2.2aK5J8LpU.t8GzKu', 'PATIENT');

-- 3. Insert Doctors Details
-- Department IDs: 1-Cardiology, 2-Neurology, 3-Orthopedics, 4-Pediatrics, 5-Dermatology, 6-General Medicine, 7-ENT, 8-Oncology
INSERT INTO doctors (email, name, qualification, specialization, department_id, experience, consultation_fee, availability, profile_image) VALUES
('mithilesh@medicare.com', 'Dr. Mithilesh', 'MD, FACC', 'Coronary Artery Disease', 1, 15, 500.00, 'Monday, Wednesday, Friday (09:00 AM - 04:00 PM)', '/images/mithilesh.jpeg'),
('hemanth@medicare.com', 'Dr. Hemanth', 'MD, DM', 'Cognitive Neurological Disorders', 2, 22, 800.00, 'Tuesday, Thursday (10:00 AM - 05:00 PM)', '/images/hemant.jpeg'),
('rahul@medicare.com', 'Dr. Rahul', 'MD, MS', 'Spine and Joint Reconstruction', 3, 18, 1000.00, 'Monday, Tuesday, Thursday (09:00 AM - 03:00 PM)', '/images/rahul.jpeg');

-- 4. Insert Appointments
-- Status: PENDING, CONFIRMED, COMPLETED, CANCELLED
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, token_number) VALUES
(4, 1, '2026-06-24', '09:00 AM', 'COMPLETED', 1),
(5, 1, '2026-06-24', '09:30 AM', 'CONFIRMED', 2),
(6, 2, '2026-06-24', '10:00 AM', 'CONFIRMED', 1),
(4, 3, '2026-06-25', '11:00 AM', 'CONFIRMED', 1),
(5, 3, '2026-06-25', '01:30 PM', 'PENDING', 1),
(6, 1, '2026-06-26', '10:30 AM', 'PENDING', 1),
(4, 2, '2026-06-15', '10:30 AM', 'COMPLETED', 1),
(5, 3, '2026-06-10', '09:00 AM', 'CANCELLED', 2);

-- 5. Insert Reviews
INSERT INTO reviews (patient_id, doctor_id, rating, comment) VALUES
(4, 1, 5, 'Dr. Mithilesh was extremely attentive and helped diagnose my heart condition early. Excellent bedside manner!'),
(5, 1, 4, 'Very knowledgeable doctor, although the wait time at the clinic was a bit long.'),
(6, 2, 5, 'Dr. Hemanth is brilliant. He took the time to explain my neurological symptoms in detail.'),
(4, 3, 5, 'Incredible surgeon. My knee feels better than ever after the joint reconstruction. Highly recommend Dr. Rahul!');
