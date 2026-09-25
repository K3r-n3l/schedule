package ru.university.schedule.model;

public enum LessonKind {
    LESSON("Занятие"),
    LECTURE("Лекция"),
    PRACTICE("Практика"),
    CANCELLED("Отмененное занятие");

    private final String displayName;

    LessonKind(String displayName) { this.displayName = displayName; }
    public String getdisplayName() { return displayName; }
}