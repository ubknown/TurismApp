package com.licentarazu.turismapp.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.licentarazu.turismapp.dto.ProfitReportDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfReportService {

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateProfitReportPdf(ProfitReportDTO reportData) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Profit Report - TurismApp", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Add owner information
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font regularFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            
            document.add(new Paragraph("Owner Information", headerFont));
            document.add(new Paragraph("Name: " + reportData.getOwnerName(), regularFont));
            document.add(new Paragraph("Email: " + reportData.getOwnerEmail(), regularFont));
            document.add(new Paragraph("Report Generated: " + reportData.getReportGeneratedDate().format(dateFormatter), regularFont));
            document.add(new Paragraph(" ")); // Spacing
            
            // Add summary statistics
            document.add(new Paragraph("Summary Statistics", headerFont));
            document.add(new Paragraph("Total Profit: " + currencyFormat.format(reportData.getTotalProfitRON()) + " RON", regularFont));
            document.add(new Paragraph("Total Properties: " + reportData.getTotalProperties(), regularFont));
            document.add(new Paragraph("Total Confirmed Bookings: " + reportData.getTotalConfirmedBookings(), regularFont));
            document.add(new Paragraph(" ")); // Spacing
            
            // Add monthly profits table
            if (reportData.getMonthlyProfitsRON() != null && !reportData.getMonthlyProfitsRON().isEmpty()) {
                document.add(new Paragraph("Monthly Profits", headerFont));
                
                PdfPTable monthlyTable = new PdfPTable(2);
                monthlyTable.setWidthPercentage(100);
                monthlyTable.setSpacingBefore(10);
                
                // Table headers
                monthlyTable.addCell(createHeaderCell("Month"));
                monthlyTable.addCell(createHeaderCell("Profit (RON)"));
                
                // Table data
                for (Map.Entry<String, Double> entry : reportData.getMonthlyProfitsRON().entrySet()) {
                    monthlyTable.addCell(createDataCell(entry.getKey()));
                    monthlyTable.addCell(createDataCell(currencyFormat.format(entry.getValue())));
                }
                
                document.add(monthlyTable);
                document.add(new Paragraph(" ")); // Spacing
            }
            
            // Add unit profits table
            if (reportData.getUnitProfits() != null && !reportData.getUnitProfits().isEmpty()) {
                document.add(new Paragraph("Property Performance", headerFont));
                
                PdfPTable unitTable = new PdfPTable(4);
                unitTable.setWidthPercentage(100);
                unitTable.setSpacingBefore(10);
                
                // Table headers
                unitTable.addCell(createHeaderCell("Property Name"));
                unitTable.addCell(createHeaderCell("Location"));
                unitTable.addCell(createHeaderCell("Total Profit (RON)"));
                unitTable.addCell(createHeaderCell("Confirmed Bookings"));
                
                // Table data
                for (ProfitReportDTO.UnitProfitSummary unit : reportData.getUnitProfits()) {
                    unitTable.addCell(createDataCell(unit.getUnitName()));
                    unitTable.addCell(createDataCell(unit.getLocation()));
                    unitTable.addCell(createDataCell(currencyFormat.format(unit.getTotalProfitRON())));
                    unitTable.addCell(createDataCell(unit.getConfirmedBookingsCount().toString()));
                }
                
                document.add(unitTable);
            }
            
            // Add footer
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("This report includes only confirmed and completed bookings.", 
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    private PdfPCell createHeaderCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
        cell.setBackgroundColor(new BaseColor(106, 90, 205)); // Violet color
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }
    
    private PdfPCell createDataCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, 
            new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        return cell;
    }
}
