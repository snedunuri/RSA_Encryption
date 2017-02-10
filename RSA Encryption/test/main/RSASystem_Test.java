package main;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class RSASystem_Test {
  
  private static final long testSeed = 1L;
  private static final Random testRandGen = new Random(testSeed);
  
  @Before
  public void setUp() {
    RSASystem.setRandGenSeed(testSeed);
  }

  @Test
  public void testGetRandomBits() {
    int expectedRandInt = testRandGen.nextInt(Integer.MAX_VALUE);
    int expectedRandBit = expectedRandInt & 1;
    assertEquals(expectedRandBit, RSASystem.getLeastSignificantBit(expectedRandInt));
  }

}
