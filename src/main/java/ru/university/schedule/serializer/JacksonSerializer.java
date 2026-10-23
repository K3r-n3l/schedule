package ru.university.schedule.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.university.schedule.model.Lesson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class JacksonSerializer implements Serializer {
    public static class ScheduleFile {
        public List<Lesson> lessons = new ArrayList<>();

        public ScheduleFile() { }

        public ScheduleFile(List<Lesson> lessons) {
            this.lessons = lessons;
        }
    }

    protected final String format;
    protected final ObjectMapper mapper;

    public JacksonSerializer(ObjectMapper mapper, String format) {
        this.mapper = mapper;
        this.format = format;

       mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public void save(List<Lesson> lessons, Path path) {
        try {
            mapper.writeValue(path.toFile(), new ScheduleFile(new ArrayList<>(lessons)));
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл: " + path, e);
        }
    }

    @Override
    public List<Lesson> load(Path path) {
        try {
            ScheduleFile file = mapper.readValue(path.toFile(), ScheduleFile.class);
            return file.lessons == null ? List.of() : file.lessons;
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось прочитать файл: " + e.getMessage(), e);
        }
    }
}
