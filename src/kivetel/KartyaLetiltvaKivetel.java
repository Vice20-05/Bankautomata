package kivetel;

/** The card is blocked (3 wrong PINs). */
public class KartyaLetiltvaKivetel extends AtmKivetel {

    private final String kartyaszam;

    public KartyaLetiltvaKivetel(String kartyaszam) {
        super("A(z) " + kartyaszam + " kártya le van tiltva. Fordulj a bankodhoz.");
        this.kartyaszam = kartyaszam;
    }

    public String getKartyaszam() {
        return kartyaszam;
    }
}
