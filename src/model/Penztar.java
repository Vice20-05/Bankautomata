package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import kivetel.NincsElegKeszpenzKivetel;

/**
 * The cash inside the ATM: how many banknotes of each value (10, 50, 100, 200 lei).
 */
public class Penztar {

    public static final int[] CIMLETEK = { 200, 100, 50, 10 };

    // value -> number of notes, biggest value first
    private final TreeMap<Integer, Integer> keszlet = new TreeMap<>(Collections.reverseOrder());

    public Penztar() {
        for (int cimlet : CIMLETEK) {
            keszlet.put(cimlet, 0);
        }
    }

    /** Puts notes into the ATM (e.g. when the bank refills it). */
    public void feltolt(int cimlet, int darab) {
        if (!keszlet.containsKey(cimlet)) {
            throw new IllegalArgumentException("Ismeretlen címlet: " + cimlet);
        }
        if (darab <= 0) {
            throw new IllegalArgumentException("A darabszámnak pozitívnak kell lennie.");
        }
        keszlet.merge(cimlet, darab, Integer::sum);
    }

    /**
     * Works out which notes to give, WITHOUT taking them out yet.
     * <p>
     * Greedy: from the biggest note down, take as many as fit. If the 200s ran
     * out, it simply continues with the 100s. With 10/50/100/200 every note is a
     * multiple of the smaller ones, so if greedy doesn't find a solution, there is none.
     *
     * @return value -> count (only the notes that are used)
     * @throws NincsElegKeszpenzKivetel if there isn't enough cash or the amount can't be made
     */
    public Map<Integer, Integer> kiszamol(int osszeg) throws NincsElegKeszpenzKivetel {
        if (osszeg <= 0) {
            throw new IllegalArgumentException("Az összegnek pozitívnak kell lennie.");
        }
        if (osszeg > osszesKeszpenz()) {
            throw new NincsElegKeszpenzKivetel(osszeg, "csak " + osszesKeszpenz() + " lej van benne.");
        }

        Map<Integer, Integer> kiadando = new LinkedHashMap<>();
        int maradek = osszeg;
        for (Map.Entry<Integer, Integer> sor : keszlet.entrySet()) {
            int cimlet = sor.getKey();
            int darab = Math.min(maradek / cimlet, sor.getValue());
            if (darab > 0) {
                kiadando.put(cimlet, darab);
                maradek -= darab * cimlet;
            }
        }
        if (maradek != 0) {
            throw new NincsElegKeszpenzKivetel(osszeg, "nem rakható ki a meglévő címletekből.");
        }
        return kiadando;
    }

    /** Takes the notes out (after kiszamol() said it's possible). */
    public void kivesz(Map<Integer, Integer> bankjegyek) {
        for (Map.Entry<Integer, Integer> sor : bankjegyek.entrySet()) {
            keszlet.merge(sor.getKey(), -sor.getValue(), Integer::sum);
        }
    }

    public int osszesKeszpenz() {
        int osszes = 0;
        for (Map.Entry<Integer, Integer> sor : keszlet.entrySet()) {
            osszes += sor.getKey() * sor.getValue();
        }
        return osszes;
    }

    public int darab(int cimlet) {
        return keszlet.getOrDefault(cimlet, 0);
    }
}
