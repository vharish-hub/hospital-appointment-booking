package com.medicare.repository;

import com.medicare.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    void deleteByDoctorId(Long doctorId);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status IN :statuses")
    List<Appointment> findByDoctorIdAndDateAndStatusIn(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, @Param("statuses") Collection<String> statuses);
    
    List<Appointment> findByAppointmentDate(LocalDate date);
    
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(Long doctorId, LocalDate date, String appointmentTime, String excludedStatus);
    
    Long countByStatus(String status);
    Long countByAppointmentDate(LocalDate date);

    @Query("SELECT SUM(a.doctor.consultationFee) FROM Appointment a WHERE a.status = 'COMPLETED'")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE MONTH(a.appointmentDate) = :month AND YEAR(a.appointmentDate) = :year")
    Long countByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(a.doctor.consultationFee), 0.0) FROM Appointment a WHERE a.status = 'COMPLETED' AND MONTH(a.appointmentDate) = :month AND YEAR(a.appointmentDate) = :year")
    Double calculateRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT a.doctor.department.departmentName, COUNT(a) FROM Appointment a GROUP BY a.doctor.department.departmentName")
    List<Object[]> countAppointmentsByDepartment();
}
