package Models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * One rental contract. The fee is ALWAYS computed from the rental duration:
 * fee = days x (daily rate of the motorcycle + add-ons per day)
 */
public class Rental {

    public static final double WAIVER_RATE = 50;   // Full Collision Waiver / day
    public static final double GEAR_RATE   = 30;   // ECE Helmet & Jacket / day
    public static final double GPS_RATE    = 20;   // Rugged GPS / Panniers / day
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final String unit, customerName, phone, license;
    private final double dailyRate;
    private final LocalDate pickUpDate, returnDate;
    private final boolean waiver, gear, gps;
    private boolean paid;

    public Rental(String unit, double dailyRate, String customerName, String phone, String license,
                  LocalDate pickUpDate, LocalDate returnDate,
                  boolean waiver, boolean gear, boolean gps, boolean paid) {
        this.unit = clean(unit);
        this.dailyRate = dailyRate;
        this.customerName = clean(customerName);
        this.phone = clean(phone);
        this.license = clean(license);
        this.pickUpDate = pickUpDate;
        this.returnDate = returnDate;
        this.waiver = waiver;
        this.gear = gear;
        this.gps = gps;
        this.paid = paid;
    }

    // "|" is the separator of the save file, so it must never appear inside the data
    private static String clean(String s) {
        return s == null ? "" : s.replace("|", " ").trim();
    }

    // ---------- calculations ----------

    /** Number of rental days. Same-day return counts as 1 day. */
    public static int countDays(LocalDate from, LocalDate to) {
        return (int) Math.max(1, ChronoUnit.DAYS.between(from, to));
    }

    public static double addOnsPerDay(boolean waiver, boolean gear, boolean gps) {
        return (waiver ? WAIVER_RATE : 0) + (gear ? GEAR_RATE : 0) + (gps ? GPS_RATE : 0);
    }

    public int getDays() {
        return countDays(pickUpDate, returnDate);
    }

    public double getFee() {
        return getDays() * (dailyRate + addOnsPerDay(waiver, gear, gps));
    }

    // ---------- getters ----------

    public String getUnit()          { return unit; }
    public double getDailyRate()     { return dailyRate; }
    public String getCustomerName()  { return customerName; }
    public String getPhone()         { return phone; }
    public String getLicense()       { return license; }
    public LocalDate getPickUpDate() { return pickUpDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean hasWaiver()       { return waiver; }
    public boolean hasGear()         { return gear; }
    public boolean hasGps()          { return gps; }
    public boolean isPaid()          { return paid; }
    public void markPaid()           { this.paid = true; }

    // ---------- save / load ----------

    public String toLine() {
        return String.join("|", unit, String.valueOf(dailyRate), customerName, phone, license,
                pickUpDate.toString(), returnDate.toString(),
                String.valueOf(waiver), String.valueOf(gear), String.valueOf(gps), String.valueOf(paid));
    }

    /** Returns null when the line is damaged, so one bad line never breaks the whole file. */
    public static Rental fromLine(String line) {
        try {
            String[] p = line.split("\\|", -1);
            if (p.length != 11) return null;
            return new Rental(p[0], Double.parseDouble(p[1]), p[2], p[3], p[4],
                    LocalDate.parse(p[5]), LocalDate.parse(p[6]),
                    Boolean.parseBoolean(p[7]), Boolean.parseBoolean(p[8]),
                    Boolean.parseBoolean(p[9]), Boolean.parseBoolean(p[10]));
        } catch (RuntimeException e) {
            return null;
        }
    }
}