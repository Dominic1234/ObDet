package com.example.obdet;

import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import java.io.IOException;
import java.util.List;

public class ObjectDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionObject>> {

    private static final String TAG = "ObjDetectionProcessor";

    private final FirebaseVisionObjectDetector detector;

    public ObjectDetectionProcessor() {
        FirebaseVisionObjectDetectorOptions options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                    .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()
                    .enableMultipleObjects()
                    .build();

        detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
    }

    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    protected Task<List<FirebaseVisionObject>> processImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    protected void onSuccess(
            @NonNull List<FirebaseVisionObject> objects,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        for (int i = 0; i < objects.size(); ++i) {
            FirebaseVisionObject object = objects.get(i);
            ObjGraphic objGraphic = new ObjGraphic(graphicOverlay);
            graphicOverlay.add(objGraphic);
            objGraphic.updateObj(object, frameMetadata.getCameraFacing());
        }
    }

    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

}
