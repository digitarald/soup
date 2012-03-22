
package org.mozilla.labs.Soup.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

/**
 * Helper class for fetching and disk-caching images from the web.
 */
public class ImageFactory {

    private static final String TAG = "ImageFactory";

    public static Bitmap getResizedImage(String uri, int newHeight, int newWidth) {
        Bitmap bitmap = null;

        String scheme = Uri.parse(uri).getScheme();

        if (scheme != null && scheme.equals("data")) {

            try {
                byte[] decodedString = Base64.decode(uri.substring(22), Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            } catch (Exception e) {
                Log.w(TAG, "Base64.decode", e);
            }

        } else {

            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(uri).getContent());
            } catch (Exception e) {
                Log.w(TAG, "BitmapFactory.decodeStream", e);
            }

        }

        if (bitmap == null) {
            Log.w(TAG, "Invalid bitmap for " + uri);
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float)newWidth) / width;
        float scaleHeight = ((float)newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static interface OnFetchCompleteListener {
        public void onFetchComplete(Object cookie, Bitmap result);
    }

    /**
     * Only call this method from the main (UI) thread.
     */
    public static void fetchImage(final Context context, final String url,
            final OnFetchCompleteListener callback) {
        fetchImage(context, url, null, null, callback);
    }

    /**
     * Only call this method from the main (UI) thread.
     */
    public static void fetchImage(final Context context, final String url,
            final BitmapFactory.Options decodeOptions, final Object cookie,
            final OnFetchCompleteListener callback) {
        new AsyncTask<String, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                final String url = params[0];

                return fetchImageSync(context, url, decodeOptions);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                callback.onFetchComplete(cookie, result);
            }
        }.execute(url);
    }

    private static Bitmap fetchImageSync(final Context context, final String url,
            final BitmapFactory.Options decodeOptions) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        // First compute the cache key and cache file path for this URL
        File cacheFile = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
            mDigest.update(url.getBytes());
            final String cacheKey = bytesToHexString(mDigest.digest());
            // if
            // (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            cacheFile = new File(context.getCacheDir() + File.separator + "bitmap_" + cacheKey
                    + ".tmp");
        } catch (NoSuchAlgorithmException e) {
            // Oh well, SHA-1 not available (weird), don't cache bitmaps.
        }

        if (cacheFile != null && cacheFile.exists()) {
            Bitmap cachedBitmap = BitmapFactory.decodeFile(cacheFile.toString(), decodeOptions);
            if (cachedBitmap != null) {
                return cachedBitmap;
            }
        }

        try {
            final HttpClient httpClient = HttpFactory
                    .getHttpClient(context.getApplicationContext());
            final HttpResponse resp = httpClient.execute(new HttpGet(url));
            final HttpEntity entity = resp.getEntity();

            final int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK || entity == null) {
                return null;
            }

            final byte[] respBytes = EntityUtils.toByteArray(entity);

            // Write response bytes to cache.
            if (cacheFile != null) {
                try {
                    cacheFile.getParentFile().mkdirs();
                    cacheFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(cacheFile);
                    fos.write(respBytes);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "Error writing to bitmap cache: " + cacheFile.toString(), e);
                } catch (IOException e) {
                    Log.w(TAG, "Error writing to bitmap cache: " + cacheFile.toString(), e);
                }
            }

            // Decode the bytes and return the bitmap.
            return BitmapFactory.decodeByteArray(respBytes, 0, respBytes.length, decodeOptions);
        } catch (Exception e) {
            Log.w(TAG, "Problem while loading image: " + e.toString(), e);
        }

        return null;
    }

    // http://stackoverflow.com/questions/332079
    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
