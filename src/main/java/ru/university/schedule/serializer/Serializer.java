package ru.university.schedule.serializer;

import ru.university.schedule.model.Lesson;
import java.nio.file.Path;

public interface Serializer {
    String format();
    void save(Lesson lesson, Path path);
    Lesson load(Path path);
}
