package ru.university.schedule.gui;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public final class Files {
    public record Choice(Path path, FileFormat format) { }

    private Files() { }

    public static Optional<Choice> chooseOpen(Stage stage, String title) {
        FileChooser chooser = chooser(title);
        File file = chooser.showOpenDialog(stage);
        if (file == null) return Optional.empty();
        Path path = file.toPath();
        return Optional.of(new Choice(path, resolveFormat(path, chooser)));
    }

    public static Optional<Choice> chooseSave(Stage stage, String title) {
        FileChooser chooser = chooser(title);
        chooser.setInitialFileName("schedule");
        File file = chooser.showSaveDialog(stage);
        if (file == null) return Optional.empty();
        FileFormat format = FileFormat.byFilter(chooser.getSelectedExtensionFilter())
                .orElse(FileFormat.JSON);
        return Optional.of(new Choice(ensureExtension(file.toPath(), format), format));
    }

    public static Path ensureExtension(Path path, FileFormat format) {
        String name = path.getFileName().toString();
        String suffix = "." + format.extension();
        return name.toLowerCase().endsWith(suffix) ? path : path.resolveSibling(name + suffix);
    }

    private static FileFormat resolveFormat(Path path, FileChooser chooser) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        Optional<FileFormat> byExt = dot < 0
                ? Optional.empty() : FileFormat.byExtension(name.substring(dot + 1));
        return byExt
                .or(() -> FileFormat.byFilter(chooser.getSelectedExtensionFilter()))
                .orElse(FileFormat.JSON);
    }

    private static FileChooser chooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        for (FileFormat format : FileFormat.values()) {
            chooser.getExtensionFilters().add(format.filter());
        }
        return chooser;
    }
}
