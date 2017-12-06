package com.dhrw.sitwithus.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class EncodedBitmap {

    /** */
    public static Bitmap toBitmap(String encodedBitmap) {
        byte[] pictureBytes = Base64.decode(encodedBitmap, Base64.URL_SAFE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length, options);
    }

    /** */
    public static String toString(Bitmap bitmap) {

        // Scale the image down so it does not take too much data to transmit
        float aspect = bitmap.getWidth() / bitmap.getHeight();
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (200f * aspect),
                (int) (200f / aspect), true);

        // Convert the bytes of the image to base64 encoded text
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.URL_SAFE);
    }

    /** */
    public static Bitmap getRoundBitmap(Bitmap bitmap, int roundRadius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Fill the canvas with a transparent background
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        // Draw the region where the image should be drawn
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setColor(0xffffffff);
        canvas.drawRoundRect(new RectF(rect), roundRadius, roundRadius, paint);

        // Draw the image to all non-transparent parts
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
