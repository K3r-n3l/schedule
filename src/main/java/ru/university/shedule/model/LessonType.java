package ru.university.shedule.model;

import java.util.Optional;

public enum LessonType {
    LESSON("Занятие"),
    PRACTICE("Практика"),
    LECTURE("Лекция"),
    CANCELLED("Отмененное занятие");

    private final String displayName;

    LessonType(String displayName) { this.displayName = displayName; }
    public String getName()        { return displayName; }

    public Optional<LessonType> typeByName(String name) {
        for (LessonType t : LessonType.values()) {
            if (t.displayName.equals(name)) return Optional.of(t);
        }
        return Optional.empty();
    }
}
