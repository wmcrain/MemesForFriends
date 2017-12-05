package com.dhrw.sitwithus.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
}
