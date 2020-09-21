package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

    private static final String LOG_TAG = Emojifier.class.getSimpleName();
    //Variables with threshold constants
    private static final float SMILE = 0.5f;
    private static final float EYE_OPEN = 0.5f;
    //Booleans to track the states of the facial expression
    private static boolean leftEyeOpen = false;
    private static boolean rightEyeOpen = false;
    private static boolean smile = false;

    static void detectFacesAndOverlayEmoji(Context context, Bitmap bitmap) {

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        SparseArray<Face> faces = detector.detect(frame);

       Log.d(LOG_TAG, "detectFaces: number of faces = " + faces.size());

        if (faces.size() == 0) {
            Toast.makeText(context, "No faces detected", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < faces.size(); i++) {
                Bitmap emojiBitmap;

                Face face = faces.valueAt(i);

                switch (whichEmoji(face)) {
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                        break;
                    case CLOSED_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
                        break;
                    case CLOSED_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
                        break;
                    case LEFT_TWINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_TWINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
                        break;
                    case RIGHT_TWINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
                        break;
                }
            }
        }

        detector.release();
    }

    static Emoji whichEmoji(Face face) {
        smile = face.getIsSmilingProbability() > SMILE;
        rightEyeOpen = face.getIsRightEyeOpenProbability() > EYE_OPEN;
        leftEyeOpen = face.getIsLeftEyeOpenProbability() > EYE_OPEN;

        if (smile) {
            if (rightEyeOpen && leftEyeOpen)
                return Emoji.SMILE;
            else if (!rightEyeOpen && leftEyeOpen)
                return Emoji.RIGHT_TWINK;
            else if(rightEyeOpen && !leftEyeOpen)
                return Emoji.LEFT_WINK;
            else
                return Emoji.CLOSED_SMILE;
        } else {
            if (rightEyeOpen && leftEyeOpen)
                return Emoji.FROWN;
            else if (!rightEyeOpen && leftEyeOpen)
                return Emoji.RIGHT_TWINK_FROWN;
            else if(rightEyeOpen && !leftEyeOpen)
                return Emoji.LEFT_TWINK_FROWN;
            else
                return Emoji.CLOSED_FROWN;
        }
    }

}
