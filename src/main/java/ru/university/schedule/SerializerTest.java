package ru.university.schedule;

import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;
import ru.university.schedule.serializer.Serializer;
import ru.university.schedule.serializer.XmlSerializer;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class SerializerTest {
    public void main() {
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


//        Serializer serbin = new BinarySerializer();
//        serbin.save(lecture, Path.of("ser"));
//
//        Lesson loaded = serbin.load(Path.of("ser"));
//        if (loaded != null) {
//            System.out.println("Loaded:" + loaded);
//            System.out.println("Origin:" + lecture);
//            System.out.println("Equals? " + lecture.equals(loaded));
//        }

        Serializer serxml = new XmlSerializer();
        serxml.save(lecture, Path.of("ser.xml"));

        Lesson loaded = serxml.load(Path.of("ser.xml"));

        if (loaded != null) {
            System.out.println("Loaded:" + loaded);
            System.out.println("Origin:" + lecture);
            System.out.println("Equals? " + lecture.equals(loaded));
        }
    }
}
