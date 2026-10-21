package ru.university.schedule.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.university.schedule.model.Lesson;

import java.io.IOException;
import java.nio.file.Path;

public class XmlSerializer implements Serializer {
    private static final String format = "xml";
    private final XmlMapper objectMapper;

    public XmlSerializer() {
        objectMapper = new XmlMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public void save(Lesson lesson, Path path) {
        try {
            objectMapper.writeValue(path.toFile(), lesson);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Lesson load(Path path) {
        try {
            Lesson loaded = objectMapper.readValue(path.toFile(), Lesson.class);
            return loaded;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
