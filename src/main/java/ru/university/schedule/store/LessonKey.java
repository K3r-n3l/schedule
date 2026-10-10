package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.time.LocalDateTime;
import java.util.Comparator;

public record LessonKey(LocalDateTime dateTime, String group)
        implements Comparable<LessonKey> {

    public static LessonKey of(Lesson lesson) {
        return new LessonKey(lesson.getDateTime(), lesson.getGroup());
    }

    private static final Comparator<LessonKey> ORDER =
            Comparator.comparing(LessonKey::dateTime)
                    .thenComparing(LessonKey::group);

    @Override
    public int compareTo(LessonKey other) {
        return ORDER.compare(this, other);
    }
}