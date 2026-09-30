

import codeAlphaHotel.model.Room;
import codeAlphaHotel.model.RoomCategory;
import codeAlphaHotel.repository.FileDataStorage;
import codeAlphaHotel.repository.GuestRepository;
import codeAlphaHotel.repository.ReservationRepository;
import codeAlphaHotel.repository.RoomRepository;
import codeAlphaHotel.repository.StorageException;
import codeAlphaHotel.service.HotelService;
import codeAlphaHotel.ui.ConsoleUI;

public class Main {

    private static final String DEFAULT_DATA_FILE = "data/hotel_data.txt";



    public static void main(String[] args) {
        String dataPath = (args.length > 0) ? args[0] : DEFAULT_DATA_FILE;
        FileDataStorage storage = new FileDataStorage(dataPath);

        RoomRepository roomRepository = new RoomRepository();
        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();

        if (!loadSavedData(storage, roomRepository, guestRepository, reservationRepository)) {
            roomRepository = new RoomRepository();
            guestRepository = new GuestRepository();
            reservationRepository = new ReservationRepository();
            seedRooms(roomRepository);
            try {
                storage.save(roomRepository.findAll(), guestRepository.findAll(), reservationRepository.findAll());
            } catch (StorageException e) {
                System.out.println("Warning: progress cannot be saved. " + e.getMessage());
            }
        }

        HotelService hotelService = new HotelService(roomRepository, guestRepository, reservationRepository, storage);
        ConsoleUI ui = new ConsoleUI(hotelService);
        ui.run();
    }

    private static boolean loadSavedData(FileDataStorage storage, RoomRepository rooms, GuestRepository guests,
                                         ReservationRepository reservations) {
        if (!storage.exists()) {
            System.out.println("No saved data found - setting up a new hotel.");
            return false;
        }
        try {
            storage.load(rooms, guests, reservations);
            if (rooms.findAll().isEmpty()) {
                throw new StorageException("File has no room data.");
            }
            System.out.println("Loaded saved data from " + storage.describeLocation());
            return true;
        } catch (StorageException e) {
            System.out.println("Could not use saved data: " + e.getMessage());
            try {
                System.out.println("Your old file was kept as: " + storage.backupCorruptFile());
            } catch (StorageException backupError) {
                System.out.println(backupError.getMessage());
            }
            System.out.println("Setting up a new hotel instead.");
            return false;
        }
    }

    private static void seedRooms(RoomRepository rooms) {
        rooms.addRoom(new Room("101", RoomCategory.STANDARD, 80.00));
        rooms.addRoom(new Room("102", RoomCategory.STANDARD, 80.00));
        rooms.addRoom(new Room("103", RoomCategory.STANDARD, 85.00));
        rooms.addRoom(new Room("201", RoomCategory.DELUXE, 150.00));
        rooms.addRoom(new Room("202", RoomCategory.DELUXE, 160.00));
        rooms.addRoom(new Room("301", RoomCategory.SUITE, 300.00));
        rooms.addRoom(new Room("302", RoomCategory.SUITE, 320.00));
    }
}
