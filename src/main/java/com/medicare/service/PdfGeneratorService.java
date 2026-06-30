package com.medicare.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medicare.model.Appointment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

    public ByteArrayInputStream generateAppointmentSlip(Appointment appointment) {
        Document document = new Document(PageSize.A6);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font tokenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);

            // Title
            Paragraph title = new Paragraph("MEDICARE HOSPITAL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Appointment Confirmation Slip", FontFactory.getFont(FontFactory.HELVETICA, 8));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph("--------------------------------------------------"));

            // Token Block
            Paragraph tokenLabel = new Paragraph("YOUR TOKEN NUMBER", FontFactory.getFont(FontFactory.HELVETICA, 8));
            tokenLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(tokenLabel);

            Paragraph token = new Paragraph(String.valueOf(appointment.getTokenNumber()), tokenFont);
            token.setAlignment(Element.ALIGN_CENTER);
            document.add(token);

            document.add(new Paragraph(" "));

            // Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableCell(table, "Appointment ID:", headerFont);
            addTableCell(table, "#" + appointment.getId(), bodyFont);

            addTableCell(table, "Patient Name:", headerFont);
            addTableCell(table, appointment.getPatient().getFullName(), bodyFont);

            addTableCell(table, "Doctor Name:", headerFont);
            addTableCell(table, appointment.getDoctor().getName(), bodyFont);

            addTableCell(table, "Department:", headerFont);
            addTableCell(table, appointment.getDoctor().getDepartment().getDepartmentName(), bodyFont);

            addTableCell(table, "Date & Time:", headerFont);
            addTableCell(table, appointment.getAppointmentDate().toString() + " @ " + appointment.getAppointmentTime(), bodyFont);

            addTableCell(table, "Consultation Fee:", headerFont);
            addTableCell(table, "Rs. " + String.format("%.2f", appointment.getDoctor().getConsultationFee()), bodyFont);

            addTableCell(table, "Status:", headerFont);
            addTableCell(table, appointment.getStatus(), bodyFont);

            document.add(table);

            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Please bring this slip and arrive 15 minutes early.\nThank you!", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateAppointmentsPdfReport(List<Appointment> appointments, String reportTitle) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("MediCare Appointment Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(reportTitle, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.0f, 2.0f, 1.8f, 1.5f, 1.5f, 1.2f});

            addTableHeader(table, "App ID", tableHeaderFont);
            addTableHeader(table, "Patient", tableHeaderFont);
            addTableHeader(table, "Doctor", tableHeaderFont);
            addTableHeader(table, "Department", tableHeaderFont);
            addTableHeader(table, "Date", tableHeaderFont);
            addTableHeader(table, "Time Slot", tableHeaderFont);
            addTableHeader(table, "Status", tableHeaderFont);

            double totalRevenue = 0.0;
            for (Appointment app : appointments) {
                table.addCell(new Phrase("#" + app.getId(), tableBodyFont));
                table.addCell(new Phrase(app.getPatient().getFullName(), tableBodyFont));
                table.addCell(new Phrase(app.getDoctor().getName(), tableBodyFont));
                table.addCell(new Phrase(app.getDoctor().getDepartment().getDepartmentName(), tableBodyFont));
                table.addCell(new Phrase(app.getAppointmentDate().toString(), tableBodyFont));
                table.addCell(new Phrase(app.getAppointmentTime(), tableBodyFont));
                table.addCell(new Phrase(app.getStatus(), tableBodyFont));
                
                if ("COMPLETED".equals(app.getStatus())) {
                    totalRevenue += app.getDoctor().getConsultationFee();
                }
            }

            document.add(table);

            document.add(new Paragraph(" "));
            Paragraph summary = new Paragraph(String.format("Total Appointments: %d  |  Revenue from Completed Bookings: Rs. %.2f",
                    appointments.size(), totalRevenue), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
            summary.setAlignment(Element.ALIGN_RIGHT);
            document.add(summary);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateAppointmentsExcelReport(List<Appointment> appointments) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Appointments");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Patient Name", "Doctor Name", "Department", "Date", "Time", "Status", "Fee (Rs.)"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Appointment app : appointments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(app.getId());
                row.createCell(1).setCellValue(app.getPatient().getFullName());
                row.createCell(2).setCellValue(app.getDoctor().getName());
                row.createCell(3).setCellValue(app.getDoctor().getDepartment().getDepartmentName());
                row.createCell(4).setCellValue(app.getAppointmentDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                row.createCell(5).setCellValue(app.getAppointmentTime());
                row.createCell(6).setCellValue(app.getStatus());
                row.createCell(7).setCellValue(app.getDoctor().getConsultationFee());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e.getMessage());
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new java.awt.Color(200, 220, 255));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
