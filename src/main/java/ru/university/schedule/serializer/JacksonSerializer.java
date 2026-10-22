package ru.university.schedule.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.store.AvlLessonStore;

import java.io.IOException;
import java.nio.file.Path;

public abstract class JacksonSerializer implements Serializer {
    protected final String format;
    protected final ObjectMapper mapper;

    public JacksonSerializer(ObjectMapper mapper, String format) {
        this.mapper = mapper;
        this.format = format;

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public void save(Lesson lesson, Path path) {
        try {
            mapper.writeValue(path.toFile(), lesson);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveLessons(AvlLessonStore lessons, Path path) {
        for (Lesson lesson : lessons.snapshot()) {
            save(lesson, path);
        }
    }

    @Override
    public Lesson load(Path path) {
        try {
            Lesson loaded = mapper.readValue(path.toFile(), Lesson.class);
            return loaded;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public AvlLessonStore loadLessons(Path path) {
        AvlLessonStore store = new AvlLessonStore();
        return store;
    }
}
