package ru.university.schedule.csv;

import ru.university.schedule.model.*;

import static ru.university.schedule.csv.CsvFormat.Fields.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class CsvLoader {
    public record SkippedLine(int lineNumber, CsvErrorCode code, String reason) {
    }

    public record LoadResult(List<Lesson> lessons, List<SkippedLine> skipped) {
        public boolean hasSkipped() {
            return skipped != null && !skipped.isEmpty();
        }
    }

    public static LoadResult load(Path file) throws IOException {
        ArrayList<Lesson> lessons = new ArrayList<>();
        ArrayList<SkippedLine> skipped = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                if (line.isBlank()) {
                    continue;
                }
                if (lineNumber == 1 && line.equals(CsvFormat.HEADER)) {
                    continue;
                }

                try {
                    lessons.add(parseLine(lineNumber, line));
                } catch (CsvLineException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return new LoadResult(lessons, skipped);
    }

    private static Lesson parseLine(int lineNumber, String line) throws CsvLineException {
        String[] parts = line.split(CsvFormat.DELIMITER, -1);

        if (parts.length != CsvFormat.COLUMN_COUNT) {
            throw new CsvLineException(lineNumber, CsvErrorCode.WRONG_FIELD_COUNT,
                    "ожидалось %d полей, получено %d".formatted(CsvFormat.COLUMN_COUNT, parts.length));
        }

        LocalDateTime dateTime = parseDateTime(parts[DATE_TIME.ordinal()], lineNumber);



        LessonKind type = CsvFormat.parseTypeLabel(parts[0])
                .orElseThrow(() -> new CsvLineException(lineNumber, CsvErrorCode.UNKNOWN_TYPE, parts[0]));

        final String group = parts[GROUP.index()];
        final String subject = parts[SUBJECT.index()];
        final String room = parts[ROOM.index()];
        final String teacher = parts[TEACHER.index()];

        final Lesson result = switch (type) {
            case LESSON -> new Lesson(dateTime, group, subject, room, teacher);

            case LECTURE -> new Lecture(dateTime, group, subject, room, teacher,
                    parts[STREAM.index()]);

            case PRACTICE -> new Practice(dateTime, group, subject, room, teacher,
                    parseSubgroup(parts[SUBGROUP.index()], lineNumber));

            case CANCELLED -> new Cancelled(dateTime, group, subject, room, teacher);
        };

        List<String> errors = result.validate();

        if (!errors.isEmpty()) {
            throw new CsvLineException(lineNumber, CsvErrorCode.INVALID_DATA,
                    String.join("; ", errors));
        }

        return result;
    }

    private static LocalDateTime parseDateTime(String dateTime, int lineNumber) throws CsvLineException {
        try {
            return LocalDateTime.parse(dateTime, CsvFormat.DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new CsvLineException(lineNumber, CsvErrorCode.BAD_DATETIME, dateTime);
        }
    }

    private static int parseSubgroup(String subgroup, int lineNumber) throws CsvLineException {
        try {
            return Integer.parseInt(subgroup);
        } catch (NumberFormatException e) {
            throw new CsvLineException(lineNumber, CsvErrorCode.BAD_NUMBER, subgroup);
        }
    }
}
