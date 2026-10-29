package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes every transaction into a text file (one line each, appended).
 * <p>
 * If writing fails (no permission, full disk...), it only prints a warning:
 * the customer already got the money, a log problem must not stop the payout.
 */
public class Naplo {

    private static final DateTimeFormatter IDO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String fajlnev;

    public Naplo(String fajlnev) {
        this.fajlnev = fajlnev;
    }

    public void naploz(String esemeny) {
        // try-with-resources: the writer is closed automatically, even if write() throws
        try (BufferedWriter iro = new BufferedWriter(new FileWriter(fajlnev, true))) {
            iro.write(LocalDateTime.now().format(IDO) + " | " + esemeny);
            iro.newLine();
        } catch (IOException e) {
            System.err.println("[Figyelem] A naplózás nem sikerült (" + e.getMessage()
                    + "), a tranzakció ettől még érvényes.");
        }
    }

    public String getFajlnev() {
        return fajlnev;
    }
}
