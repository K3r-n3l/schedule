package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.time.LocalDateTime;

public class AvlLessonStore implements LessonStore {
    @Override
    public void add(Lesson lesson) {

    }

    @Override
    public Lesson findByDateTime(LocalDateTime dateTime) {
        return null;
    }

    @Override
    public boolean removeByDateTime(LocalDateTime dateTime) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}
