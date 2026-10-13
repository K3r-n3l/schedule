package ru.university.schedule.gui;

import java.time.format.DateTimeFormatter;

public final class DisplayFormat {
    public static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final DateTimeFormatter TIME_ONLY =
            DateTimeFormatter.ofPattern("HH:mm");

    private DisplayFormat() { }
}