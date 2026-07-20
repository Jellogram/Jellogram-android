package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileBannerHelper {

    private static final String BANNER_SERVER = "http://31.77.147.69:5080";

    public interface BannerCallback {
        void onSuccess(String url);
        void onError(String error);
    }

    public static void uploadBanner(final long userId, final Bitmap bitmap, final BannerCallback callback) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                byte[] imageBytes = baos.toByteArray();

                String boundary = "Boundary-" + System.currentTimeMillis();
                String userIdStr = String.valueOf(userId);
                byte[] userIdBytes = userIdStr.getBytes("UTF-8");

                byte[] body = buildMultipartBody(boundary, userIdBytes, imageBytes);

                URL url = new URL(BANNER_SERVER + "/api/banner/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(body);
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    byte[] responseBytes = new byte[1024];
                    int len = is.read(responseBytes);
                    is.close();
                    String responseUrl = new String(responseBytes, 0, len, "UTF-8").trim();
                    if (callback != null) callback.onSuccess(responseUrl);
                } else {
                    if (callback != null) callback.onError("Server returned " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void downloadBanner(final long userId, final BannerBitmapCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BANNER_SERVER + "/api/banner/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                    if (callback != null) callback.onSuccess(bitmap);
                } else {
                    if (callback != null) callback.onError("No banner");
                }
                conn.disconnect();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void deleteBanner(final long userId, final BannerCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BANNER_SERVER + "/api/banner/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (callback != null) callback.onSuccess(responseCode == 200 ? "deleted" : "error " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    private static byte[] buildMultipartBody(String boundary, byte[] userIdBytes, byte[] imageBytes) throws Exception {
        byte[] delimiter = ("--" + boundary + "\r\n").getBytes("UTF-8");
        byte[] closeDelimiter = ("--" + boundary + "--\r\n").getBytes("UTF-8");
        byte[] crlf = "\r\n".getBytes("UTF-8");

        byte[] userPartHeader = ("Content-Disposition: form-data; name=\"user_id\"\r\n\r\n").getBytes("UTF-8");
        byte[] imagePartHeader = ("Content-Disposition: form-data; name=\"image\"; filename=\"banner.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n").getBytes("UTF-8");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(delimiter);
        bos.write(userPartHeader);
        bos.write(userIdBytes);
        bos.write(crlf);
        bos.write(delimiter);
        bos.write(imagePartHeader);
        bos.write(imageBytes);
        bos.write(crlf);
        bos.write(closeDelimiter);
        return bos.toByteArray();
    }

    public interface BannerBitmapCallback {
        void onSuccess(Bitmap bitmap);
        void onError(String error);
    }
}
