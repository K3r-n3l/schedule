package ru.university.schedule.model;

import java.util.Optional;

public enum LessonKind {
    LESSON("Занятие", "lesson"),
    LECTURE("Лекция", "lecture"),
    PRACTICE("Практика", "practice"),
    CANCELLED("Отмененное занятие", "cancelled");

    public final String displayName;
    public final String csvName;

    public static Optional<LessonKind> fromCsvName(String csvName) {
        return switch (csvName) {
            case "lesson" -> Optional.of(LessonKind.LESSON);
            case "lecture" -> Optional.of(LessonKind.LECTURE);
            case "practice" -> Optional.of(LessonKind.PRACTICE);
            case "cancelled" -> Optional.of(LessonKind.CANCELLED);
            default -> Optional.empty();
        };
    }

    LessonKind(String displayName, String csvName) {
        this.displayName = displayName;
        this.csvName = csvName;
    }
}