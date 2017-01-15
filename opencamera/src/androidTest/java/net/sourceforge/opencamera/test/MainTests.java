package net.sourceforge.opencamera.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MainTests {
	// Tests that don't fit into another of the Test suites
	public static Test suite() {
        /*return new TestSuiteBuilder(AllTests.class)
        .includeAllPackagesUnderHere()
        .build();*/
		TestSuite suite = new TestSuite(MainTests.class.getName());
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testStartCameraPreviewCount"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSaveVideoMode"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSaveFlashTorchQuit"));
		//suite.addTest(TestSuite.createTest(MainActivityTest.class, "testSaveFlashTorchSwitchCamera"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFlashStartup"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFlashStartup2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testPreviewSize"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testPreviewSizeWYSIWYG"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testAutoFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testAutoFocusCorners"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testPopup"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSwitchResolution"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFaceDetection"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFocusFlashAvailability"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSwitchVideo"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFocusSwitchVideoSwitchCameras"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFocusRemainMacroSwitchCamera"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFocusRemainMacroSwitchPhoto"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFocusSaveMacroSwitchPhoto"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFocusSwitchVideoResetContinuous"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousPictureFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousPictureRepeatTouch"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousPictureSwitchAuto"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousVideoFocusForPhoto"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testStartupAutoFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testExposureLockNotSaved"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSaveQuality"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testZoom"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testZoomIdle"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testZoomSwitchCamera"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSwitchCameraIdle"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testGallery"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSettings"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFolderChooserNew"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFolderChooserInvalid"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSaveFolderHistory"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSaveFolderHistorySAF"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoResolutions1"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoResolutions2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoResolutions3"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testVideoResolutions4"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testPreviewRotation"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testSceneMode"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testColorEffect"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testWhiteBalance"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testImageQuality"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testFailOpenCamera"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testBestPreviewFps"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testMatchPreviewFpsToVideo"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testLocationToDMS"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testAudioControlIcon"));
        return suite;
    }
}
