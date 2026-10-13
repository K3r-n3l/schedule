package ru.university.schedule;

import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;
import ru.university.schedule.store.AvlLessonStore;
import ru.university.schedule.store.LessonKey;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main() {
        Lesson lecture = new Lecture(
                LocalDateTime.parse("2026-06-06T14:30:00"),
                "ИВТ-552",
                "Программирование",
                "7-218",
                "mr.prepod",
                "first stream"
        );

        Lesson practice = new Practice(
                LocalDateTime.parse("2026-06-06T10:30:00"),
                "АБ-625",
                "Элтех",
                "999",
                "prepod 2",
                2
        );

        Lesson lesson = new Lesson(
                LocalDateTime.parse("2026-06-06T13:30:00"),
                "ИВТ-552",
                "Занятие",
                "7-218",
                "mr.prepod"
        );

        Lesson cancelled = new Cancelled(
                LocalDateTime.parse("2026-06-06T12:32:00"),
                "РРРФВ",
                "Забыл",
                "1221",
                "eaa"
        );

        AvlLessonStore<LessonKey, Lesson> tree = new AvlLessonStore<>();

        tree.add(new LessonKey(lecture.getDateTime(), lecture.getGroup()), lecture);
        tree.add(new LessonKey(practice.getDateTime(), practice.getGroup()), practice);
        tree.add(new LessonKey(cancelled.getDateTime(), cancelled.getGroup()), cancelled);
        tree.add(new LessonKey(lesson.getDateTime(), lesson.getGroup()), lesson);

    }
}
