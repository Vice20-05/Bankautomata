package kivetel;

/**
 * The ATM can't pay out the amount: either it has too little cash, or the
 * amount can't be made from the banknotes it still has (e.g. 30 lei when only
 * 50s are left).
 */
public class NincsElegKeszpenzKivetel extends AtmKivetel {

    private final int kertOsszeg;

    public NincsElegKeszpenzKivetel(int kertOsszeg, String ok) {
        super("Az automata nem tud " + kertOsszeg + " lejt kiadni: " + ok);
        this.kertOsszeg = kertOsszeg;
    }

    public int getKertOsszeg() {
        return kertOsszeg;
    }
}
