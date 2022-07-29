package no.odit;

import java.io.*;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class TimeLockPuzzle {

    private final int P_RANDOM_SEED_LENGTH = getRandomInteger(12, 16);
    private final int Q_RANDOM_SEED_LENGTH = getRandomInteger(12, 16);
    private final int RSA_PRIME_LENGTH = 1024;
    private final BigInteger ONE = new BigInteger("1");
    private final BigInteger TWO = new BigInteger("2");
    private final BigInteger SIXTY = new BigInteger("60");
    private final BigInteger CALCULATIONS_PER_SECOND = new BigInteger("125000");
    private final BigInteger CALCULATIONS_PER_MINUTE = CALCULATIONS_PER_SECOND.multiply(SIXTY);

    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String args[]) throws IOException {
        System.out.println("Creating time capsule...");
        TimeLockPuzzle timeLock = new TimeLockPuzzle();
        String secretMessage = inputText("secret message");
        BigInteger minutes = new BigInteger(inputText("amount of minutes"));
        timeLock.createPuzzle(secretMessage, minutes);
        System.out.println("Successfully created puzzle!");
    }

    public void createPuzzle(String secretMessage, BigInteger minutes) throws IOException {
        System.out.println("Number of squarings/second = " + CALCULATIONS_PER_SECOND);
        System.out.println("Number of squarings/minute = " + CALCULATIONS_PER_MINUTE);
        BigInteger t = CALCULATIONS_PER_MINUTE.multiply(minutes);
        System.out.println("Squarings (total) = " + t);

        // Now generate RSA parameters
        System.out.println("Using " + RSA_PRIME_LENGTH + "-bit primes.");
        BigInteger twoPower = (new BigInteger("1")).shiftLeft(RSA_PRIME_LENGTH);

        BigInteger prand = new BigInteger(getRandomBigInteger(P_RANDOM_SEED_LENGTH));
        BigInteger qrand = new BigInteger(getRandomBigInteger(Q_RANDOM_SEED_LENGTH));
        System.out.println("Computing...");

        BigInteger p = new BigInteger("5");
        p = getNextPrime(p.modPow(prand, twoPower));
        System.out.println("p = " + p);

        BigInteger q = new BigInteger("5");
        q = getNextPrime(q.modPow(qrand, twoPower));
        System.out.println("q = " + q);

        BigInteger n = p.multiply(q);
        System.out.println("n = " + n);

        BigInteger pm1 = p.subtract(ONE);
        BigInteger qm1 = q.subtract(ONE);
        BigInteger phi = pm1.multiply(qm1);
        System.out.println("phi = " + phi);

        // Now generate final puzzle value w
        BigInteger u = TWO.modPow(t, phi);
        BigInteger w = TWO.modPow(u, n);
        System.out.println("w (hex) = " + w.toString(16));

        // Obtain and encrypt the secret message
        // Include seed for p as a check
        StringBuffer sgen = new StringBuffer(secretMessage);
        sgen = sgen.append(" (seed value b for p = " + prand + ")");
        System.out.println("Puzzle secret = " + sgen);
        BigInteger secret = getBigIntegerFromStringBuffer(sgen);
        if (secret.compareTo(n) > 0) {
            System.out.println("Secret too large!");
            return;
        }
        BigInteger z = secret.xor(w);
        System.out.println("z(hex) = " + z.toString(16));

        // Write output to a file
        PrintWriter pw = new PrintWriter(new FileWriter("output.txt"));
        pw.println("Crypto-Time-Capsule");
        pw.println();
        pw.println("Puzzle parameters (all in decimal):");
        pw.println();
        pw.println("n = " + n);
        pw.println("t = " + t);
        pw.println("z = " + z);
        pw.println();
        pw.println("To solve the puzzle, first compute w = 2^(2^t) (mod n).");
        pw.println("Then exclusive-or the result with z.");
        pw.println("(Right-justify the two strings first.)");
        pw.println();
        pw.println("The result is the secret message (8 bits per character),");
        pw.println("including information that will allow you to factor n.");
        pw.println("(The extra information is a seed value b, such that ");
        pw.println("5^b (mod 2^1024) is just below a prime factor of n.)");
        pw.println();
        pw.close();

        System.out.println("File \"output.txt\" created.");

        // Test
        System.out.println("Testing puzzle...");
        String result = solvePuzzle(n, t, z);
        System.out.println("Test result: " + result);
    }

    private static String inputText(String description) throws IOException {
        System.out.print("Input " + description + ": ");
        return in.readLine();
    }

    private String solvePuzzle(BigInteger n, BigInteger t, BigInteger z) {
        BigInteger w = TWO;
        for (int i = 0; i < t.intValue(); i++) {
            w = w.modPow(TWO, n);
        }
        BigInteger x = w.xor(z);
        return new String(x.toByteArray());
    }

    private BigInteger getBigIntegerFromStringBuffer(StringBuffer s) {
        BigInteger bigInt = new BigInteger("0");
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            bigInt = bigInt.shiftLeft(8).add(new BigInteger(Integer.toString(c)));
        }
        System.out.println("Value of string entered (hex) = " + bigInt.toString(16));
        return bigInt;
    }

    private int getRandomInteger(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    private String getRandomBigInteger(int length) {
        StringBuilder sb = new StringBuilder();
        String digits = "0123456789";
        for (int i = 0; i < length; i++) {
            int randomDigitIndex = getRandomInteger(0, digits.length());
            sb.append(digits.charAt(randomDigitIndex));
        }
        return sb.toString();
    }

    private BigInteger getNextPrime(BigInteger startvalue) {
        BigInteger p = startvalue;
        if (!p.and(ONE).equals(ONE)) p = p.add(ONE);
        while (!p.isProbablePrime(40)) p = p.add(TWO);
        return (p);
    }

}