package model;

import java.time.LocalDate;

import kivetel.HibasPinKivetel;
import kivetel.KartyaLetiltvaKivetel;

/**
 * A bank card: card number, PIN, the account behind it.
 * After {@value #MAX_HIBAS_PIN} wrong PINs in a row the card is blocked.
 * It also remembers how much was withdrawn today (for the daily limit).
 */
public class Kartya {

    public static final int MAX_HIBAS_PIN = 3;

    private final String kartyaszam;
    private final String pin;
    private final Szamla szamla;

    private int hibasProbalkozasok = 0;
    private boolean letiltva = false;

    private LocalDate felvetelNapja = LocalDate.now();
    private int maFelvett = 0;

    public Kartya(String kartyaszam, String pin, Szamla szamla) {
        if (kartyaszam == null || kartyaszam.isBlank()) {
            throw new IllegalArgumentException("A kártyaszám nem lehet üres.");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("A PIN pontosan 4 számjegy.");
        }
        if (szamla == null) {
            throw new IllegalArgumentException("A kártyához számla kell.");
        }
        this.kartyaszam = kartyaszam;
        this.pin = pin;
        this.szamla = szamla;
    }

    /**
     * Checks the PIN. A good PIN resets the wrong-try counter.
     *
     * @throws KartyaLetiltvaKivetel if the card is (or now becomes) blocked
     * @throws HibasPinKivetel       if the PIN is wrong but there are tries left
     */
    public void ellenorizPin(String beirtPin) throws HibasPinKivetel, KartyaLetiltvaKivetel {
        if (letiltva) {
            throw new KartyaLetiltvaKivetel(kartyaszam);
        }
        if (pin.equals(beirtPin)) {
            hibasProbalkozasok = 0;
            return;
        }
        hibasProbalkozasok++;
        if (hibasProbalkozasok >= MAX_HIBAS_PIN) {
            letiltva = true;
            throw new KartyaLetiltvaKivetel(kartyaszam);
        }
        throw new HibasPinKivetel(MAX_HIBAS_PIN - hibasProbalkozasok);
    }

    /** How much was withdrawn with this card today (a new day starts from 0). */
    public int getMaFelvett() {
        ujNapEllenorzes();
        return maFelvett;
    }

    /** Only the ATM calls it (package-private), after a successful withdrawal. */
    void hozzaadMaiFelvetel(int osszeg) {
        ujNapEllenorzes();
        maFelvett += osszeg;
    }

    private void ujNapEllenorzes() {
        LocalDate ma = LocalDate.now();
        if (!ma.equals(felvetelNapja)) {
            felvetelNapja = ma;
            maFelvett = 0;
        }
    }

    public String getKartyaszam() {
        return kartyaszam;
    }

    public Szamla getSzamla() {
        return szamla;
    }

    public boolean isLetiltva() {
        return letiltva;
    }
}
