package codeAlphaHotel.model;

public class Room {
    private final String number;
    private final  RoomCategory category;
    private final double pricePreNight;


    public Room(String number, RoomCategory category, double pricePreNight) {
        this.number = number;
        this.category = category;
        this.pricePreNight = pricePreNight;
    }
    public String getNumber() {
        return number;
    }
    public RoomCategory getCategory() {
        return category;
    }
    public double getPricePreNight() {
        return pricePreNight;
    }

    @Override
    public String toString() {
        return String.format("%-6s %-10s $%.2f/night", number, category, pricePreNight);

    }


}
