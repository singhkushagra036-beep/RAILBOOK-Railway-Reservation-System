package com.railbook.railbook.service;

import com.railbook.railbook.model.Booking;

public interface PdfTicketService {
    byte[] generateTicketPdf(Booking booking);
}
