package com.scanny.scanner.scrapbook;

import android.location.Location;
import android.media.ExifInterface;
import android.os.Build;
import android.text.TextUtils;

import org.bouncycastle.crypto.tls.CipherSuite;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExifUtils {
    private static final String DATETIME_FORMAT = "yyyy:MM:dd HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy:MM:dd";
    private static final String TAG = "ExifUtils";
    private static final String TAG_GPS_DATE_STAMP = "GPSDateStamp";
    private static final String TIME_FORMAT = "HH:mm:ss";

    private ExifUtils() {
    }

    public static int getExifRotation(String str) {
        try {
            String attribute = new ExifInterface(str).getAttribute(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION);
            if (TextUtils.isEmpty(attribute)) {
                return 0;
            }
            int parseInt = Integer.parseInt(attribute);
            if (parseInt == 3) {
                return CipherSuite.TLS_DHE_PSK_WITH_NULL_SHA256;
            }
            if (parseInt == 6) {
                return 90;
            }
            if (parseInt != 8) {
                return 0;
            }
            return 270;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getAngle(String str) {
        try {
            int attributeInt = new ExifInterface(str).getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 0);
            if (attributeInt == 3) {
                return CipherSuite.TLS_DHE_PSK_WITH_NULL_SHA256;
            }
            if (attributeInt == 6) {
                return 90;
            }
            if (attributeInt != 8) {
                return 0;
            }
            return 270;
        } catch (IOException exception) {
            return 0;
        }
    }

    public static void save(String str, Date date, int i, Boolean bool, Location location) throws IOException {
        ExifInterface exifInterface = new ExifInterface(str);
        if (date != null) {
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_DATETIME, new SimpleDateFormat(DATETIME_FORMAT, Locale.ENGLISH).format(date));
        }
        exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_MAKE, Build.MANUFACTURER);
        exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_MODEL, Build.MODEL);
        exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, i + "");
        if (bool != null) {
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_FLASH, String.valueOf(bool.booleanValue() ? 1 : 0));
        }
        if (location != null) {
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LATITUDE, formatExifGpsDMS(location.getLatitude()));
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE_REF, androidx.exifinterface.media.ExifInterface.LONGITUDE_EAST);
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE, formatExifGpsDMS(location.getLongitude()));
            exifInterface.setAttribute("GPSDateStamp", new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).format(date));
        }
        exifInterface.saveAttributes();
    }

    private static String formatExifGpsDMS(double d) {
        double floor = Math.floor(d);
        double d2 = d - floor;
        double floor2 = Math.floor(d2 * 60.0d);
        String valueOf = String.valueOf((int) floor);
        String valueOf2 = String.valueOf((int) floor2);
        String valueOf3 = String.valueOf((int) ((d2 - (floor2 / 60.0d)) * 3600.0d * 1000.0d));
        return valueOf + "/1," + valueOf2 + "/1," + valueOf3.substring(0, Math.min(valueOf3.length(), 4)) + "/1000";
    }
}
