package com.railbook.railbook.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.model.Booking;
import com.railbook.railbook.model.BookingStatus;
import com.railbook.railbook.model.Passenger;
import com.railbook.railbook.service.PdfTicketService;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfTicketServiceImpl implements PdfTicketService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    @Override
    public byte[] generateTicketPdf(Booking booking) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(11, 37, 69));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(11, 37, 69));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font disclaimerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(100, 116, 139));

            // Header Banner Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.addElement(new Paragraph("RAILBOOK", titleFont));
            logoCell.addElement(new Paragraph("Electronic Reservation Slip (ERS) • Valid with Original ID", subtitleFont));
            headerTable.addCell(logoCell);

            PdfPCell pnrCell = new PdfPCell();
            pnrCell.setBorder(Rectangle.NO_BORDER);
            pnrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pnrP = new Paragraph("PNR: " + booking.getPnrNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 119, 182)));
            pnrP.setAlignment(Element.ALIGN_RIGHT);
            pnrCell.addElement(pnrP);

            Color statusColor = (booking.getBookingStatus() == BookingStatus.CONFIRMED) ? new Color(22, 101, 52) : new Color(153, 27, 27);
            Paragraph statusP = new Paragraph("Status: " + booking.getBookingStatus().name(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, statusColor));
            statusP.setAlignment(Element.ALIGN_RIGHT);
            pnrCell.addElement(statusP);
            headerTable.addCell(pnrCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // Journey Details Table
            Paragraph sec1 = new Paragraph("JOURNEY DETAILS", sectionTitleFont);
            sec1.setSpacingAfter(4);
            document.add(sec1);

            PdfPTable journeyTable = new PdfPTable(4);
            journeyTable.setWidthPercentage(100);
            journeyTable.setWidths(new float[]{25, 25, 25, 25});

            addKeyValCell(journeyTable, "Train No. & Name:", booking.getTrain().getTrainNumber() + " - " + booking.getTrain().getTrainName(), bodyBoldFont, bodyFont);
            addKeyValCell(journeyTable, "Travel Class:", booking.getTravelClass().getDisplayName(), bodyBoldFont, bodyFont);
            addKeyValCell(journeyTable, "From Station:", booking.getTrain().getSourceStation().getFormattedDisplay(), bodyBoldFont, bodyFont);
            addKeyValCell(journeyTable, "To Station:", booking.getTrain().getDestinationStation().getFormattedDisplay(), bodyBoldFont, bodyFont);

            addKeyValCell(journeyTable, "Date of Journey:", booking.getJourneyDate().format(DATE_FORMATTER), bodyBoldFont, bodyFont);
            addKeyValCell(journeyTable, "Departure Time:", booking.getTrain().getDepartureTime().format(TIME_FORMATTER) + " hrs", bodyBoldFont, bodyFont);
            addKeyValCell(journeyTable, "Arrival Time:", booking.getTrain().getArrivalTime().format(TIME_FORMATTER) + " hrs", bodyBoldFont, bodyFont);
            addKeyValCell(journeyTable, "Booking Date:", booking.getBookingDate().format(DATETIME_FORMATTER), bodyBoldFont, bodyFont);

            document.add(journeyTable);
            document.add(new Paragraph(" "));

            // Passenger Details Table
            Paragraph sec2 = new Paragraph("PASSENGER DETAILS", sectionTitleFont);
            sec2.setSpacingAfter(4);
            document.add(sec2);

            PdfPTable passTable = new PdfPTable(6);
            passTable.setWidthPercentage(100);
            passTable.setWidths(new float[]{8, 32, 12, 14, 18, 16});

            Color navyHeader = new Color(19, 64, 116);
            addHeaderCell(passTable, "#", headerFont, navyHeader);
            addHeaderCell(passTable, "Passenger Name", headerFont, navyHeader);
            addHeaderCell(passTable, "Age", headerFont, navyHeader);
            addHeaderCell(passTable, "Gender", headerFont, navyHeader);
            addHeaderCell(passTable, "Seat / Coach", headerFont, navyHeader);
            addHeaderCell(passTable, "Berth Type", headerFont, navyHeader);

            int pIdx = 1;
            for (Passenger p : booking.getPassengers()) {
                addBodyCell(passTable, String.valueOf(pIdx++), bodyFont, Element.ALIGN_CENTER);
                addBodyCell(passTable, p.getFullName(), bodyBoldFont, Element.ALIGN_LEFT);
                addBodyCell(passTable, String.valueOf(p.getAge()), bodyFont, Element.ALIGN_CENTER);
                addBodyCell(passTable, p.getGender(), bodyFont, Element.ALIGN_CENTER);
                addBodyCell(passTable, p.getSeatNumber() != null ? p.getSeatNumber() : "CNF", bodyBoldFont, Element.ALIGN_CENTER);
                addBodyCell(passTable, p.getAllocatedBerth() != null ? p.getAllocatedBerth() : "Confirmed", bodyFont, Element.ALIGN_CENTER);
            }
            document.add(passTable);
            document.add(new Paragraph(" "));

            // Fare & Payment Summary
            Paragraph sec3 = new Paragraph("FARE & PAYMENT SUMMARY", sectionTitleFont);
            sec3.setSpacingAfter(4);
            document.add(sec3);

            PdfPTable fareTable = new PdfPTable(4);
            fareTable.setWidthPercentage(100);
            fareTable.setWidths(new float[]{25, 25, 25, 25});

            addKeyValCell(fareTable, "Total Fare:", "Rs. " + booking.getTotalFare().toPlainString(), bodyBoldFont, bodyFont);
            String paymentMode = (booking.getPayment() != null) ? booking.getPayment().getPaymentMode().getDisplayName() : "UPI";
            addKeyValCell(fareTable, "Payment Mode:", paymentMode, bodyBoldFont, bodyFont);

            String txnId = (booking.getPayment() != null) ? booking.getPayment().getTransactionId() : "N/A";
            addKeyValCell(fareTable, "Transaction ID:", txnId, bodyBoldFont, bodyFont);

            String paymentStatus = (booking.getPayment() != null) ? booking.getPayment().getPaymentStatus().name() : "SUCCESS";
            addKeyValCell(fareTable, "Payment Status:", paymentStatus, bodyBoldFont, bodyFont);

            document.add(fareTable);

            // Cancellation Notice if cancelled
            if (booking.getBookingStatus() == BookingStatus.CANCELLED && booking.getCancellation() != null) {
                document.add(new Paragraph(" "));
                Paragraph cancelNotice = new Paragraph(
                        "CANCELLATION DETAILS: Cancelled on " +
                        booking.getCancellation().getCancellationDate().format(DATETIME_FORMATTER) +
                        " | Refund: Rs. " + booking.getCancellation().getRefundAmount() +
                        " | Cancellation Charges: Rs. " + booking.getCancellation().getCancellationCharge() +
                        " | Status: " + booking.getCancellation().getRefundStatus(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(153, 27, 27))
                );
                document.add(cancelNotice);
            }

            document.add(new Paragraph(" "));
            Paragraph instructions = new Paragraph(
                    "IMPORTANT INSTRUCTIONS:\n" +
                    "1. This is a computer-generated demonstration reservation slip issued by RailBook.\n" +
                    "2. One of the passengers booked on an e-ticket is required to present valid original Government Photo ID proof.\n" +
                    "3. All payments and refunds demonstrated in this system are simulated for academic/demonstration purposes.\n" +
                    "4. For cancellation rules and PNR queries, visit RailBook web portal at http://localhost:8080/pnr.",
                    disclaimerFont
            );
            document.add(instructions);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BookingException("Failed to generate PDF ticket: " + e.getMessage());
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addKeyValCell(PdfPTable table, String label, String val, Font labelFont, Font valFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(4);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(val, valFont));
        cell.addElement(p);
        table.addCell(cell);
    }
}
