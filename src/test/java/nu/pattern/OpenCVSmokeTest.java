package nu.pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.imgcodecs.*;
import org.opencv.dnn.*;
import org.opencv.features2d.*;
import org.opencv.objdetect.*;
import org.opencv.calib3d.*;
import org.opencv.photo.*;
import org.opencv.video.*;
import org.opencv.videoio.*;
import org.opencv.ml.*;

/**
 * Smoke tests for all public OpenCV classes in Apertix.
 * Verifies that each class loads and can be instantiated
 * without UnsatisfiedLinkError on every platform.
 */
public class OpenCVSmokeTest {

    @BeforeClass
    public static void loadLibrary() {
        OpenCV.loadLocally();
    }

    // ========== core ==========

    @Test
    public void testCoreVersion() {
        String version = Core.getVersionString();
        assertNotNull(version);
        assertTrue(version.startsWith("4.10"));
    }

    @Test
    public void testMat() {
        Mat m = new Mat(3, 3, CvType.CV_8UC1);
        assertNotNull(m);
        assertEquals(3, m.rows());
        assertEquals(3, m.cols());
    }

    @Test
    public void testMatZeros() {
        Mat m = Mat.zeros(5, 5, CvType.CV_8UC3);
        assertNotNull(m);
        assertEquals(5, m.rows());
    }

    @Test
    public void testMatOnes() {
        Mat m = Mat.ones(4, 4, CvType.CV_32FC1);
        assertNotNull(m);
    }

    @Test
    public void testMatEye() {
        Mat m = Mat.eye(3, 3, CvType.CV_64FC1);
        assertNotNull(m);
    }

    @Test
    public void testCvType() {
        assertEquals(0, CvType.CV_8UC1);
        assertEquals(16, CvType.CV_8UC3);
    }

    @Test
    public void testScalar() {
        Scalar s = new Scalar(1, 2, 3);
        assertNotNull(s);
        assertEquals(1.0, s.val[0], 0.001);
    }

    @Test
    public void testSize() {
        Size s = new Size(640, 480);
        assertEquals(640.0, s.width, 0.001);
        assertEquals(480.0, s.height, 0.001);
    }

    @Test
    public void testPoint() {
        Point p = new Point(10, 20);
        assertEquals(10.0, p.x, 0.001);
        assertEquals(20.0, p.y, 0.001);
    }

    @Test
    public void testPoint3() {
        Point3 p = new Point3(1, 2, 3);
        assertEquals(3.0, p.z, 0.001);
    }

    @Test
    public void testRect() {
        Rect r = new Rect(0, 0, 100, 200);
        assertEquals(100, r.width);
        assertEquals(200, r.height);
    }

    @Test
    public void testRect2d() {
        Rect2d r = new Rect2d(0, 0, 50.5, 100.5);
        assertNotNull(r);
    }

    @Test
    public void testRange() {
        Range r = new Range(0, 10);
        assertEquals(10, r.size());
    }

    @Test
    public void testRotatedRect() {
        RotatedRect rr = new RotatedRect(new Point(5, 5), new Size(10, 10), 45);
        assertNotNull(rr);
        assertEquals(45.0, rr.angle, 0.001);
    }

    @Test
    public void testTermCriteria() {
        TermCriteria tc = new TermCriteria(TermCriteria.COUNT, 100, 0.01);
        assertNotNull(tc);
    }

    @Test
    public void testTickMeter() {
        TickMeter tm = new TickMeter();
        tm.start();
        tm.stop();
        assertTrue(tm.getTimeMicro() >= 0);
    }

    @Test
    public void testDMatch() {
        DMatch dm = new DMatch();
        assertNotNull(dm);
    }

    @Test
    public void testKeyPoint() {
        KeyPoint kp = new KeyPoint();
        assertNotNull(kp);
    }

    @Test
    public void testCvException() {
        CvException ex = new CvException("test");
        assertEquals("test", ex.getMessage());
    }

