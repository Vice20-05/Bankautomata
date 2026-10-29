package model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kivetel.HibasPinKivetel;
import kivetel.KartyaLetiltvaKivetel;
import kivetel.NapiLimitKivetel;
import kivetel.NincsElegKeszpenzKivetel;
import kivetel.NincsFedezetKivetel;

/**
 * Tests for the ATM rules: at least one assertThrows for every own exception.
 */
public class BankautomataTest {

    // JUnit creates an empty temporary folder for every test
    @TempDir
    Path tempMappa;

    private Szamla szamla;
    private Kartya kartya;
    private Penztar penztar;
    private Bankautomata atm;

    @BeforeEach
    public void setUp() throws Exception {
        szamla = new Szamla("Teszt Elek", 1000);
        kartya = new Kartya("1234-5678", "1111", szamla);

        penztar = new Penztar();
        penztar.feltolt(200, 2);
        penztar.feltolt(100, 5);
        penztar.feltolt(50, 2);
        penztar.feltolt(10, 5);   // 400 + 500 + 100 + 50 = 1050 lei

        atm = new Bankautomata(penztar, new Naplo(tempMappa.resolve("teszt.log").toString()), 800);
        atm.kartyaBehelyez(kartya, "1111");
    }

    // ---------- own exceptions ----------

    @Test
    public void wrongPinThrowsAndTellsTriesLeft() {
        HibasPinKivetel e = assertThrows(HibasPinKivetel.class, () -> kartya.ellenorizPin("0000"));
        assertEquals(2, e.getMaradekProbalkozas());
    }

    @Test
    public void threeWrongPinsBlockTheCard() throws Exception {
        assertThrows(HibasPinKivetel.class, () -> kartya.ellenorizPin("0000"));
        assertThrows(HibasPinKivetel.class, () -> kartya.ellenorizPin("0000"));
        assertThrows(KartyaLetiltvaKivetel.class, () -> kartya.ellenorizPin("0000"));
        assertTrue(kartya.isLetiltva());
        // even the good PIN doesn't help anymore
        assertThrows(KartyaLetiltvaKivetel.class, () -> kartya.ellenorizPin("1111"));
    }

    @Test
    public void goodPinResetsTheCounter() throws Exception {
        assertThrows(HibasPinKivetel.class, () -> kartya.ellenorizPin("0000"));
        assertThrows(HibasPinKivetel.class, () -> kartya.ellenorizPin("0000"));
        kartya.ellenorizPin("1111");
        HibasPinKivetel e = assertThrows(HibasPinKivetel.class, () -> kartya.ellenorizPin("0000"));
        assertEquals(2, e.getMaradekProbalkozas());
    }

    @Test
    public void notEnoughMoneyOnAccount() {
        NincsFedezetKivetel e = assertThrows(NincsFedezetKivetel.class, () -> szamla.kivesz(1300));
        assertEquals(300, e.getHianyzoOsszeg());
    }

    @Test
    public void notEnoughCashInTheAtm() {
        assertThrows(NincsElegKeszpenzKivetel.class, () -> penztar.kiszamol(2000));
    }

    @Test
    public void amountThatCantBeMadeFromTheNotes() {
        assertThrows(NincsElegKeszpenzKivetel.class, () -> penztar.kiszamol(35));
    }

    @Test
    public void dailyLimit() throws Exception {
        atm.penzFelvetel(500);
        NapiLimitKivetel e = assertThrows(NapiLimitKivetel.class, () -> atm.penzFelvetel(400));
        assertEquals(300, e.getMaMegFelveheto());
    }

    // ---------- unchecked: programmer errors ----------

    @Test
    public void zeroOrNegativeAmountIsIllegal() {
        assertThrows(IllegalArgumentException.class, () -> szamla.befizet(-100));
        assertThrows(IllegalArgumentException.class, () -> szamla.kivesz(0));
    }

    @Test
    public void noCardInTheAtm() {
        atm.kartyaKiad();
        assertThrows(IllegalStateException.class, () -> atm.egyenleg());
    }

    // ---------- banknotes + business rules ----------

    @Test
    public void greedyBanknotes() throws Exception {
        assertEquals(Map.of(200, 1, 100, 1, 50, 1, 10, 3), penztar.kiszamol(380));
    }

    @Test
    public void whenThe200sRunOutItUses100s() throws Exception {
        atm.penzFelvetel(400);                       // takes both 200s
        assertEquals(0, penztar.darab(200));
        assertEquals(Map.of(100, 3), atm.penzFelvetel(300));
    }

    @Test
    public void failedWithdrawalChangesNothing() {
        szamla = new Szamla("Szegeny", 100);
        Kartya szegenyKartya = new Kartya("9999-0000", "2222", szamla);
        assertDoesNotThrow(() -> atm.kartyaBehelyez(szegenyKartya, "2222"));

        assertThrows(NincsFedezetKivetel.class, () -> atm.penzFelvetel(500));
        assertEquals(100, szamla.getEgyenleg());
        assertEquals(1050, penztar.osszesKeszpenz());
        assertEquals(0, szegenyKartya.getMaFelvett());
    }

    // ---------- log ----------

    @Test
    public void withdrawalIsLogged() throws Exception {
        atm.penzFelvetel(100);
        String naplo = Files.readString(tempMappa.resolve("teszt.log"));
        assertTrue(naplo.contains("FELVET | 100 lej"));
    }

    @Test
    public void logErrorDoesNotStopThePayout() throws IOException {
        // the "file" is a folder -> FileWriter can't open it -> IOException inside Naplo
        Path mappa = Files.createDirectory(tempMappa.resolve("nem-fajl"));
        Bankautomata rosszNaploval = new Bankautomata(penztar, new Naplo(mappa.toString()), 800);

        assertDoesNotThrow(() -> rosszNaploval.kartyaBehelyez(kartya, "1111"));
        assertDoesNotThrow(() -> rosszNaploval.penzFelvetel(100));
        assertEquals(900, szamla.getEgyenleg());
    }
}
