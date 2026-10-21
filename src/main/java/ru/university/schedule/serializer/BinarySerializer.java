package ru.university.schedule.serializer;

import ru.university.schedule.model.Lesson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BinarySerializer implements Serializer {
    private static final String format = "binary";

    @Override
    public String format() {
        return format;
    }

    @Override
    public void save(Lesson lesson, Path file) {
        try (ObjectOutputStream oss = new ObjectOutputStream(Files.newOutputStream(file))) {
            oss.writeObject(lesson);
        } catch (IOException e) {}
    }

    @Override
    public Lesson load(Path file) {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            Lesson loaded = (Lesson)ois.readObject();
            return loaded;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
