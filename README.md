# Bankautomata saját kivételekkel (b-07)

A console ATM simulation where every possible error has its own exception, and
the user never sees a stack trace, whatever they type.

## Requirements

- Java JDK 17 or newer (uses `switch ->`)

## Project structure

```
Bankautomata/
├── src/
│   ├── Main.java              -> console menu, catches every error
│   ├── kivetel/               -> own exception hierarchy
│   │   ├── AtmKivetel.java            -> checked parent (extends Exception)
│   │   ├── HibasPinKivetel.java       -> tries left
│   │   ├── KartyaLetiltvaKivetel.java -> card number
│   │   ├── NincsFedezetKivetel.java   -> missing amount
│   │   ├── NincsElegKeszpenzKivetel.java -> requested amount
│   │   └── NapiLimitKivetel.java      -> limit + what's still allowed today
│   └── model/
│       ├── Szamla.java        -> encapsulated balance (befizet / kivesz only)
│       ├── Kartya.java        -> PIN check, blocked after 3 wrong PINs, daily total
│       ├── Penztar.java       -> banknotes (10, 50, 100, 200), greedy payout
│       ├── Naplo.java         -> transaction log, try-with-resources
│       └── Bankautomata.java  -> puts it all together
├── test/model/
│   └── BankautomataTest.java  -> 14 JUnit 5 tests (assertThrows for every own exception)
├── data/                      -> tranzakciok.log is written here
├── lib/                       -> junit-platform-console-standalone-6.1.3.jar
├── docs/
│   └── bankautomata.puml      -> PlantUML class diagram, exception hierarchy in its own package
└── README.md
```

## How to run

Run it from the project folder (the log goes to `data/tranzakciok.log`):

```bash
javac -d out src/Main.java src/kivetel/*.java src/model/*.java
java -cp out Main
```

Demo cards:

| Owner     | Card      | PIN  | Balance  |
|-----------|-----------|------|----------|
| Kiss Anna | 1111-2222 | 1234 | 1500 lei |
| Nagy Béla | 3333-4444 | 0000 | 300 lei  |

Daily limit: 2000 lei. The ATM starts with 5×200, 10×100, 10×50, 20×10 lei.

## How to run the tests

```bash
javac -d out src/Main.java src/kivetel/*.java src/model/*.java
javac -d out_test -cp out:lib/junit-platform-console-standalone-6.1.3.jar test/model/*.java
java -jar lib/junit-platform-console-standalone-6.1.3.jar \
  execute --class-path out:out_test --scan-class-path
```

## Checked or unchecked, and why

All the ATM errors (`AtmKivetel` and its 5 children) are **checked**, because the
caller can react to them sensibly: after a wrong PIN it asks again, when the
balance or the cash isn't enough it asks for a smaller amount. Making them checked
means the compiler forces the menu to handle them, so none of them can slip
through to the user. A **0 or negative amount** is an `IllegalArgumentException`
(**unchecked**): that's a programmer error (the caller should have validated the
input), there is nothing sensible to "recover" there, so it shouldn't make every
method signature longer. Calling a card operation with no card inserted is also a
programmer error, so it's an `IllegalStateException`.

## Business rules

- 3 wrong PINs in a row → the card is blocked (`KartyaLetiltvaKivetel`), even the good PIN doesn't help after that. A good PIN resets the counter.
- Payout from 200 → 100 → 50 → 10 lei notes, greedy from the biggest. If the 200s ran out, it continues with the 100s. Because every note is a multiple of the smaller ones, if greedy can't make the amount, nothing can.
- A withdrawal checks **everything first** (daily limit, banknotes, balance) and only then changes anything, so a failed withdrawal leaves the account, the cash and the daily total untouched.
- Every transaction (also the refused ones) is a line in `data/tranzakciok.log`. If writing the log fails, `Naplo` prints a warning to `stderr` and the payout still goes through.

## UML diagram

`docs/bankautomata.puml`: open it with the PlantUML VS Code extension or paste it
into https://www.plantuml.com/plantuml. The `kivetel` package holds the checked
hierarchy, the unchecked `java.lang` ones are drawn next to it, and the dashed
arrows show which class throws what.

## Note

Claude only checked if the folders have README and gitignore and pushed them so all of them are pushed
