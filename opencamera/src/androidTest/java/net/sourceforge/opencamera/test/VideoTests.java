package net.sourceforge.opencamera.test;

import junit.framework.Test;
import junit.framework.TestSuite;
public class VideoTests {
	// Tests related to video recording; note that tests to do with video mode that don't record are still part of MainTests
	public static Test suite() {
		TestSuite suite = new TestSuite(MainTests.class.getName());
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideo"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoAudioControl"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoSAF"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testImmersiveMode"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testImmersiveModeEverything"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoStabilization"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoExposureLock"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoFocusArea"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoQuick"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoQuickSAF"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMaxDuration"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMaxDurationRestart"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMaxDurationRestartInterrupt"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoSettings"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMacro"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoFlashVideo"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoTimerInterrupt"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoPopup"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoTimerPopup"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoAvailableMemory"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoAvailableMemory2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMaxFileSize1"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMaxFileSize2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoMaxFileSize3"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakeVideoForceFailure"));
		// put tests which change bitrate, fps or test 4K at end
		// update: now deprecating these tests, as setting these settings can be dodgy on some devices
		/*suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoBitrate"));
		suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoFPS"));
		suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideo4K"));*/
        return suite;
    }
}
