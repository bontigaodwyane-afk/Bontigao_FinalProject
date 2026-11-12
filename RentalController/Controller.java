package RentalController;

import Models.Rental;
import Models.RentalManagement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    // EDIT THESE to match your real motorcycle list and daily rates.
    public static final String[] UNITS = {
        "Honda Click 160", "Honda ADV160", "Yamaha Aerox", "Honda Beat 110",
        "Suzuki Raider R150 Fi", "Yamaha Sniper 150", "Honda CRF"
    };
    public static final double[] RATES = {300, 450, 400, 250, 400, 400, 500};

    private final RentalManagement management = new RentalManagement();
    private boolean longestFirst = true;

    // ---------- motorcycles ----------

    public static double rateOf(String unit) {
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(unit)) return RATES[i];
        }
        return 0;
    }

    /** How many units of each motorcycle model the shop owns. */
    public static final int STOCK_PER_MODEL = 10;

    /** Units of this model that can still be rented (stock minus rentals not yet paid). */
    public int availableCount(String unit) {
        return Math.max(0, STOCK_PER_MODEL - management.rentedCount(unit));
    }

    /** True when no unit of this model is left. */
    public boolean isUnitBusy(String unit) {
        return availableCount(unit) == 0;
    }

    /** Only models that still have at least one unit left. */
    public List<String> getAvailableUnits() {
        List<String> list = new ArrayList<>();
        for (String u : UNITS) {
            if (availableCount(u) > 0) list.add(u);
        }
        return list;
    }

    // ---------- dispatch ----------

    /** Validates the form values and creates the rental. Pick up date is always TODAY. */
    public Rental dispatch(String unit, String name, String phone, String license,
                           LocalDate returnDate, boolean waiver, boolean gear, boolean gps) {
        if (unit == null) {
            throw new IllegalArgumentException("Please select a motorcycle.");
        }
        if (availableCount(unit) == 0) {
            throw new IllegalArgumentException("No more " + unit + " available - all "
                    + STOCK_PER_MODEL + " units are rented out.\n"
                    + "Mark a rental as paid in the Records tab to free one, or choose another motorcycle.");
        }
        name = name == null ? "" : name.trim();
        if (name.length() < 2) {
            throw new IllegalArgumentException("Please enter the rider's full name.");
        }
        phone = phone == null ? "" : phone.trim();
        if (!phone.matches("(09|\\+639)\\d{9}")) {
            throw new IllegalArgumentException("Enter a valid mobile number, e.g. 09123456789 or +639123456789.");
        }
        license = license == null ? "" : license.trim();
        if (license.isEmpty()) license = "N/A";

        LocalDate pickUp = LocalDate.now();
        if (returnDate == null || returnDate.isBefore(pickUp)) {
            throw new IllegalArgumentException("The return date cannot be earlier than the pick up date.");
        }

        Rental r = new Rental(unit, rateOf(unit), name, phone, license,
                pickUp, returnDate, waiver, gear, gps, false);
        management.add(r);
        management.save();
        return r;
    }

    // ---------- records ----------

    /** Sorts with bubble sort (by days rented) and returns the list shown in the table. */
    public List<Rental> getSortedRentals() {
        management.bubbleSortByDays(longestFirst);
        return management.getAll();
    }

    /** @return false when the record was already paid. */
    public boolean markPaid(int row) {
        Rental r = management.get(row);
        if (r.isPaid()) return false;
        r.markPaid();
        management.save();
        return true;
    }

    public void delete(int row) {
        management.remove(row);
        management.save();
    }

    public boolean isLongestFirst()  { return longestFirst; }
    public void toggleSortOrder()    { longestFirst = !longestFirst; }
    public int  recordCount()        { return management.size(); }
    public double totalPaid()        { return management.totalPaid(); }
    public double totalUnpaid()      { return management.totalUnpaid(); }
    public void save()               { management.save(); }
}