    @Test
    public void testMatOfByte() {
        MatOfByte m = new MatOfByte();
        assertNotNull(m);
    }

    @Test
    public void testMatOfInt() {
        MatOfInt m = new MatOfInt();
        assertNotNull(m);
    }

    @Test
    public void testMatOfFloat() {
        MatOfFloat m = new MatOfFloat();
        assertNotNull(m);
    }

    @Test
    public void testMatOfDouble() {
        MatOfDouble m = new MatOfDouble();
        assertNotNull(m);
    }

    @Test
    public void testMatOfPoint() {
        MatOfPoint m = new MatOfPoint();
        assertNotNull(m);
    }

    @Test
    public void testMatOfPoint2f() {
        MatOfPoint2f m = new MatOfPoint2f();
        assertNotNull(m);
    }

    @Test
    public void testMatOfPoint3() {
        MatOfPoint3 m = new MatOfPoint3();
        assertNotNull(m);
    }

    @Test
    public void testMatOfPoint3f() {
        MatOfPoint3f m = new MatOfPoint3f();
        assertNotNull(m);
    }

    @Test
    public void testMatOfRect() {
        MatOfRect m = new MatOfRect();
        assertNotNull(m);
    }

    @Test
    public void testMatOfRect2d() {
        MatOfRect2d m = new MatOfRect2d();
        assertNotNull(m);
    }

    @Test
    public void testMatOfKeyPoint() {
        MatOfKeyPoint m = new MatOfKeyPoint();
        assertNotNull(m);
    }

    @Test
    public void testMatOfDMatch() {
        MatOfDMatch m = new MatOfDMatch();
        assertNotNull(m);
    }

    @Test
    public void testMatOfFloat4() {
        MatOfFloat4 m = new MatOfFloat4();
        assertNotNull(m);
    }

    @Test
    public void testMatOfFloat6() {
        MatOfFloat6 m = new MatOfFloat6();
        assertNotNull(m);
    }

    @Test
    public void testMatOfInt4() {
        MatOfInt4 m = new MatOfInt4();
        assertNotNull(m);
    }

    @Test
    public void testMatOfRotatedRect() {
        MatOfRotatedRect m = new MatOfRotatedRect();
        assertNotNull(m);
    }

    @Test
    public void testCoreFlip() {
        Mat src = Mat.ones(3, 3, CvType.CV_8UC1);
        Mat dst = new Mat();
        Core.flip(src, dst, 1);
        assertNotNull(dst);
        assertEquals(3, dst.rows());
    }

    @Test
    public void testCoreTranspose() {
        Mat src = Mat.ones(2, 3, CvType.CV_8UC1);
        Mat dst = new Mat();
        Core.transpose(src, dst);
        assertEquals(3, dst.rows());
        assertEquals(2, dst.cols());
    }

    @Test
    public void testCoreAdd() {
        Mat a = Mat.ones(3, 3, CvType.CV_8UC1);
        Mat b = Mat.ones(3, 3, CvType.CV_8UC1);
        Mat dst = new Mat();
        Core.add(a, b, dst);
        assertNotNull(dst);
    }

    @Test
    public void testCoreMinMaxLoc() {
        Mat m = Mat.eye(3, 3, CvType.CV_32FC1);
        Core.MinMaxLocResult result = Core.minMaxLoc(m);
        assertNotNull(result);
        assertEquals(1.0, result.maxVal, 0.001);
    }

    @Test
    public void testCoreMean() {
        Mat m = Mat.ones(3, 3, CvType.CV_8UC1);
        Scalar mean = Core.mean(m);
        assertEquals(1.0, mean.val[0], 0.001);
    }

    // ========== imgproc ==========

    @Test
    public void testImgprocCvtColor() {
        Mat src = new Mat(10, 10, CvType.CV_8UC3, new Scalar(100, 150, 200));
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        assertEquals(1, dst.channels());
    }

