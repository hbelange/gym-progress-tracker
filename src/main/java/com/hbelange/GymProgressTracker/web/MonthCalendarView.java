package com.hbelange.GymProgressTracker.web;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Month-grid view data shared by calendar-style pages (workouts, measurements). */
public record MonthCalendarView(
    List<List<LocalDate>> weeks,
    String monthLabel,
    int prevYear,
    int prevMonth,
    int nextYear,
    int nextMonth,
    LocalDate today
) {

    public static MonthCalendarView of(Integer year, Integer month) {
        YearMonth yearMonth = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();

        List<LocalDate> days = new ArrayList<>();
        int leadingBlanks = yearMonth.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < leadingBlanks; i++) {
            days.add(null);
        }
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            days.add(yearMonth.atDay(day));
        }
        while (days.size() % 7 != 0) {
            days.add(null);
        }

        List<List<LocalDate>> weeks = new ArrayList<>();
        for (int i = 0; i < days.size(); i += 7) {
            weeks.add(days.subList(i, i + 7));
        }

        YearMonth previousMonth = yearMonth.minusMonths(1);
        YearMonth nextMonth = yearMonth.plusMonths(1);
        String monthLabel = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear();

        return new MonthCalendarView(
            weeks,
            monthLabel,
            previousMonth.getYear(),
            previousMonth.getMonthValue(),
            nextMonth.getYear(),
            nextMonth.getMonthValue(),
            LocalDate.now()
        );
    }
}
