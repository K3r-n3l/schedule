package ru.university.schedule.gui;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Optional;

public final class GenerateDialog extends Dialog<GenerateDialog.Params> {
    public record Params(int count, int days, LocalDate startDay) { }

    private static final int MAX_COUNT = 100_000;
    private static final int MAX_DAYS = 365;
    private static final Insets FORM_PADDING = new Insets(12);
    private static final int GRID_GAP = 8;

    private final TextField countField = new TextField("100");
    private final TextField daysField = new TextField("14");

    private Params draftResult;

    private GenerateDialog(Window owner) {
        setTitle("Генерация данных");
        initOwner(owner);

        GridPane grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(FORM_PADDING);
        grid.add(new Label("Количество занятий (1–" + MAX_COUNT + "):"), 0, 0);
        grid.add(countField, 1, 0);
        grid.add(new Label("Период, дней (1–" + MAX_DAYS + "):"), 0, 1);
        grid.add(daysField, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.control.Button okButton =
                (javafx.scene.control.Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            draftResult = buildAndValidate();
            if (draftResult == null) event.consume();
        });

        setResultConverter(button -> button == ButtonType.OK ? draftResult : null);
    }

    public static Optional<Params> show(Window owner) {
        return new GenerateDialog(owner).showAndWait();
    }

    private Params buildAndValidate() {
        int count = parseInt(countField.getText(), 1, MAX_COUNT, "Количество");
        int days = parseInt(daysField.getText(), 1, MAX_DAYS, "Период");
        if (count == -1 || days == -1) return null;
        return new Params(count, days, LocalDate.now());
    }

    private int parseInt(String raw, int min, int max, String fieldName) {
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < min || value > max) {
                Dialogs.error(getOwner(), "Ошибка ввода",
                        fieldName + ": должно быть от " + min + " до " + max);
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            Dialogs.error(getOwner(), "Ошибка ввода",
                    fieldName + ": введите целое число");
            return -1;
        }
    }
}