    @Test
    public void testImgprocResize() {
        Mat src = Mat.ones(100, 100, CvType.CV_8UC3);
        Mat dst = new Mat();
        Imgproc.resize(src, dst, new Size(50, 50));
        assertEquals(50, dst.rows());
        assertEquals(50, dst.cols());
    }

    @Test
    public void testImgprocGaussianBlur() {
        Mat src = Mat.ones(10, 10, CvType.CV_8UC1);
        Mat dst = new Mat();
        Imgproc.GaussianBlur(src, dst, new Size(3, 3), 0);
        assertNotNull(dst);
    }

    @Test
    public void testImgprocThreshold() {
        Mat src = Mat.ones(10, 10, CvType.CV_8UC1);
        Mat dst = new Mat();
        Imgproc.threshold(src, dst, 128, 255, Imgproc.THRESH_BINARY);
        assertNotNull(dst);
    }

    @Test
    public void testImgprocCanny() {
        Mat src = Mat.zeros(10, 10, CvType.CV_8UC1);
        Mat dst = new Mat();
        Imgproc.Canny(src, dst, 50, 150);
        assertNotNull(dst);
    }

    @Test
    public void testCLAHE() {
        CLAHE clahe = Imgproc.createCLAHE();
        assertNotNull(clahe);
    }

    @Test
    public void testSubdiv2D() {
        Subdiv2D sd = new Subdiv2D();
        assertNotNull(sd);
    }

    @Test
    public void testLineSegmentDetector() {
        LineSegmentDetector lsd = Imgproc.createLineSegmentDetector();
        assertNotNull(lsd);
    }

    @Test
    public void testIntelligentScissorsMB() {
        IntelligentScissorsMB is = new IntelligentScissorsMB();
        assertNotNull(is);
    }

    @Test
    public void testGeneralizedHoughBallard() {
        GeneralizedHoughBallard gh = Imgproc.createGeneralizedHoughBallard();
        assertNotNull(gh);
    }

    @Test
    public void testGeneralizedHoughGuil() {
        GeneralizedHoughGuil gh = Imgproc.createGeneralizedHoughGuil();
        assertNotNull(gh);
    }

    @Test
    public void testMoments() {
        Mat m = Mat.zeros(10, 10, CvType.CV_8UC1);
        Moments moments = Imgproc.moments(m);
        assertNotNull(moments);
    }

    // ========== imgcodecs ==========

    @Test
    public void testImgcodecsEncode() {
        Mat m = Mat.zeros(10, 10, CvType.CV_8UC3);
        MatOfByte buf = new MatOfByte();
        boolean ok = Imgcodecs.imencode(".png", m, buf);
        assertTrue(ok);
        assertTrue(buf.total() > 0);
    }

