package net.sourceforge.opencamera.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PhotoCamera2Tests {
	// Tests related to taking photos that require Camera2 - only need to run this suite with Camera2
	public static Test suite() {
		TestSuite suite = new TestSuite(MainTests.class.getName());
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoManualFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoManualISOExposure"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoRaw"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoRawMulti"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoRawWaitCaptureResult"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedTrashRaw"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedTrashRaw2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoHDR"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoHDRSaveExpo"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoHDRFrontCamera"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoHDRAutoStabilise"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoHDRPhotoStamp"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoExpo"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoExpo5"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFlashAutoFakeMode"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFlashOnFakeMode"));
        return suite;
    }
}
