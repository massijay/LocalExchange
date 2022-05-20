package com.mcris.localexchange.helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String getFriendlyDate(LocalDate date) {
        // TODO: use string resources
        if (date.equals(LocalDate.now())) {
            return "Oggi";
        } else if (date.equals(LocalDate.now().minusDays(1))) {
            return "Ieri";
        } else if (date.getYear() == LocalDate.now().getYear()) {
            return date.format(DateTimeFormatter.ofPattern("d MMMM"));
        } else {
            return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
        }
    }
}
