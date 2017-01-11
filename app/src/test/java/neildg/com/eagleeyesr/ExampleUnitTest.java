package neildg.com.eagleeyesr;

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
        int numPatches = 590;

        int divisionOfWork =  numPatches / MAX_NUM_THREADS;

        int lowerX = 0;
        int upperX = divisionOfWork;

        while(lowerX <= numPatches) {
            System.out.println("Lower X: " +lowerX+ " Upper X: " +upperX + " Numpatches: " +numPatches);

            lowerX = upperX + 1;
            upperX += divisionOfWork;

            upperX = clamp(upperX, lowerX, numPatches);
        }

        /*for(int i = 1; i <= MAX_NUM_THREADS; i++) {
            System.out.println("Lower X: " +lowerX+ " Upper X: " +upperX + " Numpatches: " +numPatches);

            lowerX = upperX + 1;
            upperX += divisionOfWork;

            upperX = clamp(upperX, lowerX, numPatches);
        }*/
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