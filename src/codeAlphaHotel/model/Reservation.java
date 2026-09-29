package codeAlphaHotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private final String id;
    private final String guestId;
    private final String roomNumber;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double totalPrice;
    private final LocalDateTime createdAt;
    private PaymentStatus paymentStatus;
    private ReservationStatus status;

    public Reservation(String id, String guestId, String roomNumber, LocalDate checkIn, LocalDate checkOut,
                       double totalPrice, PaymentStatus paymentStatus, ReservationStatus status,
                       LocalDateTime createdAt){
        this.id = id;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.createdAt = createdAt;


    }
    public String getId() {

        return id;
    }

    public String getGuestId() {

        return guestId;
    }

    public String getRoomNumber() {

        return roomNumber;
    }

    public LocalDate getCheckIn() {

        return checkIn;
    }

    public LocalDate getCheckOut() {

        return checkOut;
    }

    public long getNights() {

        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double getTotalPrice() {

        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    public PaymentStatus getPaymentStatus() {

        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {

        this.paymentStatus = paymentStatus;
    }

    public ReservationStatus getStatus() {

        return status;
    }

    public void cancel() {

        this.status = ReservationStatus.CANCELLED;
    }
    public boolean overlaps(LocalDate otherCheckIn, LocalDate otherCheckOut) {
        return checkIn.isBefore(otherCheckOut) && otherCheckIn.isBefore(checkOut);
    }
}

