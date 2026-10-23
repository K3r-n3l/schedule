package ru.university.schedule.serializer;

import ru.university.schedule.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvSerializer implements Serializer {
    public enum Fields {
        TYPE(0),
        DATE_TIME(1),
        GROUP(2),
        SUBJECT(3),
        ROOM(4),
        TEACHER(5),
        STREAM(6),
        SUBGROUP(7);

        public final int index;

        Fields(int index) {
            this.index = index;
        }
    }

    public enum CsvErrorCode {
        WRONG_FIELD_COUNT("Неверное количество полей"),
        UNKNOWN_TYPE("Неизвестный тип занятия"),
        BAD_DATETIME("Некорректные дата и время"),
        BAD_NUMBER("Ожидалось число"),
        INVALID_DATA("Данные не прошли валидацию");

        private final String description;

        CsvErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static final String format = "csv";
    public static final String DELIMITER = ";";
    public static final int COLUMN_COUNT = Fields.values().length;
    public static final String HEADER = "type;dateTime;group;subject;room;teacher;stream;subgroup";
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public class CsvLineException extends Exception {
        private final int lineNumber;
        private final CsvErrorCode code;

        public CsvLineException(int lineNumber, CsvErrorCode code, String details) {
            super("Строка %d; Код ошибки %s, описание: %s%s".
                    formatted(lineNumber, code, code.getDescription(),
                            details == null || details.isBlank() ? "" : ": " + details));
            this.lineNumber = lineNumber;
            this.code = code;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public CsvErrorCode getCode() {
            return code;
        }
    }

    @Override public String format() {
        return format;
    }

    @Override
    public void save(List<Lesson> lessons, Path file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (Lesson l : lessons) {
                writer.write(toCsv(l));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл: " + file, e);
        }
    }

    private static String toCsv(Lesson lesson) {
        String stream = "";
        String subgroup = "";

        if (lesson instanceof Lecture lecture) stream = lecture.getStream();
        if (lesson instanceof Practice practice) subgroup = String.valueOf(practice.getSubgroup());

        return String.join(DELIMITER,
                lesson.kind().csvName,
                lesson.getDateTime().format(DATE_TIME),
                lesson.getGroup(),
                lesson.getSubject(),
                lesson.getRoom(),
                lesson.getTeacher(),
                stream, subgroup);
    }

    @Override
    public List<Lesson> load(Path path) {
        return loadWithReport(path).lessons();
    }

    @Override
    public LoadResult loadWithReport(Path file) {
        List<Lesson> lessons = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        List<LoadResult.Skipped> skipped = new ArrayList<>();

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
                if (lineNumber == 1 && line.equals(HEADER)) {
                    continue;
                }

                try {
                    lessons.add(parseLine(lineNumber, line));
                    numbers.add(lineNumber);
                } catch (CsvLineException e) {
                    skipped.add(new LoadResult.Skipped(lineNumber, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл: " + file, e);
        }
        return new LoadResult(lessons, numbers, skipped);
    }

    private Lesson parseLine(int lineNumber, String line) throws CsvLineException {
        String[] parts = line.split(DELIMITER, -1);

        if (parts.length != COLUMN_COUNT) {
            throw new CsvLineException(lineNumber, CsvErrorCode.WRONG_FIELD_COUNT,
                    "ожидалось %d полей, получено %d".formatted(COLUMN_COUNT, parts.length));
        }

        LocalDateTime dateTime = parseDateTime(parts[Fields.DATE_TIME.index], lineNumber);


        LessonKind type = LessonKind.fromCsvName(parts[0])
                .orElseThrow(() -> new CsvLineException(lineNumber, CsvErrorCode.UNKNOWN_TYPE, parts[0]));

        final String group = parts[Fields.GROUP.index];
        final String subject = parts[Fields.SUBJECT.index];
        final String room = parts[Fields.ROOM.index];
        final String teacher = parts[Fields.TEACHER.index];

        final Lesson result = switch (type) {
            case LESSON -> new Lesson(dateTime, group, subject, room, teacher);

            case LECTURE -> new Lecture(dateTime, group, subject, room, teacher,
                    parts[Fields.STREAM.index]);

            case PRACTICE -> new Practice(dateTime, group, subject, room, teacher,
                    parseSubgroup(parts[Fields.SUBGROUP.index], lineNumber));

            case CANCELLED -> new Cancelled(dateTime, group, subject, room, teacher);
        };

        List<String> errors = result.validate();

        if (!errors.isEmpty()) {
            throw new CsvLineException(lineNumber, CsvErrorCode.INVALID_DATA,
                    String.join("; ", errors));
        }

        return result;
    }

    private LocalDateTime parseDateTime(String dateTime, int lineNumber) throws CsvLineException {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new CsvLineException(lineNumber, CsvErrorCode.BAD_DATETIME, dateTime);
        }
    }

    private int parseSubgroup(String subgroup, int lineNumber) throws CsvLineException {
        try {
            return Integer.parseInt(subgroup);
        } catch (NumberFormatException e) {
            throw new CsvLineException(lineNumber, CsvErrorCode.BAD_NUMBER, subgroup);
        }
    }
}
