package ru.university.schedule.gui;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public final class KeyInputDialog extends Dialog<KeyInputDialog.KeyInput> {
    public record KeyInput(LocalDateTime dateTime, String group) { }

    private static final DateTimeFormatter UI_TIME = DateTimeFormatter.ofPattern("H:mm");
    private static final Insets FORM_PADDING = new Insets(12);
    private static final int GRID_GAP = 8;

    private final DatePicker datePicker = new DatePicker();
    private final TextField timeField = new TextField();
    private final TextField groupField = new TextField();
    private final Label errorLabel = new Label();

    private KeyInput draftResult;

    private KeyInputDialog(Window owner, String title) {
        setTitle(title);
        initOwner(owner);

        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        GridPane grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(FORM_PADDING);

        grid.add(new Label("Дата:"), 0, 0);
        grid.add(datePicker, 1, 0);

        grid.add(new Label("Время (ч:мм):"), 0, 1);
        grid.add(timeField, 1, 1);

        grid.add(new Label("Группа:"), 0, 2);
        grid.add(groupField, 1, 2);

        getDialogPane().setContent(new javafx.scene.layout.VBox(errorLabel, grid));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.control.Button okButton =
                (javafx.scene.control.Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            draftResult = buildAndValidate();
            if (draftResult == null) event.consume();
        });

        setResultConverter(button -> button == ButtonType.OK ? draftResult : null);
    }

    public static Optional<KeyInput> show(Window owner, String title) {
        return new KeyInputDialog(owner, title).showAndWait();
    }

    private KeyInput buildAndValidate() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        LocalDate date = datePicker.getValue();
        if (date == null) errors.add("Выберите дату");

        LocalTime time = null;
        try {
            time = LocalTime.parse(timeField.getText().trim(), UI_TIME);
        } catch (DateTimeParseException e) {
            errors.add("Введите время");
        }

        String group = groupField.getText().trim();
        if (group.isEmpty()) errors.add("Укажите группу");

        if (!errors.isEmpty()) {
            errorLabel.setText(String.join("\n", errors));
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return null;
        }
        return new KeyInput(LocalDateTime.of(date, time), group);
    }
}