    @Test
    public void testImgcodecsEncodeAndDecode() {
        Mat src = new Mat(10, 10, CvType.CV_8UC3, new Scalar(50, 100, 150));
        MatOfByte buf = new MatOfByte();
        Imgcodecs.imencode(".png", src, buf);
        Mat decoded = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR);
        assertNotNull(decoded);
        assertEquals(10, decoded.rows());
    }

    // ========== dnn ==========

    @Test
    public void testNet() {
        Net net = new Net();
        assertNotNull(net);
        assertTrue(net.empty());
    }

    @Test
    public void testDnnBlobFromImage() {
        Mat img = Mat.zeros(224, 224, CvType.CV_8UC3);
        Mat blob = Dnn.blobFromImage(img);
        assertNotNull(blob);
    }

    @Test
    public void testImage2BlobParams() {
        Image2BlobParams params = new Image2BlobParams();
        assertNotNull(params);
    }

    @Test
    public void testDictValue() {
        DictValue dv = new DictValue(42);
        assertNotNull(dv);
    }

    // Classes nécessitant un modèle — on vérifie juste le chargement de la classe
    @Test
    public void testClassificationModelClassLoads() {
        assertNotNull(ClassificationModel.class);
    }

    @Test
    public void testDetectionModelClassLoads() {
        assertNotNull(DetectionModel.class);
    }

    @Test
    public void testKeypointsModelClassLoads() {
        assertNotNull(KeypointsModel.class);
    }

    @Test
    public void testSegmentationModelClassLoads() {
        assertNotNull(SegmentationModel.class);
    }

    @Test
    public void testTextDetectionModelDBClassLoads() {
        assertNotNull(TextDetectionModel_DB.class);
    }

    @Test
    public void testTextDetectionModelEASTClassLoads() {
        assertNotNull(TextDetectionModel_EAST.class);
    }

    @Test
    public void testTextRecognitionModelClassLoads() {
        assertNotNull(TextRecognitionModel.class);
    }

    // ========== features2d ==========

    @Test
    public void testSIFT() {
        SIFT sift = SIFT.create();
        assertNotNull(sift);
    }

    @Test
    public void testORB() {
        ORB orb = ORB.create();
        assertNotNull(orb);
    }

    @Test
    public void testBRISK() {
        BRISK brisk = BRISK.create();
        assertNotNull(brisk);
    }

    @Test
    public void testKAZE() {
        KAZE kaze = KAZE.create();
        assertNotNull(kaze);
    }

    @Test
    public void testAKAZE() {
        AKAZE akaze = AKAZE.create();
        assertNotNull(akaze);
    }

    @Test
    public void testMSER() {
        MSER mser = MSER.create();
        assertNotNull(mser);
    }

    @Test
    public void testFastFeatureDetector() {
        FastFeatureDetector ffd = FastFeatureDetector.create();
        assertNotNull(ffd);
    }

    @Test
    public void testAgastFeatureDetector() {
        AgastFeatureDetector afd = AgastFeatureDetector.create();
        assertNotNull(afd);
    }

    @Test
    public void testGFTTDetector() {
        GFTTDetector gftt = GFTTDetector.create();
        assertNotNull(gftt);
    }

    @Test
    public void testSimpleBlobDetector() {
        SimpleBlobDetector sbd = SimpleBlobDetector.create();
        assertNotNull(sbd);
    }

    @Test
    public void testSimpleBlobDetectorParams() {
        SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();
        assertNotNull(params);
    }

    @Test
    public void testAffineFeature() {
        AffineFeature af = AffineFeature.create(SIFT.create());
        assertNotNull(af);
    }

    @Test
    public void testBFMatcher() {
        BFMatcher bfm = BFMatcher.create();
        assertNotNull(bfm);
    }

    @Test
    public void testFlannBasedMatcher() {
        FlannBasedMatcher fbm = FlannBasedMatcher.create();
        assertNotNull(fbm);
    }

    @Test
    public void testBOWKMeansTrainer() {
        BOWKMeansTrainer bow = new BOWKMeansTrainer(10);
        assertNotNull(bow);
    }

    // ========== objdetect ==========

    @Test
    public void testCascadeClassifier() {
        CascadeClassifier cc = new CascadeClassifier();
        assertNotNull(cc);
    }

    @Test
    public void testHOGDescriptor() {
        HOGDescriptor hog = new HOGDescriptor();
        assertNotNull(hog);
    }

    @Test
    public void testQRCodeDetector() {
        QRCodeDetector qr = new QRCodeDetector();
        assertNotNull(qr);
    }

    @Test
    public void testQRCodeDetectorAruco() {
        QRCodeDetectorAruco qr = new QRCodeDetectorAruco();
        assertNotNull(qr);
    }

    @Test
    public void testQRCodeEncoder() {
        QRCodeEncoder qr = QRCodeEncoder.create();
        assertNotNull(qr);
    }

    @Test
    public void testQRCodeEncoderParams() {
        QRCodeEncoder_Params params = new QRCodeEncoder_Params();
        assertNotNull(params);
    }

    @Test
    public void testBarcodeDetector() {
        BarcodeDetector bd = new BarcodeDetector();
        assertNotNull(bd);
    }

    @Test
    public void testArucoDetector() {
        ArucoDetector ad = new ArucoDetector();
        assertNotNull(ad);
    }

    @Test
    public void testDetectorParameters() {
        DetectorParameters dp = new DetectorParameters();
        assertNotNull(dp);
    }

    @Test
    public void testDictionary() {
        Dictionary dict = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);
        assertNotNull(dict);
    }

    @Test
    public void testRefineParameters() {
        RefineParameters rp = new RefineParameters();
        assertNotNull(rp);
    }

    @Test
    public void testCharucoBoard() {
        Dictionary dict = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);
        CharucoBoard board = new CharucoBoard(new Size(5, 7), 0.04f, 0.02f, dict);
        assertNotNull(board);
    }

    @Test
    public void testGridBoard() {
        Dictionary dict = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);
        GridBoard board = new GridBoard(new Size(5, 7), 0.04f, 0.01f, dict);
        assertNotNull(board);
    }

    // FaceDetectorYN, FaceRecognizerSF — nécessitent un modèle
    @Test
    public void testFaceDetectorYNClassLoads() {
        assertNotNull(FaceDetectorYN.class);
    }

    @Test
    public void testFaceRecognizerSFClassLoads() {
        assertNotNull(FaceRecognizerSF.class);
    }

    // ========== calib3d ==========

    @Test
    public void testStereoBM() {
        StereoBM sbm = StereoBM.create();
        assertNotNull(sbm);
    }

    @Test
    public void testStereoSGBM() {
        StereoSGBM sgbm = StereoSGBM.create(0, 16, 3);
        assertNotNull(sgbm);
    }

    @Test
    public void testUsacParams() {
        UsacParams up = new UsacParams();
        assertNotNull(up);
    }

    @Test
    public void testCalib3dFindHomography() {
        MatOfPoint2f src = new MatOfPoint2f(
            new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(0, 1)
        );
        MatOfPoint2f dst = new MatOfPoint2f(
            new Point(0, 0), new Point(2, 0), new Point(2, 2), new Point(0, 2)
        );
        Mat h = Calib3d.findHomography(src, dst);
        assertNotNull(h);
        assertFalse(h.empty());
    }

    // ========== photo ==========

    @Test
    public void testAlignMTB() {
        AlignMTB align = Photo.createAlignMTB();
        assertNotNull(align);
    }

    @Test
    public void testCalibrateDebevec() {
        CalibrateDebevec cd = Photo.createCalibrateDebevec();
        assertNotNull(cd);
    }

    @Test
    public void testCalibrateRobertson() {
        CalibrateRobertson cr = Photo.createCalibrateRobertson();
        assertNotNull(cr);
    }

    @Test
    public void testMergeDebevec() {
        MergeDebevec md = Photo.createMergeDebevec();
        assertNotNull(md);
    }

    @Test
    public void testMergeMertens() {
        MergeMertens mm = Photo.createMergeMertens();
        assertNotNull(mm);
    }

    @Test
    public void testMergeRobertson() {
        MergeRobertson mr = Photo.createMergeRobertson();
        assertNotNull(mr);
    }

    @Test
    public void testTonemap() {
        Tonemap tm = Photo.createTonemap();
        assertNotNull(tm);
    }

    @Test
    public void testTonemapDrago() {
        TonemapDrago td = Photo.createTonemapDrago();
        assertNotNull(td);
    }

    @Test
    public void testTonemapMantiuk() {
        TonemapMantiuk tm = Photo.createTonemapMantiuk();
        assertNotNull(tm);
    }

    @Test
    public void testTonemapReinhard() {
        TonemapReinhard tr = Photo.createTonemapReinhard();
        assertNotNull(tr);
    }

    @Test
    public void testPhotoDenoise() {
        Mat src = new Mat(10, 10, CvType.CV_8UC3, new Scalar(100, 100, 100));
        Mat dst = new Mat();
        Photo.fastNlMeansDenoisingColored(src, dst);
        assertNotNull(dst);
    }

    // ========== video ==========

    @Test
    public void testBackgroundSubtractorKNN() {
        BackgroundSubtractorKNN bs = Video.createBackgroundSubtractorKNN();
        assertNotNull(bs);
    }

    @Test
    public void testBackgroundSubtractorMOG2() {
        BackgroundSubtractorMOG2 bs = Video.createBackgroundSubtractorMOG2();
        assertNotNull(bs);
    }

    @Test
    public void testDISOpticalFlow() {
        DISOpticalFlow dis = DISOpticalFlow.create();
        assertNotNull(dis);
    }

    @Test
    public void testFarnebackOpticalFlow() {
        FarnebackOpticalFlow fb = FarnebackOpticalFlow.create();
        assertNotNull(fb);
    }

    @Test
    public void testSparsePyrLKOpticalFlow() {
        SparsePyrLKOpticalFlow sp = SparsePyrLKOpticalFlow.create();
        assertNotNull(sp);
    }

    @Test
    public void testVariationalRefinement() {
        VariationalRefinement vr = VariationalRefinement.create();
        assertNotNull(vr);
    }

    @Test
    public void testKalmanFilter() {
        KalmanFilter kf = new KalmanFilter(4, 2);
        assertNotNull(kf);
    }

    @Test
    public void testTrackerMIL() {
        TrackerMIL tracker = TrackerMIL.create();
        assertNotNull(tracker);
    }

    @Test
    public void testTrackerMILParams() {
        TrackerMIL_Params params = new TrackerMIL_Params();
        assertNotNull(params);
    }

    @Test
    public void testTrackerDaSiamRPNParams() {
        TrackerDaSiamRPN_Params params = new TrackerDaSiamRPN_Params();
        assertNotNull(params);
    }

    @Test
    public void testTrackerGOTURNParams() {
        TrackerGOTURN_Params params = new TrackerGOTURN_Params();
        assertNotNull(params);
    }

    @Test
    public void testTrackerNanoParams() {
        TrackerNano_Params params = new TrackerNano_Params();
        assertNotNull(params);
    }

    @Test
    public void testTrackerVitParams() {
        TrackerVit_Params params = new TrackerVit_Params();
        assertNotNull(params);
    }

    // ========== videoio ==========

    @Test
    public void testVideoCapture() {
        VideoCapture vc = new VideoCapture();
        assertNotNull(vc);
        assertFalse(vc.isOpened());
    }

    @Test
    public void testVideoWriter() {
        VideoWriter vw = new VideoWriter();
        assertNotNull(vw);
        assertFalse(vw.isOpened());
    }

    // ========== ml ==========

    @Test
    public void testSVM() {
        SVM svm = SVM.create();
        assertNotNull(svm);
    }

    @Test
    public void testSVMSGD() {
        SVMSGD svmsgd = SVMSGD.create();
        assertNotNull(svmsgd);
    }

    @Test
    public void testKNearest() {
        KNearest knn = KNearest.create();
        assertNotNull(knn);
    }

    @Test
    public void testDTrees() {
        DTrees dt = DTrees.create();
        assertNotNull(dt);
    }

    @Test
    public void testRTrees() {
        RTrees rt = RTrees.create();
        assertNotNull(rt);
    }

    @Test
    public void testBoost() {
        Boost boost = Boost.create();
        assertNotNull(boost);
    }

    @Test
    public void testANN_MLP() {
        ANN_MLP ann = ANN_MLP.create();
        assertNotNull(ann);
    }

    @Test
    public void testEM() {
        EM em = EM.create();
        assertNotNull(em);
    }

    @Test
    public void testLogisticRegression() {
        LogisticRegression lr = LogisticRegression.create();
        assertNotNull(lr);
    }

    @Test
    public void testNormalBayesClassifier() {
        NormalBayesClassifier nbc = NormalBayesClassifier.create();
        assertNotNull(nbc);
    }
}
