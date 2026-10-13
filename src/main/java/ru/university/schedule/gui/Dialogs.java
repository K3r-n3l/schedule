package ru.university.schedule.gui;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public final class Dialogs {

    private Dialogs() { }

    public static void error(Window owner, String header, String content) {
        show(Alert.AlertType.ERROR, owner, header, content);
    }

    public static void info(Window owner, String header, String content) {
        show(Alert.AlertType.INFORMATION, owner, header, content);
    }

    private static void show(Alert.AlertType type, Window owner, String header, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setHeaderText(header);
        alert.setContentText(content == null || content.isBlank() ? null : content);
        alert.showAndWait();
    }
}