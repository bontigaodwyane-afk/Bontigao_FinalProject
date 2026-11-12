package Models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps all rentals, sorts them with BUBBLE SORT (by days rented),
 * and saves them to a file so the records are still there after closing the program.
 *
 * Save file:  <your user folder>/MotorcycleHubData/rentals.txt
 */
public class RentalManagement {

    private final ArrayList<Rental> rentals = new ArrayList<>();
    private final Path dataDir  = Paths.get(System.getProperty("user.home"), "MotorcycleHubData");
    private final Path dataFile = dataDir.resolve("rentals.txt");

    public RentalManagement() {
        load();
    }

    // ---------- basic operations ----------

    public void add(Rental r)      { rentals.add(r); }
    public void remove(int index)  { rentals.remove(index); }
    public Rental get(int index)   { return rentals.get(index); }
    public int size()              { return rentals.size(); }

    public List<Rental> getAll() {
        return Collections.unmodifiableList(rentals);
    }

    /** How many units of this motorcycle are rented out right now (rentals not yet marked as paid). */
    public int rentedCount(String unit) {
        int count = 0;
        for (Rental r : rentals) {
            if (r.getUnit().equals(unit) && !r.isPaid()) count++;
        }
        return count;
    }

    public double totalPaid() {
        double t = 0;
        for (Rental r : rentals) if (r.isPaid()) t += r.getFee();
        return t;
    }

    public double totalUnpaid() {
        double t = 0;
        for (Rental r : rentals) if (!r.isPaid()) t += r.getFee();
        return t;
    }

    // ---------- BUBBLE SORT by number of days rented ----------

    /**
     * @param longestFirst true  = most days first (descending)
     *                     false = fewest days first (ascending)
     */
    public void bubbleSortByDays(boolean longestFirst) {
        int n = rentals.size();
        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - pass; j++) {
                int left  = rentals.get(j).getDays();
                int right = rentals.get(j + 1).getDays();
                boolean outOfOrder = longestFirst ? left < right : left > right;
                if (outOfOrder) {
                    Collections.swap(rentals, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) break;   // already sorted, stop early
        }
    }

    // ---------- save / load ----------

    public void save() {
        try {
            Files.createDirectories(dataDir);
            List<String> lines = new ArrayList<>();
            for (Rental r : rentals) lines.add(r.toLine());
            Files.write(dataFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the records: " + e.getMessage(), e);
        }
    }

    private void load() {
        if (!Files.exists(dataFile)) return;
        try {
            for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                Rental r = Rental.fromLine(line);
                if (r != null) rentals.add(r);
            }
        } catch (IOException e) {
            System.err.println("Could not read " + dataFile + ": " + e.getMessage());
        }
    }
}