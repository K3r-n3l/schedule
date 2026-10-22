package ru.university.schedule.serializer;

import ru.university.schedule.model.Lesson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BinarySerializer implements Serializer {
    private static final String format = "bin";

    @Override
    public String format() {
        return format;
    }

    @Override
    public void save(List<Lesson> lessons, Path path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(new ArrayList<>(lessons));
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл: " + path, e);
        }
    }

    @Override
    public List<Lesson> load(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            return (List<Lesson>) ois.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать файл: " + path, e);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException("Файл повреждён: " + e.getMessage(), e);
        }
    }
}
