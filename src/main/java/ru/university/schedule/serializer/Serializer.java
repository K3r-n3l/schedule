package ru.university.schedule.serializer;

import ru.university.schedule.model.Lesson;

import java.nio.file.Path;
import java.util.List;

public interface Serializer {
    String format();
    void save(List<Lesson> lessons, Path path);
    List<Lesson> load(Path path);
    default LoadResult loadWithReport(Path path) {
        return LoadResult.of(load(path));
    }
}
