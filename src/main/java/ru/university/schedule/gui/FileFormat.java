package ru.university.schedule.gui;

import javafx.stage.FileChooser;
import ru.university.schedule.serializer.*;

import java.util.Arrays;
import java.util.Optional;

public enum FileFormat {
    CSV     ("CSV (*.csv)",      "csv",    new CsvSerializer()),
    BIN     ("Binary (*.bin)",   "bin",    new BinarySerializer()),
    JSON    ("JSON (*.json)",     "json",  new JsonSerializer()),
    XML     ("XML (*.xml)",       "xml",   new XmlSerializer()),
    YAML    ("YAML (*.yaml)",     "yaml",  new YamlSerializer()),
    PROTOBUF("Protobuf (*.binpb)","binpb", new ProtobufSerializer());

    private final String label;
    private final String extension;
    private final Serializer serializer;

    FileFormat(String label, String extension, Serializer serializer) {
        this.label = label;
        this.extension = extension;
        this.serializer = serializer;
    }

    public Serializer serializer() { return serializer; }

    public String extension() { return extension; }

    public FileChooser.ExtensionFilter filter() {
        return new FileChooser.ExtensionFilter(label, "*." + extension);
    }

    @Override public String toString() { return label; }

    public static Optional<FileFormat> byExtension(String ext) {
        return Arrays.stream(values())
                .filter(f -> f.extension.equalsIgnoreCase(ext))
                .findFirst();
    }

    public static Optional<FileFormat> byFilter(FileChooser.ExtensionFilter filter) {
        if (filter == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(f -> f.label.equals(filter.getDescription()))
                .findFirst();
    }
}
