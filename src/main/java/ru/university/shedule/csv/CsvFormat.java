package ru.university.shedule.csv;

import ru.university.shedule.model.LessonType;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class CsvFormat {

    public enum Fields {
        TYPE(0),
        DATE_TIME(1),
        GROUP(2),
        SUBJECT(3),
        ROOM(4),
        TEACHER(5),
        STREAM(6),
        SUBGROUP(7);

        private final int index;

        Fields(int index) {
            this.index = index;
        }

        public int getIndex() { return index; }
    }

    public static final String DELIMITER = ";";
    public static final int COLUMN_COUNT = Fields.values().length;
    public static final String EXTENSION = ".csv";

    public static final String HEADER =
            "type;dateTime;group;subject;room;teacher;stream;subgroup";

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String getTypeLabel(LessonType type) {
        return switch (type) {
            case LESSON -> "lesson";
            case LECTURE -> "lecture";
            case PRACTICE -> "practice";
            case CANCELLED -> "cancelled";
        };
    }

    public static Optional<LessonType> parseLabelType(String label) {
        return switch (label) {
            case "lesson" -> Optional.of(LessonType.LESSON);
            case "lecture" -> Optional.of(LessonType.LECTURE);
            case "practice" -> Optional.of(LessonType.PRACTICE);
            case "cancelled" -> Optional.of(LessonType.CANCELLED);
            default -> Optional.empty();
        };
    }

    private CsvFormat() {
    }
}
