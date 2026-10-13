package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.util.Optional;

public class AvlLessonStore
        extends AvlStore<LessonKey, Lesson> implements LessonStore {

    public void add(Lesson lesson) {
        LessonKey key = new LessonKey(lesson.getDateTime(), lesson.getGroup());
        add(key, lesson);
    }

    @Override
    public Optional<Lesson> findNearestLesson(String group) {
        return Optional.empty();
    }
}
