package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.time.LocalDateTime;

public interface LessonStore {
    void add(Lesson lesson);
    Lesson findByDateTime(LocalDateTime dateTime);
    boolean removeByDateTime(LocalDateTime dateTime);
    int size();
}
