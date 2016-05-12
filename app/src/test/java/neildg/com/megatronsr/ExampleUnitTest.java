package neildg.com.megatronsr;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testDivideTask() throws Exception{
        int MAX_NUM_THREADS = 20;
        int numPatches = 100;

        int divisionOfWork = numPatches / MAX_NUM_THREADS;

        int lowerX = 0;
        int upperX = divisionOfWork;

        while(upperX <= numPatches) {
            System.out.println("Lower X: " +lowerX+ " Upper X: " +upperX);

            lowerX = upperX + 1;
            upperX += divisionOfWork;

            upperX = clamp(upperX, 0, numPatches);
            assertTrue(upperX <= numPatches);
        }
    }

    private static int clamp(int value, int minValue, int maxValue) {
        if(value > minValue && value < maxValue) {
            return value;
        }
        else if(value == minValue) {
            return minValue;
        }
        else {
            return maxValue;
        }
    }
}