package ru.university.schedule.gui;

import ru.university.schedule.model.Editable;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LessonDialog extends Dialog<Lesson> {

    private enum LessonKind {
        LECTURE("Лекция"),
        PRACTICE("Практика");

        private final String title;

        LessonKind(String title) { this.title = title; }

        @Override public String toString() { return title; }   // так ComboBox показывает имя
    }

    private static final DateTimeFormatter UI_TIME = DateTimeFormatter.ofPattern("H:mm");
    private static final Insets FORM_PADDING = new Insets(12);
    private static final int GRID_GAP = 8;
    private static final double FIELD_WIDTH = 240;
    private static final String STYLE_ERROR = "-fx-text-fill: #b00020;";

    private final ComboBox<LessonKind> kindCombo = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final TextField timeField = new TextField();
    private final TextField groupField = new TextField();
    private final TextField subjectField = new TextField();
    private final TextField roomField = new TextField();
    private final TextField teacherField = new TextField();
    private final TextField detailsField = new TextField();
    private final Label detailsLabel = new Label("Поток:");
    private final Label errorLabel = new Label();

    private Lesson draftResult;

    public LessonDialog(Window owner, Lesson original) {
        setTitle(original == null ? "Добавление занятия" : "Редактирование занятия");
        initOwner(owner);
        setResizable(false);

        if (original == null) {
            setupForAdd();
        } else {
            setupForEdit(original);
        }
        detailsField.setPrefWidth(FIELD_WIDTH);

        buildLayout();

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            draftResult = buildAndValidate();
            if (draftResult == null) {
                event.consume();
            }
        });

        setResultConverter(button -> button == ButtonType.OK ? draftResult : null);
    }

    public static Optional<Lesson> showForAdd(Window owner) {
        return new LessonDialog(owner, null).showAndWait();
    }

    public static Optional<Lesson> showForEdit(Window owner, Lesson original) {
        return new LessonDialog(owner, original).showAndWait();
    }

    private void setupForAdd() {
        kindCombo.getItems().setAll(LessonKind.values());
        kindCombo.valueProperty().addListener((obs, old, kind) -> updateDetailsLabel());
    }

    private void setupForEdit(Lesson original) {
        if (original instanceof Lecture lecture) {
            kindCombo.getItems().setAll(LessonKind.LECTURE);
            kindCombo.setValue(LessonKind.LECTURE);
            detailsField.setText(lecture.getStream());
        } else if (original instanceof Practice practice) {
            kindCombo.getItems().setAll(LessonKind.PRACTICE);
            kindCombo.setValue(LessonKind.PRACTICE);
            detailsField.setText(String.valueOf(practice.getSubgroup()));
        }
        kindCombo.setDisable(true);

        datePicker.setValue(original.getDateTime().toLocalDate());
        timeField.setText(original.getDateTime().toLocalTime().format(UI_TIME));
        groupField.setText(original.getGroup());
        subjectField.setText(original.getSubject());
        roomField.setText(original.getRoom());
        teacherField.setText(original.getTeacher());
        updateDetailsLabel();
    }

    private void updateDetailsLabel() {
        detailsLabel.setText(kindCombo.getValue() == LessonKind.PRACTICE ? "Подгруппа:" : "Поток:");
    }

    private void buildLayout() {
        errorLabel.setStyle(STYLE_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        GridPane grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(FORM_PADDING);

        int row = 0;
        grid.add(new Label("Тип:"), 0, row);          grid.add(kindCombo, 1, row++);
        grid.add(new Label("Дата:"), 0, row);         grid.add(datePicker, 1, row++);
        grid.add(new Label("Время (ч:мм):"), 0, row); grid.add(timeField, 1, row++);
        grid.add(new Label("Группа:"), 0, row);       grid.add(groupField, 1, row++);
        grid.add(new Label("Дисциплина:"), 0, row);   grid.add(subjectField, 1, row++);
        grid.add(new Label("Аудитория:"), 0, row);    grid.add(roomField, 1, row++);
        grid.add(new Label("Преподаватель:"), 0, row);grid.add(teacherField, 1, row++);
        grid.add(detailsLabel, 0, row);               grid.add(detailsField, 1, row);

        getDialogPane().setContent(new VBox(errorLabel, grid));
    }

    private Lesson buildAndValidate() {
        List<String> errors = new ArrayList<>();

        LocalDate date = datePicker.getValue();
        if (date == null) errors.add("Выберите дату");

        LocalTime time = null;
        try {
            time = LocalTime.parse(timeField.getText().trim(), UI_TIME);
        } catch (DateTimeParseException e) {
            errors.add("Время: введите часы:минуты, например 10:45");
        }

        LessonKind kind = kindCombo.getValue();
        if (kind == null) errors.add("Выберите тип занятия");

        String group = groupField.getText().trim();
        String subject = subjectField.getText().trim();
        String room = roomField.getText().trim();
        String teacher = teacherField.getText().trim();
        String details = detailsField.getText().trim();

        Lesson draft = null;
        if (kind != null && date != null && time != null) {
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            if (kind == LessonKind.LECTURE) {
                draft = new Lecture(dateTime, group, subject, room, teacher, details);
            } else {
                try {
                    draft = new Practice(dateTime, group, subject, room, teacher,
                            Integer.parseInt(details));
                } catch (NumberFormatException e) {
                    errors.add("Подгруппа: введите целое число");
                }
            }
        }

        if (draft instanceof Editable) {
            errors.addAll(draft.validate());
        }

        if (!errors.isEmpty()) {
            showError(errors);
            return null;
        }
        return draft;
    }

    private void showError(List<String> errors) {
        errorLabel.setText(String.join("\n", errors));
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}