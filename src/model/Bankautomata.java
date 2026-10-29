package model;

import java.util.Map;

import kivetel.AtmKivetel;
import kivetel.HibasPinKivetel;
import kivetel.KartyaLetiltvaKivetel;
import kivetel.NapiLimitKivetel;
import kivetel.NincsElegKeszpenzKivetel;
import kivetel.NincsFedezetKivetel;

/**
 * The ATM itself: a card goes in, the PIN is checked, then the customer can
 * check the balance, withdraw and deposit. Every step is logged.
 */
public class Bankautomata {

    private final Penztar penztar;
    private final Naplo naplo;
    private final int napiLimit;

    private Kartya aktivKartya; // null = no card in the machine

    public Bankautomata(Penztar penztar, Naplo naplo, int napiLimit) {
        if (penztar == null || naplo == null) {
            throw new IllegalArgumentException("A pénztár és a napló kötelező.");
        }
        if (napiLimit <= 0) {
            throw new IllegalArgumentException("A napi limitnek pozitívnak kell lennie.");
        }
        this.penztar = penztar;
        this.naplo = naplo;
        this.napiLimit = napiLimit;
    }

    /** Card in + PIN. Only after a good PIN does the card become active. */
    public void kartyaBehelyez(Kartya kartya, String pin) throws HibasPinKivetel, KartyaLetiltvaKivetel {
        try {
            kartya.ellenorizPin(pin);
            aktivKartya = kartya;
            naplo.naploz(kartya.getKartyaszam() + " | BELEPES");
        } catch (AtmKivetel e) {
            naplo.naploz(kartya.getKartyaszam() + " | ELUTASITVA | " + e.getMessage());
            throw e; // the menu still has to tell the user
        }
    }

    public int egyenleg() {
        return kartya().getSzamla().getEgyenleg();
    }

    /**
     * Withdrawal. Everything is checked BEFORE anything changes, so a failed
     * withdrawal leaves the account and the cash untouched.
     *
     * @return the notes that come out (value -> count)
     */
    public Map<Integer, Integer> penzFelvetel(int osszeg)
            throws NapiLimitKivetel, NincsFedezetKivetel, NincsElegKeszpenzKivetel {
        Kartya kartya = kartya();
        try {
            int maMegFelveheto = napiLimit - kartya.getMaFelvett();
            if (osszeg > maMegFelveheto) {
                throw new NapiLimitKivetel(napiLimit, maMegFelveheto);
            }
            Map<Integer, Integer> bankjegyek = penztar.kiszamol(osszeg); // nothing changes yet
            kartya.getSzamla().kivesz(osszeg);                            // can throw -> nothing changed
            penztar.kivesz(bankjegyek);
            kartya.hozzaadMaiFelvetel(osszeg);

            naplo.naploz(kartya.getKartyaszam() + " | FELVET | " + osszeg + " lej | " + bankjegyek);
            return bankjegyek;
        } catch (AtmKivetel e) {
            naplo.naploz(kartya.getKartyaszam() + " | ELUTASITVA | " + e.getMessage());
            throw e;
        }
    }

    public void befizetes(int osszeg) {
        kartya().getSzamla().befizet(osszeg);
        naplo.naploz(aktivKartya.getKartyaszam() + " | BEFIZET | " + osszeg + " lej");
    }

    public void kartyaKiad() {
        if (aktivKartya != null) {
            naplo.naploz(aktivKartya.getKartyaszam() + " | KILEPES");
        }
        aktivKartya = null;
    }

    public boolean vanKartya() {
        return aktivKartya != null;
    }

    public int getNapiLimit() {
        return napiLimit;
    }

    public Penztar getPenztar() {
        return penztar;
    }

    // calling a card operation without a card is a programmer error -> unchecked
    private Kartya kartya() {
        if (aktivKartya == null) {
            throw new IllegalStateException("Nincs kártya az automatában.");
        }
        return aktivKartya;
    }
}
