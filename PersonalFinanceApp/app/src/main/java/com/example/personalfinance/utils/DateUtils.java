package com.example.personalfinance.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_FORMAT = "dd/MM/yyyy";

    public static String getCurrentDateString() {
        return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).format(new Date());
    }

    public static String formatDateForDisplay(String serverDate) {
        if (serverDate == null || serverDate.isBlank()) return "";
        try {
            // Trim any timestamp info if backend returns ISO-8601 LocalDateTime
            String dateOnly = serverDate.contains("T") ? serverDate.split("T")[0] : serverDate;
            Date parsed = new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).parse(dateOnly);
            if (parsed != null) {
                return new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault()).format(parsed);
            }
        } catch (Exception ignored) {}
        return serverDate;
    }

    public static String getFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).format(cal.getTime());
    }

    public static String getLastDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).format(cal.getTime());
    }

    public static String getFirstDayOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).format(cal.getTime());
    }

    public static String getLastDayOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).format(cal.getTime());
    }
}
