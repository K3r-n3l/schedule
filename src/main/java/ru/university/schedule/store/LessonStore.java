package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.util.Optional;

public interface LessonStore extends EntityStore<LessonKey, Lesson> {
    Optional<Lesson> findNearestLesson(String group);
}
