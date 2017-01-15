package net.sourceforge.opencamera.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PhotoTests {
	// Tests related to taking photos; note that tests to do with photo mode that don't take photos are still part of MainTests
	public static Test suite() {
		TestSuite suite = new TestSuite(MainTests.class.getName());
		// put these tests first as they require various permissions be allowed, that can only be set by user action
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoSAF"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testLocationOn"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testLocationDirectionOn"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testLocationOff"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testDirectionOn"));
		// other tests:
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhoto"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoContinuous"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoContinuousNoTouch"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoAutoStabilise"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFlashAuto"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFlashOn"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFlashTorch"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoAudioButton"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoNoAutofocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoNoThumbnail"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFlashBug"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFrontCamera"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoFrontCameraScreenFlash"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoLockedFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoExposureCompensation"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoLockedLandscape"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoLockedPortrait"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPaused"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedAudioButton"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedSAF"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedTrash"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedTrashSAF"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoPreviewPausedTrash2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoQuickFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoRepeatFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoRepeatFocusLocked"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoAfterFocus"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoSingleTap"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoDoubleTap"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoAlt"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoAutoLevel"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoAutoLevelAngles"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTimerBackground"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTimerSettings"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTimerPopup"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testTakePhotoBurst"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousPicture1"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousPicture2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testContinuousPictureFocusBurst"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testPhotoStamp"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testCreateSaveFolder1"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testCreateSaveFolder2"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testCreateSaveFolder3"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testCreateSaveFolder4"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testCreateSaveFolderUnicode"));
		suite.addTest(TestSuite.createTest(OpenCameraActivityTest.class, "testCreateSaveFolderEmpty"));
        return suite;
    }
}
