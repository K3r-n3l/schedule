package ru.university.schedule.csv;

import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvSaver {
    public static void save(Path file, List<Lesson> lessons) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(CsvFormat.HEADER);
            writer.newLine();

            for (Lesson l : lessons) {
                writer.write(toCsv(l));
                writer.newLine();
            }
        }
    }

    private static String toCsv(Lesson lesson) {
        String stream = "";
        String subgroup = "";

        if (lesson instanceof Lecture lecture)   stream = lecture.getStream();
        if (lesson instanceof Practice practice) subgroup = String.valueOf(practice.getSubgroup());

        // "type;dateTime;group;subject;room;teacher;stream;subgroup";
        return String.join(CsvFormat.DELIMITER,
                CsvFormat.typeLabel(lesson.kind()),
                lesson.getDateTime().format(CsvFormat.DATE_TIME),
                lesson.getGroup(),
                lesson.getSubject(),
                lesson.getRoom(),
                lesson.getTeacher(),
                stream, subgroup);
    }
}
