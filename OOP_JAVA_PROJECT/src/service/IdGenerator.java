package service;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

/** Tracks successful IDs; a cancelled form never consumes the proposed ID.
 * This console application runs in one process. Counters are saved on Save/Exit.
 */
public final class IdGenerator {
    private static final Map<String, BigInteger> highest = new HashMap<>();
    private static final String[] PREFIXES = {"DOC-", "PNT-", "APT-", "SCH-"};
    private IdGenerator() { }

    // Legacy IDs such as SCH-511-MON remain valid but are not numeric sequences.
    public static synchronized void observe(String id) {
        if (id == null) return;
        String value = id.trim().toUpperCase(Locale.ROOT);
        for (String prefix : PREFIXES) {
            if (value.startsWith(prefix) && value.substring(prefix.length()).matches("[0-9]+")) {
                BigInteger number = new BigInteger(value.substring(prefix.length()));
                highest.merge(prefix, number, BigInteger::max);
            }
        }
    }

    /** Returns a preview; observe() commits it only after a successful insertion. */
    public static synchronized String next(String prefix) {
        if (!Arrays.asList(PREFIXES).contains(prefix)) throw new IllegalArgumentException("Unknown ID prefix.");
        String number = highest.getOrDefault(prefix, BigInteger.ZERO).add(BigInteger.ONE).toString();
        return prefix + "0".repeat(Math.max(0, 3 - number.length())) + number;
    }

    // Preserve high-water marks even if the largest record was later deleted.
    public static synchronized void load() {
        Path file = Path.of("id_sequences.txt");
        if (!Files.exists(file)) return;
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            data.load(reader);
            for (String prefix : PREFIXES) {
                String number = data.getProperty(prefix);
                if (number != null) {
                    if (number.trim().matches("[0-9]+")) observe(prefix + number.trim());
                    else System.out.println("Ignoring invalid saved counter for " + prefix);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error loading ID counters: " + e.getMessage());
        }
    }

    public static synchronized void save() {
        Properties data = new Properties();
        for (String prefix : PREFIXES) data.setProperty(prefix, highest.getOrDefault(prefix, BigInteger.ZERO).toString());
        Path temporary = Path.of("id_sequences.txt.tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) { data.store(writer, "Last successfully used numeric IDs"); }
            try { Files.move(temporary, Path.of("id_sequences.txt"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, Path.of("id_sequences.txt"), StandardCopyOption.REPLACE_EXISTING); }
            System.out.println("ID counters saved successfully.");
        } catch (IOException e) { System.out.println("Error saving ID counters: " + e.getMessage()); }
    }
}
