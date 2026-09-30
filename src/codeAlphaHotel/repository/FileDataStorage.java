package codeAlphaHotel.repository;

import codeAlphaHotel.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileDataStorage implements DataStorage {


    private static final String HEADER = "# CodeAlpha Hotel Reservation System data file (v1)";
    private static final String SEP = "|";

    private final Path file;

    public FileDataStorage(String path) {
        this.file = Paths.get(path);
    }

    @Override
    public boolean exists() {
        return Files.isRegularFile(file);
    }

    @Override
    public String describeLocation() {
        return file.toAbsolutePath().toString();
    }



    @Override
    public void save(Collection<Room> rooms, Collection<Guest> guests, Collection<Reservation> reservations) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Room r : rooms) {
            lines.add(String.join(SEP, "ROOM", field(r.getNumber()), r.getCategory().name(),
                    Double.toString(r.getPricePreNight())));
        }
        for (Guest g : guests) {
            lines.add(String.join(SEP, "GUEST", field(g.getId()), field(g.getName()), field(g.getPhone())));
        }
        for (Reservation res : reservations) {
            lines.add(String.join(SEP, "RESERVATION", field(res.getId()), field(res.getGuestId()),
                    field(res.getRoomNumber()), res.getCheckIn().toString(), res.getCheckOut().toString(),
                    Double.toString(res.getTotalPrice()), res.getPaymentStatus().name(), res.getStatus().name(),
                    res.getCreatedAt().toString()));
        }

        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Could not write data file '" + file + "': " + e.getMessage(), e);
        }
    }

    private static String field(String value) {
        if (value.contains(SEP) || value.contains("\n") || value.contains("\r")) {
            throw new StorageException("Cannot save a value containing '|' or a line break: " + value);
        }
        return value;
    }



    @Override
    public void load(RoomRepository roomRepository, GuestRepository guestRepository,
                     ReservationRepository reservationRepository) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not read data file '" + file + "': " + e.getMessage(), e);
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int lineNo = i + 1;
            String[] p = line.split("\\|", -1);

            try {
                switch (p[0]) {
                    case "ROOM" -> {
                        expectFields(p, 4, lineNo);
                        double price = Double.parseDouble(p[3]);
                        requirePositive(price, "price", lineNo);
                        roomRepository.addRoom(new Room(p[1], RoomCategory.valueOf(p[2]), price));
                    }
                    case "GUEST" -> {
                        expectFields(p, 4, lineNo);
                        guestRepository.save(new Guest(p[1], p[2], p[3]));
                    }
                    case "RESERVATION" -> {
                        // Tag + id, guestId, roomNumber, checkIn, checkOut, totalPrice, paymentStatus, status, createdAt = 10 tokens
                        expectFields(p, 10, lineNo);
                        LocalDate checkIn = LocalDate.parse(p[4]);
                        LocalDate checkOut = LocalDate.parse(p[5]);
                        if (!checkOut.isAfter(checkIn)) {
                            throw new StorageException("Line " + lineNo + ": check-out must be after check-in.");
                        }
                        double totalPrice = Double.parseDouble(p[6]);
                        requirePositive(totalPrice, "total price", lineNo);
                        PaymentStatus paymentStatus = PaymentStatus.valueOf(p[7]);
                        ReservationStatus status = ReservationStatus.valueOf(p[8]);
                        LocalDateTime createdAt = LocalDateTime.parse(p[9]);
                        requireGuest(guestRepository, p[2], lineNo);
                        requireRoom(roomRepository, p[3], lineNo);
                        reservationRepository.save(new Reservation(p[1], p[2], p[3], checkIn, checkOut,
                                totalPrice, paymentStatus, status, createdAt));
                    }
                    default -> throw new StorageException("Line " + lineNo + ": unknown record type '" + p[0] + "'.");
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw new StorageException("Line " + lineNo + ": invalid value (" + e.getMessage() + ").", e);
            }
        }
    }

    private static void expectFields(String[] parts, int expected, int lineNo) {
        if (parts.length != expected) {
            throw new StorageException("Line " + lineNo + ": expected " + expected
                    + " fields but found " + parts.length + ".");
        }
    }

    private static void requirePositive(double value, String what, int lineNo) {
        if (!(value > 0)) {
            throw new StorageException("Line " + lineNo + ": " + what + " must be greater than zero.");
        }
    }

    private static Guest requireGuest(GuestRepository guestRepository, String guestId, int lineNo) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new StorageException("Line " + lineNo + ": refers to unknown guest '" + guestId + "'."));
    }

    private static Room requireRoom(RoomRepository roomRepository, String roomNumber, int lineNo) {
        return roomRepository.findByNumber(roomNumber)
                .orElseThrow(() -> new StorageException("Line " + lineNo + ": refers to unknown room '" + roomNumber + "'."));
    }

    public Path backupCorruptFile() {
        Path backup = file.resolveSibling(file.getFileName() + ".corrupt");
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            return backup;
        } catch (IOException e) {
            throw new StorageException("Could not back up unreadable data file: " + e.getMessage(), e);
        }
    }
}






