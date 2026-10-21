package ru.university.schedule.gui;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.university.schedule.csv.CsvFormat;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/** Диалоги выбора CSV-файла и работа с расширением. */
public final class CsvFiles {

    private CsvFiles() { }

    public static Optional<Path> chooseOpen(Stage stage, String title) {
        File file = chooser(title).showOpenDialog(stage);
        return file == null ? Optional.empty() : Optional.of(file.toPath());
    }

    public static Optional<Path> chooseSave(Stage stage, String title) {
        FileChooser chooser = chooser(title);
        chooser.setInitialFileName("schedule" + CsvFormat.EXTENSION);
        File file = chooser.showSaveDialog(stage);
        return file == null ? Optional.empty() : Optional.of(file.toPath());
    }

    /** Добавляет .csv, если пользователь ввёл имя без расширения. */
    public static Path ensureExtension(Path path) {
        String name = path.getFileName().toString();
        return name.toLowerCase().endsWith(CsvFormat.EXTENSION)
                ? path
                : path.resolveSibling(name + CsvFormat.EXTENSION);
    }

    private static FileChooser chooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV-файлы", "*" + CsvFormat.EXTENSION));
        return chooser;
    }
}