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
    public void save(Lesson lesson, Path path) {
        try (ObjectOutputStream oss = new ObjectOutputStream(Files.newOutputStream(path))) {
            oss.writeObject(lesson);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Lesson load(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            Lesson loaded = (Lesson) ois.readObject();
            return loaded;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
