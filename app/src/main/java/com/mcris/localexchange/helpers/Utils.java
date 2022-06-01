package com.mcris.localexchange.helpers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mcris.localexchange.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Utils {
    private static final Random random = new Random();

    public static String getFriendlyDate(LocalDate date, Context context) {
        if (date.equals(LocalDate.now())) {
            return context.getString(R.string.today);
        } else if (date.equals(LocalDate.now().minusDays(1))) {
            return context.getString(R.string.yesterday);
        } else if (date.getYear() == LocalDate.now().getYear()) {
            return date.format(DateTimeFormatter.ofPattern("d MMMM"));
        } else {
            return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
        }
    }

    public static LatLng getRandomLocationInsideBounds(LatLngBounds bounds) {
        double lat = randomDoubleInRange(bounds.southwest.latitude, bounds.northeast.latitude);
        double lon = randomDoubleInRange(bounds.southwest.longitude, bounds.northeast.longitude);
        return new LatLng(lat, lon);
    }

    public static double randomDoubleInRange(double a, double b) {
        double range = Math.abs(b - a);
        return random.nextDouble() * range + Math.min(a, b);
    }
}
