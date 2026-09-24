package ru.university.shedule;

import ru.university.shedule.model.Cancelled;
import ru.university.shedule.model.Lecture;
import ru.university.shedule.model.Lesson;
import ru.university.shedule.model.Practice;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Lesson lecture = new Lecture(
                LocalDateTime.parse("2026-06-06T12:30:00"),
                "ИВТ-552",
                "Программирование",
                "7-218",
                "mr.prepod",
                "first stream"
        );

        Lesson practice = new Practice(
                LocalDateTime.parse("2026-06-06T12:30:00"),
                "АБ-625",
                "Элтех",
                "999",
                "prepod 2",
                -1
        );

        Lesson cancelled = new Cancelled(
                LocalDateTime.parse("2026-06-06T12:30:00"),
                "РРРФВ",
                "Забыл",
                "1221",
                "eaa"
        );

        List<String> errors = practice.validate();

        if (errors.isEmpty()) System.out.println("bye");
        else {
            for (String err : errors) {
                System.out.println(err);
            }
        }
    }
}
