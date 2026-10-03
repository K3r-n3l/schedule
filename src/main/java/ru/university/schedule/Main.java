package ru.university.schedule;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.university.schedule.gui.Dialogs;
import ru.university.schedule.gui.LessonDialog;
import ru.university.schedule.csv.CsvFormat;
import ru.university.schedule.csv.CsvLoader;
import ru.university.schedule.csv.CsvSaver;
import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Editable;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class Main extends Application {
    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 600;
    private static final String APP_TITLE = "Расписание занятий";
    private static final Insets PADDING_ROOT = new Insets(8);
    private static final double STATUS_SPACING = 12;
    private static final int MAX_SKIPPED_IN_DIALOG = 20;
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final String STYLE_CANCELLED_ROW = "-fx-background-color: #ffd9d9;";

    private final ObservableList<Lesson> items = FXCollections.observableArrayList();
    private final TableView<Lesson> table = new TableView<>(items);

    private Stage stage;
    private Label statusLabel;
    private Path currentFile;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Scene scene = new Scene(buildRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildRoot() {
        VBox root = new VBox(buildToolbar(), buildTable(), buildStatusBar());
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private ToolBar buildToolbar() {
        Button loadButton = new Button("Загрузить CSV");
        loadButton.setOnAction(e -> onLoadCsv());

        Button saveButton = new Button("Сохранить CSV");
        saveButton.setOnAction(e -> onSaveCsv());

        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> onAdd());

        Button editButton = new Button("Изменить");
        editButton.setOnAction(e -> onEdit());
        editButton.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, was, item) ->
                        editButton.setDisable(!(item instanceof Editable)));

        return new ToolBar(loadButton, saveButton, new Separator(), addButton, editButton);
    }

    private TableView<Lesson> buildTable() {
        table.setPlaceholder(new Label("Нет данных — загрузите CSV или добавьте занятия"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Lesson, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().kind().getdisplayName()));

        TableColumn<Lesson, String> dateTimeCol = new TableColumn<>("Дата и время");
        dateTimeCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getDateTime().format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                )));

        TableColumn<Lesson, String> groupCol = new TableColumn<>("Группа");
        groupCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getGroup()));

        TableColumn<Lesson, String> subjectCol = new TableColumn<>("Дисциплина");
        subjectCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getSubject()));

        TableColumn<Lesson, String> roomCol = new TableColumn<>("Аудитория");
        roomCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getRoom()));

        TableColumn<Lesson, String> teacherCol = new TableColumn<>("Преподаватель");
        teacherCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getTeacher()));

        TableColumn<Lesson, String> streamCol = new TableColumn<>("Поток");
        streamCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue() instanceof Lecture lecture ? lecture.getStream() : ""));

        TableColumn<Lesson, String> subgroupCol = new TableColumn<>("Подгруппа");
        subgroupCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                cd.getValue() instanceof Practice practice
                        ? String.valueOf(practice.getSubgroup()) : ""));

        table.getColumns().addAll(typeCol, dateTimeCol, groupCol, subjectCol,
                roomCol, teacherCol, streamCol, subgroupCol);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Lesson item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(item instanceof Cancelled ? STYLE_CANCELLED_ROW : "");
                }
            }
        });

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == DOUBLE_CLICK_COUNT
                    && table.getSelectionModel().getSelectedItem() != null) {
                onEdit();
            }
        });

        return table;
    }

    private HBox buildStatusBar() {
        Label countLabel = new Label();
        countLabel.textProperty().bind(
                Bindings.format("Записей: %d", Bindings.size(items)));

        statusLabel = new Label("Готово");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(STATUS_SPACING, countLabel, spacer, statusLabel);
        bar.setPadding(PADDING_ROOT);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void onLoadCsv() {
        File file = csvChooser("Загрузить CSV").showOpenDialog(stage);
        if (file == null) return;

        try {
            CsvLoader.LoadResult result = new CsvLoader().load(file.toPath());
            items.setAll(result.lessons());
            currentFile = file.toPath();
            updateTitle();
            status("Загружено занятий: " + result.lessons().size());

            if (result.hasSkipped()) {
                showSkippedReport(result.skipped());
            }

        } catch (IOException e) {
            Dialogs.error(stage, "Не удалось прочитать файл", e.getMessage());
        }
    }

    private void onSaveCsv() {
        FileChooser chooser = csvChooser("Сохранить CSV");
        chooser.setInitialFileName("schedule" + CsvFormat.EXTENSION);
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        Path path = ensureCsvExtension(file.toPath());
        try {
            new CsvSaver().save(path, items);
            currentFile = path;
            updateTitle();
            status("Сохранено записей: " + items.size());
        } catch (IOException e) {
            Dialogs.error(stage, "Не удалось сохранить файл", e.getMessage());
        }
    }

    private void onAdd() {
        LessonDialog.showForAdd(stage).ifPresent(lesson -> {
            items.add(lesson);
            status("Добавлено: " + lesson.getSubject());
        });
    }

    private void onEdit() {
        Lesson selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!(selected instanceof Editable)) {
            Dialogs.info(stage, "Редактирование недоступно",
                    "«" + selected.kind().getdisplayName() + "» — read-only запись, изменение запрещено.");
            return;
        }

        LessonDialog.showForEdit(stage, selected).ifPresent(updated -> {
            items.set(table.getSelectionModel().getSelectedIndex(), updated);
            status("Изменено: " + updated.getSubject());
        });
    }

    private FileChooser csvChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV-файлы", "*" + CsvFormat.EXTENSION));
        return chooser;
    }

    private Path ensureCsvExtension(Path path) {
        String name = path.getFileName().toString();
        return name.toLowerCase().endsWith(CsvFormat.EXTENSION)
                ? path
                : path.resolveSibling(name + CsvFormat.EXTENSION);
    }

    private void showSkippedReport(List<CsvLoader.SkippedLine> skipped) {
        StringBuilder text = new StringBuilder(
                "Загружено с пропусками. Пропущено строк: " + skipped.size() + "\n\n");
        int shown = 0;
        for (CsvLoader.SkippedLine row : skipped) {
            if (shown++ == MAX_SKIPPED_IN_DIALOG) {
                text.append("… и ещё ").append(skipped.size() - shown + 1).append("\n");
                break;
            }
            text.append("строка ").append(row.lineNumber()).append(": ")
                    .append(row.reason()).append("\n");
        }
        Dialogs.info(stage, "Файл загружен частично", text.toString());
    }

    private void updateTitle() {
        stage.setTitle(currentFile == null
                ? APP_TITLE
                : APP_TITLE + " — " + currentFile.getFileName());
    }

    private void status(String message) {
        statusLabel.setText(message);
    }
}