package ru.university.schedule.gui;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public final class Files {
    private Files() { }

    public static Optional<Path> chooseOpen(Stage stage, String title) {
        File file = chooser(title).showOpenDialog(stage);
        return file == null ? Optional.empty() : Optional.of(file.toPath());
    }

    public static Optional<Path> chooseSave(Stage stage, String title, String format) {
        FileChooser chooser = chooser(title);
        chooser.setInitialFileName("schedule" + format);
        File file = chooser.showSaveDialog(stage);
        return file == null ? Optional.empty() : Optional.of(file.toPath());
    }

    public static Path ensureExtension(Path path, String format) {
        String name = path.getFileName().toString();
        return name.toLowerCase().endsWith(format)
                ? path
                : path.resolveSibling(name +format);
    }

    private static FileChooser chooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Файлы"));
        return chooser;
    }
}