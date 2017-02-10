package main;

public class Temp {
  public static void main(String[] args) {
    System.out.println(isPerhapsPrime(5, 12));
//    System.out.println(fastExponentiation2(2, 31));
  }
  
  public static boolean isPerhapsPrime(int a, int x) {
    int n = x + 1;
    int y = 1;
    String bitString = Integer.toBinaryString(x);
    for (int i = 0; i < bitString.length(); i++) {
      int counter = bitString.length() - i - 1;
      int z = y;
      System.out.print(counter + " ");
      System.out.print(bitString.charAt(i) + " ");
      System.out.print(z + " ");
      y = y * y % n;
      System.out.print(y + " ");
      if (y == 1 && z != 1 && z != n - 1) {
        return false;
      }
      if (bitString.charAt(i) == '1') {
        y = a * y % n;
      }
      System.out.print(y + "\n");
    }
    if (y != 1) {
      return false;
    }
    return true;
  }
  
  public static int fastExponentiation(int a, int x, int n) {
    int y = 1;
    String bitString = Integer.toBinaryString(x);
    for (int i = 0; i < bitString.length(); i++) {
      y = y * y % n;
      if (bitString.charAt(i) == '1') {
        y = a * y % n;
      }
    }
    return y;
  }
  
  public static int fastExponentiation2(int a, int x) {
    int y = 1;
    String bitString = Integer.toBinaryString(x);
    for (int i = 0; i < bitString.length(); i++) {
      y *= y;
      if (bitString.charAt(i) == '1') {
        y *= a;
      }
    }
    return y;
  }
}
