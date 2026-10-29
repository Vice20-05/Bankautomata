package kivetel;

/**
 * Parent of every ATM error.
 * <p>
 * It is a CHECKED exception (extends Exception): the caller can react to it in a
 * sensible way (ask for the PIN again, ask for a smaller amount...), so the
 * compiler forces the menu to handle it.
 */
public class AtmKivetel extends Exception {

    public AtmKivetel(String uzenet) {
        super(uzenet);
    }
}
