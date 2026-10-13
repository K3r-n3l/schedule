package ru.university.schedule.csv;

import ru.university.schedule.model.LessonKind;

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

        public final int index;
        Fields(int index) { this.index = index; }
    }

    public static final String DELIMITER = ";";
    public static final int COLUMN_COUNT = Fields.values().length;
    public static final String EXTENSION = ".csv";

    public static final String HEADER =
            "type;dateTime;group;subject;room;teacher;stream;subgroup";

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private CsvFormat() { }
}
