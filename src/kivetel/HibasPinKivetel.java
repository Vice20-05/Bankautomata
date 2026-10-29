package kivetel;

/** Wrong PIN. Carries how many tries are left before the card is blocked. */
public class HibasPinKivetel extends AtmKivetel {

    private final int maradekProbalkozas;

    public HibasPinKivetel(int maradekProbalkozas) {
        super("Hibás PIN kód. Még " + maradekProbalkozas + " próbálkozásod maradt.");
        this.maradekProbalkozas = maradekProbalkozas;
    }

    public int getMaradekProbalkozas() {
        return maradekProbalkozas;
    }
}
