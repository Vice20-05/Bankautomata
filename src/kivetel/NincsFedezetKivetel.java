package kivetel;

/** Not enough money on the account. Carries the missing amount. */
public class NincsFedezetKivetel extends AtmKivetel {

    private final int hianyzoOsszeg;

    public NincsFedezetKivetel(int hianyzoOsszeg) {
        super("Nincs elég fedezet a számlán, " + hianyzoOsszeg + " lej hiányzik.");
        this.hianyzoOsszeg = hianyzoOsszeg;
    }

    public int getHianyzoOsszeg() {
        return hianyzoOsszeg;
    }
}
