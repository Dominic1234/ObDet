package com.example.obdet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.CameraSource;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.example.obdet.GraphicOverlay.Graphic;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class ObjGraphic extends Graphic {
    private static final float OBJ_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int[] COLOR_CHOICES = {
            Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW, Color.BLACK, Color.GRAY, Color.DKGRAY
    };
    private static int currentColorIndex = 0;

    private int facing;

    private final Paint objPositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private volatile FirebaseVisionObject firebaseVisionObj;

    public ObjGraphic(GraphicOverlay overlay) {
        super(overlay);

        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[currentColorIndex];

        objPositionPaint = new Paint();
        objPositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Updates the face instance from the detection of the most recent frame. Invalidates the relevant
     * portions of the overlay to trigger a redraw.
     */
    public void updateObj(FirebaseVisionObject obj, int facing) {
        firebaseVisionObj = obj;
        this.facing = facing;
        postInvalidate();
    }

    /** Draws the face annotations for position on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionObject obj = firebaseVisionObj;
        if (obj == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(obj.getBoundingBox().centerX());
        float y = translateY(obj.getBoundingBox().centerY());
        canvas.drawCircle(x, y, OBJ_POSITION_RADIUS, objPositionPaint);
        canvas.drawText("id: " + obj.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);
        canvas.drawText(
                "Category: " + String.format("%.2f", obj.getClassificationCategory()),
                x + ID_X_OFFSET * 3,
                y - ID_Y_OFFSET,
                idPaint);
        if (facing == CameraSource.CAMERA_FACING_FRONT) {
            canvas.drawText(
                    "Confidence: " + String.format("%.2f", obj.getClassificationConfidence()),
                    x,
                    y + ID_Y_OFFSET,
                    idPaint);

        } else {
            canvas.drawText(
                    "Category: " + String.format("%.2f", obj.getClassificationCategory()),
                    x - ID_X_OFFSET * 3,
                    y - ID_Y_OFFSET,
                    idPaint);
            canvas.drawText(
                    "Confidence: " + String.format("%.2f", obj.getClassificationConfidence()),
                    x + ID_X_OFFSET * 6,
                    y + ID_Y_OFFSET,
                    idPaint);
        }

        // Draws a bounding box around the face.
        float xOffset = scaleX(obj.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(obj.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, boxPaint);
    }
}
