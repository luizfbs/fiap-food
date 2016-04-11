package br.com.fiap.fiapfood.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapHelper {

    public static Bitmap reduce(String path, int height){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = photoH/height;
        bmOptions.inJustDecodeBounds = false;

        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(path, bmOptions);
    }

}
