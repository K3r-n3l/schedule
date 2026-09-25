package ru.university.schedule;

import ru.university.schedule.csv.CsvLoader;
import ru.university.schedule.csv.CsvSaver;
import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class CsvTest {
    public static void main() {
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
                2
        );

        Lesson lesson = new Lesson(
                LocalDateTime.parse("2026-06-06T12:30:00"),
                "ИВТ-552",
                "Занятие",
                "7-218",
                "mr.prepod"
        );

        Lesson cancelled = new Cancelled(
                LocalDateTime.parse("2026-06-06T12:30:00"),
                "РРРФВ",
                "Забыл",
                "1221",
                "eaa"
        );

        final Path path = Path.of("./res.csv");

        List<Lesson> lst = List.of(lecture, practice, cancelled, lesson);

        try {
            CsvSaver.save(path, lst);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            CsvLoader.LoadResult res = CsvLoader.load(path);

            for (Lesson l : res.lessons()) {
                System.out.println(l);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
