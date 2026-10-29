package model;

import kivetel.NincsFedezetKivetel;

/**
 * A bank account. The balance is private and it can ONLY change through
 * {@link #befizet(int)} and {@link #kivesz(int)} (encapsulation).
 * <p>
 * Amounts are whole lei (int), an ATM has no coins anyway.
 */
public class Szamla {

    private final String tulajdonos;
    private int egyenleg;

    public Szamla(String tulajdonos, int kezdoEgyenleg) {
        if (tulajdonos == null || tulajdonos.isBlank()) {
            throw new IllegalArgumentException("A tulajdonos neve nem lehet üres.");
        }
        if (kezdoEgyenleg < 0) {
            throw new IllegalArgumentException("A kezdő egyenleg nem lehet negatív.");
        }
        this.tulajdonos = tulajdonos;
        this.egyenleg = kezdoEgyenleg;
    }

    /**
     * Puts money on the account.
     * A 0 or negative amount is a programmer error -> unchecked IllegalArgumentException.
     */
    public void befizet(int osszeg) {
        ellenorizOsszeg(osszeg);
        egyenleg += osszeg;
    }

    /**
     * Takes money from the account.
     *
     * @throws NincsFedezetKivetel if the balance is too small (checked: the user can ask for less)
     */
    public void kivesz(int osszeg) throws NincsFedezetKivetel {
        ellenorizOsszeg(osszeg);
        if (osszeg > egyenleg) {
            throw new NincsFedezetKivetel(osszeg - egyenleg);
        }
        egyenleg -= osszeg;
    }

    private void ellenorizOsszeg(int osszeg) {
        if (osszeg <= 0) {
            throw new IllegalArgumentException("Az összegnek pozitívnak kell lennie (kapott: " + osszeg + ").");
        }
    }

    public int getEgyenleg() {
        return egyenleg;
    }

    public String getTulajdonos() {
        return tulajdonos;
    }
}
