package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RSASystem {
  
  private static final Random randGen = new Random();
  private static StringBuilder sb = new StringBuilder();
  private static final int NUM_RANDOM_BITS = 5;
  private static final int NUM_PRIMALITY_TESTS = 20;
  private static final String ALICE = " Alice"; //has leading space to match sample output
  private static int[] alicePairs;
  private static int[] trentPairs;
  private static String FAST_EXPO_TRACE = new String();
  
  public static void main(String[] args) throws IOException {
   if (args.length != 2) { 
    System.err.println("Usage: <numOutputFiles> <pathToOutputFolder>");
    System.exit(0);
   }
   int numIters = 20;
   try {
      numIters = Integer.parseInt(args[0]);
   } catch (NumberFormatException e) {
     System.err.println("The number of output files must be a non-negative integer value");
     System.exit(0);
   }
   if (numIters < 0) {
     System.err.println("The number of output files must be a non-negative integer value");
     System.exit(0);
   } 
    File outputDirectory = new File (args[1] + "output/");
    outputDirectory.mkdir();
    if (outputDirectory.exists() && outputDirectory.isDirectory()) {
      File[] files = outputDirectory.listFiles();
      for (File f : files) {
        f.delete();
      }
    }
    for (int i = 1; i <= numIters; i++) {
      String fileName = outputDirectory + "/output" + i + ".txt";
      FileWriter fw = new FileWriter(fileName);
      printAll();
      fw.append(sb.toString());
      sb = new StringBuilder();
      fw.close();
    }
  }
  
  /**
   * Calls all the other print methods.
   */
  protected static void printAll() {
    printRandomNumbers();
    printNotAPrime();
    printPerhapsAPrime();
    printPrimesAndKeyPairs();
    printDigitalCertificate();
    printAuthentication();
  }
  
  /**
   * Appends the authentication data to the global StringBuilder.
   */
  protected static void printAuthentication() {
    sb.append("line:271\n");
    int n = alicePairs[2];
    String nStr = getBinaryString(n, 32);
    int k = calculateK(nStr);
    String u = calculateU(k);
    sb.append(String.format("k = %d, u = %d\n\n", k, Integer.parseInt(u, 2)));
    sb.append("line:274\n");
    sb.append(String.format("u = %s\n\n", u));
    
    int d = alicePairs[4];
    int e = alicePairs[3];
    int hashOfU = Integer.parseInt(computeHash(u), 2);
    int uInt = Integer.parseInt(u, 2);
    int v = fastExponentiation(hashOfU, d, n, false);
    int Ev = fastExponentiation(v, e, n, true);
    sb.append("line:281\n");
    sb.append(String.format("u = %d, h(u) = %d, v = %d, Ev = %d\n\n", uInt, hashOfU, v, Ev));
    
    sb.append("line:285\n");
    sb.append(FAST_EXPO_TRACE);
  }
  
  /**
   * Computes k by finding the first non-zero bit going from left to right in the input String.
   * @param n The input string on which k is computed
   * @return the position of first non-zero bit in n going from left to right
   */
  protected static int calculateK(String n) {
    int counter = 0;
    while (n.charAt(counter) != '1') {
      counter++;
    }
    return n.length() - counter - 1;
  }
  
  /**
   * Computes U given the position of the first non-zero bit.
   * @param k The position of the first non-zero bit going from left to right
   * @return A binary String representation of U with padding to 32 bits
   */
  protected static String calculateU(int k) {
    StringBuilder temp = new StringBuilder(k);
    temp.append(1);
    for (int i = 0; i < k - 1; i++) {
      temp.append(getLeastSignificantBit(randGen.nextInt()));
    }
    int temp2 = Integer.parseInt(temp.toString(), 2);
    return getBinaryString(temp2, 32);
  }
  
  /**
   * Appends the Digital Certificate data to the global StringBuilder.
   */
  protected static void printDigitalCertificate() {
    String nameStr = getLeftPaddedBinaryString(ALICE, 6);
    String n = getBinaryString(alicePairs[2], 32);
    String e = getBinaryString(alicePairs[3], 32);
    String r = nameStr + n + e;
    String hashOfrStr = computeHash(r);
    int hashOfr = Integer.parseInt(hashOfrStr, 2);
    int s = fastExponentiation(hashOfr, trentPairs[4], trentPairs[2], false);
    sb.append("\nline:243\n");
    sb.append(String.format("r = %s\n", r));
    sb.append(String.format("h(r) = %s\n", getBinaryString(hashOfr, 32)));
    sb.append(String.format("s = %s\n\n", getBinaryString(s, 32)));
    sb.append("line:246\n");
    sb.append(String.format("h(r) = %d, s = %d\n\n", hashOfr, s));
  }
  
  /**
   * Computes the value of modular exponentiation. No checks for invalid inputs.
   * @param a   The base
   * @param x   The exponent
   * @param n   The modulus
   * @param printOn Value that determines whether data is appended to the global StringBuilder
   * @return The value of the modular exponentiation computation
   */
  protected static int fastExponentiation(int a, int x, int n, boolean printOn) {
    StringBuilder temp = new StringBuilder();
    if (printOn) temp.append(String.format("%-2s|%-3s|%-6s|%-6s\n", "i", "xi", "y", "y"));
    int y = 1;
    String bitString = Integer.toBinaryString(x);
    for (int i = 0; i < bitString.length(); i++) {
      int counter = bitString.length() - i - 1;
      y = y * y % n;
      if (printOn) temp.append(String.format("%-2d|%-3c|%-6d|", counter, bitString.charAt(i), y));
      if (bitString.charAt(i) == '1') {
        y = a * y % n;
      }
      if (printOn) temp.append(String.format("%-6d\n", y));
    }
    if (printOn) FAST_EXPO_TRACE = temp.toString();
    return y;
  }
  
  /**
   * Computes the hash of the input string. The hash function splits the input string into
   * bytes and applies the XOR operation to each bit in each byte. The hash function maps an 
   * N byte string to a 1 byte string. The input string is not checked for valid length.
   * @param str The following property must hold: {@code str.length % 8 == 0}
   * @return The hash of the input string.
   */
  protected static String computeHash(String str) {
    String[] bytes = str.split("(?<=\\G.{8})");
    char[][] intStrs = new char[bytes.length][32];
    for (int i = 0; i < intStrs.length; i++) {
      intStrs[i] = bytes[i].toCharArray();
    }
    String res = "";
    for (int i = 0; i < intStrs[0].length; i++) {
      int sum = 0;
      for (int j = 0; j < intStrs.length; j++) {
        sum += Integer.parseInt(String.valueOf(intStrs[j][i]));
      }
      res += sum % 2;
    }
    return res;
  }
  
  /**
   * Pads a given string with leading zeros. No valid input checks.
   * @param name String that needs to be padded
   * @param strLenInBytes Length of resultant String.
   * @return padded string
   */
  protected static String getLeftPaddedBinaryString(String name, int strLenInBytes) {
    int numBits = strLenInBytes << 3;
    byte[] bytes = name.getBytes();
    StringBuilder bitStr = new StringBuilder(numBits);
    for (byte b : bytes) {
      bitStr.append(getBinaryString(b, 8));
    }
    return String.format("%" + numBits + "s", bitStr.toString()).replace(' ', '0');
  }
  
  /**
   * Finds the greatest common denominator for two integers. No tests for Invalid inputs.
   * @param a The following needs to apply: a > b
   * @param b The following needs to apply: b < a
   * @param printOn Value that determines whether content of method is appended to global 
   *        StringBuilder.
   * @return One iff a and b are relatively prime otherwise null.
   */
  protected static Integer extendedEuclideanAlgorithm(int a, int b, boolean printOn) {
    int r1 = a,
        r2 = b, 
        r3 = a % b, 
        q1 = 0, q2 = 0, qi = a / b; 
    int j = 1;
    int sj = 1, s1 = 1, s2 = 0;
    int tj = 0, t1 = 0, t2 = 1;
    if (printOn) sb.append(String.format(
        "%-2s|%-7s|%-7s|%-7s|%-7s|%-7s|%-7s\n", "j", "qi", "r", "ri+1", "ri+2", "si", "ti"));
    while ((sj * a + tj * b) != 1) {
      if (j == 1) {
        sj = 1;
        tj = 0;
      } else if (j == 2) {
        sj = 0;
        tj = 1;
      } else {
        sj = s2 - q2 * s1;
        tj = t2 - q2 * t1;
      }
      if (printOn) sb.append(String.format(
          "%-2d|%-7d|%-7d|%-7d|%-7d|%-7d|%-7d\n", j, qi, r1, r2, r3, sj, tj));
      r1 = sj * r2 + tj * r3;
      r1 = r2;
      r2 = r3;
      q2 = q1;
      q1 = qi;
      s2 = s1;
      s1 = sj;
      t2 = t1;
      t1 = tj;
      j++;
      try {
        r3 = r1 % r2;
        qi = r1 / r2;
      } catch (ArithmeticException e) {break;}
    }
    //need values for one extra iteration
    if (j == 1) {
      sj = 1;
      tj = 0;
    } else if (j == 2) {
      sj = 0;
      tj = 1;
    } else {
      sj = s2 - q2 * s1;
      tj = t2 - q2 * t1;
    }
    if (printOn)
      sb.append(String.format("%-2d|%-7s|%-7d|%-7s|%-7s|%-7d|%-7d\n", j, "", r1, "", "", sj, tj));
    if (r1 == 1) {
      return tj;
    }
    return null;
  }
  
  /**
   * Appends the data for the primes, modulus and key pairs for Alice and Trent to the global 
   * StringBuilder.
   */
  protected static void printPrimesAndKeyPairs() {
    alicePairs = getPrimesAndKeyPairs(true);
    sb.append("\nline:205\n");
    sb.append(String.format("d = %d\n", alicePairs[1]));
    sb.append("\nline:209\n");
    sb.append(String.format("p = %d, q = %d, n = %d, e = %d, d = %d\n", 
        alicePairs[0], alicePairs[1], alicePairs[2], alicePairs[3], alicePairs[4]));
    sb.append(String.format("p = %s\n", getBinaryString(alicePairs[0], 32)));
    sb.append(String.format("q = %s\n", getBinaryString(alicePairs[1], 32)));
    sb.append(String.format("n = %s\n", getBinaryString(alicePairs[2], 32)));
    sb.append(String.format("e = %s\n", getBinaryString(alicePairs[3], 32)));
    sb.append(String.format("d = %s\n", getBinaryString(alicePairs[4], 32)));
    
    trentPairs = getPrimesAndKeyPairs(false);
    sb.append("\nline:218\n");
    sb.append(String.format("p = %d, q = %d, n = %d, e = %d, d = %d\n", 
        trentPairs[0], trentPairs[1], trentPairs[2], trentPairs[3], trentPairs[4]));
    sb.append(String.format("p = %s\n", getBinaryString(trentPairs[0], 32)));
    sb.append(String.format("q = %s\n", getBinaryString(trentPairs[1], 32)));
    sb.append(String.format("n = %s\n", getBinaryString(trentPairs[2], 32)));
    sb.append(String.format("e = %s\n", getBinaryString(trentPairs[3], 32)));
    sb.append(String.format("d = %s\n", getBinaryString(trentPairs[4], 32)));
  }
  
  /**
   * Calls findPrimesAndKeyPairs() until a valid set is obtained.
   * @param printOn Value determines whether content is appended to global StringBuilder.
   * @return Array containing values in the following order {p, q, n, e, d}.
   */
  protected static int[] getPrimesAndKeyPairs(boolean printOn) {
    int[] temp;
    do { 
      temp = findPrimesAndKeyPairs(printOn);
    } while (temp == null);
    return temp;
  }
  
  /**
   * Finds two numbers that are relatively prime and computes n, e and d.
   * @param printOn Value determines whether content is appended to global StringBuilder.
   * @return Array containing values in the following order {p, q, n, e, d}.
   */
  protected static int[] findPrimesAndKeyPairs(boolean printOn) {
    int p, q;
    do {
      p = getTestedPrime();
      q = getTestedPrime();
    } while (p == q);
    int n = p * q;
    int phiOfN = (p-1) * (q-1);
    int e = 2;
    Integer d = 0;
    if (printOn) sb.append("line:192\n");
    do {
      e++;
      if (printOn) sb.append(String.format("e = %d\n", e));
      d = extendedEuclideanAlgorithm(phiOfN, e, printOn);
    } while (d == null && e < phiOfN);
    //need to restart with different primes
    if (e >= phiOfN || d == null) {
      return null;
    }
    //need to normalize d
    if (d < 0) {
      d += phiOfN;
    }
    return new int[] { p, q, n, e, d };
  }
  
  /**
   * Gets a number that is a candidate for a prime and runs the primality test a specified 
   * number of times.
   * @return A number that is prime with a statistically significant confidence.
   */
  protected static int getTestedPrime() {
    boolean foundCandidatePrime = false;
    int p = 0;
    while (!foundCandidatePrime) {
      p = getCandidatePrime();
      for (int i = 0; i < NUM_PRIMALITY_TESTS; i++) {
        int a = getRandomNumberForPrimalityTest(p);
        if (!isPerhapsPrime(a, p-1, false)) {
          break;
        }
      }
      foundCandidatePrime = true;
    }
    return p;
  }
  
  /**
   * Appends data for running the primality test on some candidate prime.
   */
  protected static void printPerhapsAPrime() {
    boolean foundAPrime = false;
    boolean doPrint = false;
    int a, possiblePrime;
    sb.append("line:169\n");
    while (!foundAPrime) {
      possiblePrime = getCandidatePrime();
      for (int i = 0; i < NUM_PRIMALITY_TESTS; i++) {
        if (i == NUM_PRIMALITY_TESTS - 1) {
          doPrint = true;
        }
        a = getRandomNumberForPrimalityTest(possiblePrime);
        foundAPrime = isPerhapsPrime(a, possiblePrime-1, doPrint);
        if (!foundAPrime) {
          break;
        }
      }
    }
  }
  
  /**
   * Generates a possible prime number by using random bits.
   * @return A possibly prime number.
   */
  protected static int getCandidatePrime() {
    int[] randomBits = new int[NUM_RANDOM_BITS];
    for (int i = NUM_RANDOM_BITS; i > 0 ; i--) {
      int randNum = randGen.nextInt();
      int randBit = getLeastSignificantBit(randNum);
      randomBits[i-1] = randBit;
    }
    int probablePrime = getCandidatePrime(randomBits);
    return probablePrime;
  }
  
  /**
   * Appends the data for running the primality test on a non-prime number to the global 
   * StringBuilder.
   */
  protected static void printNotAPrime() {
    boolean foundNonPrime = false;
    int a;
    int compositeNumber = 4;
    sb.append("line:161\n");
    while (!foundNonPrime) {
      compositeNumber *= 2;
      a = getRandomNumberForPrimalityTest(compositeNumber);
      foundNonPrime = !isPerhapsPrime(a, compositeNumber-1, false);
      if (foundNonPrime) {
        isPerhapsPrime(a, compositeNumber-1, true);
      }
    }
  }
  
  /**
   * Determines whether a number is prime or not.
   * @param a randomly chosen value between 0 < a < n.
   * @param x x = n - 1; where n is the candidate being tested
   * @param printOn Value that determines whether data is appended to the global StringBuilder.
   * @return True if n is a possible prime otherwise false.
   */
  protected static boolean isPerhapsPrime(int a, int x, boolean printOn) {
    int n = x + 1;
    int y = 1;
    String bitString = Integer.toBinaryString(x);
    if (printOn) sb.append(String.format("n = %d, a = %d\n", n, a));
    if (printOn) sb.append(String.format("%-2s|%-3s|%-4s|%-4s|%-4s\n", "i", "xi", "z", "y", "y"));
    for (int i = 0; i < bitString.length(); i++) {
      int z = y;
      int counter = bitString.length() - i - 1;
      if (printOn) sb.append(String.format("%-2s|%-3s|%-4s|", counter, bitString.charAt(i), z));
      y = y * y % n;
      if (printOn) sb.append(String.format("%-4s|", y));
      if (y == 1 && z != 1 && z != n - 1) {
        for (int j = counter-1; j >= 0; j--) {
          if (printOn) sb.append(String.format(
              "\n%-2s|%-3s|%-4s|%-4s|%-4s", j, bitString.charAt(j), "", "", ""));
        }
        if (printOn) sb.append(String.format("\n%d is not a prime because %d^2 mod %d = 1 and %d != 1 "
            + "and %d != %d - 1\n\n", n, z, n, z, z, n));
        return false;
      }
      if (bitString.charAt(i) == '1') {
        y = a * y % n;
      }
      if (printOn) sb.append(String.format("%-4s\n", y));
    }
    if (y != 1) {
      if (printOn) sb.append(String.format("%d is not a prime because %d^%d mod %d != 1\n\n",
          n, a, x, n));
      return false;
    }
    if (printOn) sb.append(String.format("%d is perhaps a prime\n\n", n));
    return true;
  }
  
  /**
   * Returns a uniformly distributed random number in the following range:
   * 0 < randomNumber < upperBound
   * @param upperBound - upper bound (exclusive)
   * @return randomNumber - poitive integer between 0 (exclusive) and upperBound (exclusive)
   */
  protected static int getRandomNumberForPrimalityTest(int upperBound) {
    return randGen.nextInt(upperBound - 1) + 1;
  }
  
  /**
   * Appends random number data to the global StringBuilder.
   */
  protected static void printRandomNumbers() {
    int[] randomBits = new int[NUM_RANDOM_BITS];
    sb.append("line:143\n");
    for (int i = NUM_RANDOM_BITS; i > 0 ; i--) {
      int randNum = randGen.nextInt();
      int randBit = getLeastSignificantBit(randNum);
      randomBits[i-1] = randBit;
      String binaryString = getBinaryString(randNum, 32);
      sb.append(String.format("b_%d|%s|%d\n", i, binaryString, randBit));
    }
    int probablePrime = getCandidatePrime(randomBits);
    sb.append(String.format("Number|%d|%s\n\n", probablePrime, getBinaryString(probablePrime, 32)));
  }
  
  /**
   * Creates a candidate prime number by using randomly generated bits.
   * @param randomBits Randomly generated bits.
   * @return The candidate for a prime.
   */
  protected static int getCandidatePrime(int[] randomBits) {
    //the least significant bit needs to be 1
    int randomPrime = 1;
    //the largest valid bit needs to be 1
    randomPrime ^= 1 << NUM_RANDOM_BITS + 1;
    //set the remaining bits with the random bits
    for (int i = NUM_RANDOM_BITS; i > 0; i--) {
      randomPrime |= randomBits[i-1] << i;
    }
    return randomPrime;
  }
  
  /**
   * Returns the least significant bit of an integer.
   * @param number The number whose least significant bit is returned.
   * @return An integer value for the least significant bit of number.
   */
  protected static int getLeastSignificantBit(int number) {
    return number & 1;
  }
  
  /**
   * Returns a binary string representation of the input number padded to the specified length.
   * @param number The number being converted to a binary String.
   * @param strLength The padded length of the output String.
   * @return Binary representation of the input number padded to the specified length.
   */
  protected static String getBinaryString(int number, int strLength) {
    return String.format("%" + strLength + "s", Integer.toBinaryString(number)).replace(' ', '0');
  }
  
  /**
   * Sets a seed for the random number generator that is used.
   * @param seed Seed value for the random number generator.
   */
  protected static void setRandGenSeed(long seed) {
    randGen.setSeed(seed);
  }
}
