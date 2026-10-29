import java.util.List;
import java.util.Map;
import java.util.Scanner;

import kivetel.AtmKivetel;
import kivetel.KartyaLetiltvaKivetel;
import model.Bankautomata;
import model.Kartya;
import model.Naplo;
import model.Penztar;
import model.Szamla;

/**
 * Console ATM simulation.
 * <p>
 * Every error is caught here and turned into a normal sentence:
 * the user never sees a stack trace, whatever they type.
 */
public class Main {

    private static final Scanner BE = new Scanner(System.in);

    public static void main(String[] args) {
        // demo data: two cards, a filled ATM
        List<Kartya> kartyak = List.of(
                new Kartya("1111-2222", "1234", new Szamla("Kiss Anna", 1500)),
                new Kartya("3333-4444", "0000", new Szamla("Nagy Béla", 300)));

        Penztar penztar = new Penztar();
        penztar.feltolt(200, 5);
        penztar.feltolt(100, 10);
        penztar.feltolt(50, 10);
        penztar.feltolt(10, 20);

        Bankautomata atm = new Bankautomata(penztar, new Naplo("data/tranzakciok.log"), 2000);

        System.out.println("=== BANKAUTOMATA ===");
        while (true) {
            System.out.println();
            for (int i = 0; i < kartyak.size(); i++) {
                Kartya k = kartyak.get(i);
                System.out.println((i + 1) + ") " + k.getSzamla().getTulajdonos() + " – " + k.getKartyaszam()
                        + (k.isLetiltva() ? "  [LETILTVA]" : ""));
            }
            System.out.println("0) Kilépés");
            int valasztas = szamBekeres("Melyik kártyát teszed be? ");
            if (valasztas == 0) {
                System.out.println("Viszlát!");
                return;
            }
            if (valasztas < 1 || valasztas > kartyak.size()) {
                System.out.println("Nincs ilyen kártya.");
                continue;
            }
            if (pinBekeres(atm, kartyak.get(valasztas - 1))) {
                kartyaMenu(atm);
            }
        }
    }

    /** Asks for the PIN until it's good (true) or the card gets blocked (false). */
    private static boolean pinBekeres(Bankautomata atm, Kartya kartya) {
        while (true) {
            System.out.print("PIN: ");
            String pin = sorBekeres();
            try {
                atm.kartyaBehelyez(kartya, pin);
                System.out.println("Üdv, " + kartya.getSzamla().getTulajdonos() + "!");
                return true;
            } catch (KartyaLetiltvaKivetel e) {
                System.out.println(e.getMessage());
                return false;
            } catch (AtmKivetel e) {          // HibasPinKivetel -> try again
                System.out.println(e.getMessage());
            }
        }
    }

    private static void kartyaMenu(Bankautomata atm) {
        while (atm.vanKartya()) {
            System.out.println();
            System.out.println("1) Egyenleg   2) Pénzfelvétel   3) Befizetés   4) Kártya kiadása");
            int valasztas = szamBekeres("> ");
            try {
                switch (valasztas) {
                    case 1 -> System.out.println("Egyenleg: " + atm.egyenleg() + " lej");
                    case 2 -> {
                        int osszeg = szamBekeres("Összeg (lej): ");
                        Map<Integer, Integer> bankjegyek = atm.penzFelvetel(osszeg);
                        System.out.println("Kiadva: " + bankjegyekSzoveg(bankjegyek));
                        System.out.println("Új egyenleg: " + atm.egyenleg() + " lej");
                    }
                    case 3 -> {
                        int osszeg = szamBekeres("Összeg (lej): ");
                        atm.befizetes(osszeg);
                        System.out.println("Befizetve. Új egyenleg: " + atm.egyenleg() + " lej");
                    }
                    case 4 -> {
                        atm.kartyaKiad();
                        System.out.println("Ne felejtsd el a kártyádat!");
                    }
                    default -> System.out.println("Nincs ilyen menüpont.");
                }
            } catch (AtmKivetel e) {
                // our own checked exceptions: the message is already a human sentence
                System.out.println("Hiba: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                // e.g. negative amount
                System.out.println("Hibás adat: " + e.getMessage());
            }
        }
    }

    /** {200=1, 50=1} -> "1 x 200, 1 x 50" */
    private static String bankjegyekSzoveg(Map<Integer, Integer> bankjegyek) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> sor : bankjegyek.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(sor.getValue()).append(" x ").append(sor.getKey());
        }
        return sb.toString();
    }

    /** Reads a whole number; asks again on letters instead of crashing. */
    private static int szamBekeres(String kerdes) {
        while (true) {
            System.out.print(kerdes);
            String sor = sorBekeres();
            try {
                return Integer.parseInt(sor.trim());
            } catch (NumberFormatException e) {
                System.out.println("Kérlek, egész számot írj be.");
            }
        }
    }

    /** One line from the keyboard. If the input ends (Ctrl+D), the program stops politely. */
    private static String sorBekeres() {
        if (!BE.hasNextLine()) {
            System.out.println();
            System.out.println("A bemenet véget ért, viszlát!");
            System.exit(0);
        }
        return BE.nextLine();
    }
}
