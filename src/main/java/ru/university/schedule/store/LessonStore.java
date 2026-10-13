package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LessonStore extends EntityStore<LessonKey, Lesson> {
    Optional<Lesson> findNearest(LocalDateTime target, String group);
}
