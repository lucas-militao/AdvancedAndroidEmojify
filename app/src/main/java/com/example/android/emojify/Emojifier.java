package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import timber.log.Timber;

public class Emojifier {

    private static final String LOG_TAG = Emojifier.class.getSimpleName();
    //Variables with threshold constants
    private static final double SMILE = .15;
    private static final double EYE_OPEN = .5;
    private static final float EMOJI_SCALE_FACTOR = .9f;
    //Booleans to track the states of the facial expression
    private static boolean leftEyeOpen = false;
    private static boolean rightEyeOpen = false;
    private static boolean smile = false;

    static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap picture) {

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(picture).build();

        SparseArray<Face> faces = detector.detect(frame);

        Timber.d("detectFaces: number of faces = " + faces.size());

        Bitmap resultBitmap = picture;

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
                    default:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                        break;
                }

                resultBitmap = addBitmapToFace(picture, emojiBitmap, face);
            }
        }

        detector.release();

        return resultBitmap;
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

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

}
