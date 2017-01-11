package net.sourceforge.opencamera.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class HDRTests {
	/** Tests for HDR algoritm - only need to run on a single device
	 *  Should manually look over the images dumped onto DCIM/
	 *  To use these tests, the testdata/ subfolder should be manually copied to the test device in the DCIM/testOpenCamera/
	 *  folder (so you have DCIM/testOpenCamera/testdata/). We don't use assets/ as we'd end up with huge APK sizes which takes
	 *  time to transfer to the device everytime we run the tests.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(MainTests.class.getName());
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR1"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR3"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR4"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR5"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR6"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR7"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR8"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR9"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR10"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR11"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR12"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR13"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR14"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR15"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR16"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR17"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR18"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR19"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR20"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR21"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR22"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR23"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR24"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR25"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR26"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR27"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR28"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testHDR29"));
        return suite;
    }
}
