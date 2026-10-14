package ru.university.schedule;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import ru.university.schedule.gui.*;
import ru.university.schedule.csv.CsvFormat;
import ru.university.schedule.csv.CsvLoader;
import ru.university.schedule.csv.CsvSaver;
import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Editable;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;
import ru.university.schedule.store.AvlLessonStore;
import ru.university.schedule.store.LessonKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main extends Application {
    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 700;
    private static final String APP_TITLE = "Расписание занятий";
    private static final Insets PADDING_ROOT = new Insets(8);
    private static final double STATUS_SPACING = 12;
    private static final int MAX_SKIPPED_IN_DIALOG = 20;
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final String STYLE_CANCELLED_ROW = "-fx-background-color: #ffd9d9;";

    private final AvlLessonStore store = new AvlLessonStore();

    private final ObservableList<Lesson> items = FXCollections.observableArrayList();
    private final TableView<Lesson> table = new TableView<>(items);

    private Stage stage;
    private Label statusLabel;
    private Path currentFile;
    private final TreeCanvasPanel treePanel = new TreeCanvasPanel();

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Tab visualTab = new Tab("Визуализация AVL", buildVisualRoot());
        visualTab.setClosable(false);
        Tab tableTab = new Tab("Таблица расписания", buildTableRoot());
        tableTab.setClosable(false);

        Scene scene = new Scene(new TabPane(visualTab, tableTab), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.show();

        refreshView();
    }

    private VBox buildVisualRoot() {
        Button loadButton = new Button("Загрузить CSV");
        loadButton.setOnAction(e -> onLoadCsv());

        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> onAdd());

        Button findButton = new Button("Найти по ключу");
        findButton.setOnAction(e -> onFindByKey());

        Button nearestButton = new Button("Ближайшее занятие группы");
        nearestButton.setOnAction(e -> onFindNearest());

        Button removeButton = new Button("Удалить по ключу");
        removeButton.setOnAction(e -> onRemoveByKey());

        Button generateButton = new Button("Сгенерировать");
        generateButton.setOnAction(e -> onGenerate());

        ToolBar toolbar = new ToolBar(loadButton,
                addButton, findButton, nearestButton, removeButton, generateButton);

        VBox root = new VBox(toolbar, treePanel);
        VBox.setVgrow(treePanel, Priority.ALWAYS);
        return root;
    }

    private void onFindByKey() {
        Optional<KeyInputDialog.KeyInput> input =
                KeyInputDialog.show(stage, "Поиск занятия");
        if (input.isEmpty()) return;

        LessonKey key = new LessonKey(input.get().dateTime(), input.get().group());
        Optional<Lesson> found = store.findByKey(key);

        if (found.isEmpty()) {
            Dialogs.info(stage, "Не найдено",
                    "Занятия " + key + " нет в хранилище");
        } else {
            Dialogs.info(stage, "Найдено", found.get().toString());
        }
        refreshView();
    }

    private void onFindNearest() {
        Optional<KeyInputDialog.KeyInput> input =
                KeyInputDialog.show(stage, "Ближайшее занятие группы");
        if (input.isEmpty()) return;

        String group = input.get().group();
        var nearest = store.findNearest(input.get().dateTime(), group);

        status(nearest
                .map(l -> "Ближайшее у " + group + ": " + DisplayFormat.DATE_TIME.format(l.getDateTime())
                        + ", " + l.getSubject())
                .orElse("У группы " + group + " занятий нет"));
        refreshView();
    }

    private void onRemoveByKey() {
        Optional<KeyInputDialog.KeyInput> input =
                KeyInputDialog.show(stage, "Удаление занятия");
        if (input.isEmpty()) return;

        LessonKey key = new LessonKey(input.get().dateTime(), input.get().group());
        boolean removed = store.removeByKey(key);
        status(removed ? "Удалено: " + key : "Ключ " + key + " отсутствовал — ничего не удалено");
        refreshView();
    }


    private VBox buildTableRoot() {
        VBox root = new VBox(buildTableToolbar(), buildTable(), buildStatusBar());
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private ToolBar buildTableToolbar() {
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
                (obs, was, item) -> editButton.setDisable(!(item instanceof Editable)));

        return new ToolBar(loadButton, saveButton, new Separator(), addButton, editButton);
    }

    private TableView<Lesson> buildTable() {
        table.setPlaceholder(new Label("Нет данных — загрузите CSV или добавьте занятия"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Lesson, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().kind().displayName));

        TableColumn<Lesson, String> dateTimeCol = new TableColumn<>("Дата и время");
        dateTimeCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getDateTime()
                        .format(DisplayFormat.DATE_TIME)));

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
                setStyle(empty || item == null
                        ? ""
                        : (item instanceof Cancelled ? STYLE_CANCELLED_ROW : ""));
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

    private void onGenerate() {
        GenerateDialog.show(stage).ifPresent(params -> {
            try {
                List<Lesson> generated = new ScheduleGenerator().generate(
                        params.count(), params.startDay(), params.days());
                int added = 0;
                for (Lesson lesson : generated) {
                    try {
                        store.add(lesson);
                        added++;
                    } catch (IllegalArgumentException e) {
                        // коллизии исключены конструкцией генератора, но защита дешевле инсина
                        // (например, если в store уже есть занятия в тех же слотах)
                    }
                }
                status("Сгенерировано и добавлено: " + added);
            } catch (IllegalArgumentException e) {
                Dialogs.error(stage, "Генерация невозможна", e.getMessage());
            } finally {
                refreshView();
            }
        });
    }

    private void onLoadCsv() {
        File file = csvChooser("Загрузить CSV").showOpenDialog(stage);
        if (file == null) return;

        try {
            CsvLoader.LoadResult result = new CsvLoader().load(file.toPath());

            int duplicates = 0;
            for (Lesson lesson : result.lessons()) {
                try {
                    store.add(lesson);
                } catch (IllegalArgumentException e) {
                    duplicates++;
                }
            }

            currentFile = file.toPath();
            updateTitle();
            String summary = "Загружено занятий: " + (result.lessons().size() - duplicates);
            if (duplicates > 0) summary += ", пропущено дубликатов: " + duplicates;
            status(summary);

            if (result.hasSkipped()) showSkippedReport(result.skipped());
            refreshView();

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
            new CsvSaver().save(path, store.snapshot());
            currentFile = path;
            updateTitle();
            status("Сохранено записей: " + store.size());
        } catch (IOException e) {
            Dialogs.error(stage, "Не удалось сохранить файл", e.getMessage());
        }
    }

    private void onAdd() {
        LessonDialog.showForAdd(stage).ifPresent(lesson -> {
            try {
                store.add(lesson);
                status("Добавлено: " + lesson.getSubject());
            } catch (IllegalArgumentException e) {
                Dialogs.error(stage, "Ключ занят",
                        "У группы " + lesson.getGroup() + " в этом слоте уже есть занятие.");
            } finally {
                refreshView();
            }
        });
    }

    private void onEdit() {
        Lesson selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!(selected instanceof Editable)) {
            Dialogs.info(stage, "Редактирование недоступно",
                    "«" + selected.kind().displayName + "» — read-only запись, изменение запрещено.");
            return;
        }

        LessonDialog.showForEdit(stage, selected).ifPresent(updated -> {
            LessonKey oldKey = LessonKey.of(selected);
            if (store.removeByKey(oldKey)) {
                try {
                    store.add(updated);
                    status("Изменено: " + updated.getSubject());
                } catch (IllegalArgumentException e) {
                    store.add(selected);
                    Dialogs.error(stage, "Ключ занят",
                            "Перемещение невозможно: слот уже занят другим занятием.");
                }
            } else {
                Dialogs.error(stage, "Ошибка", "Запись исчезла из хранилища — обновите данные.");
            }
            refreshView();
        });
    }

    private void refreshView() {
        items.setAll(store.snapshot());
        treePanel.update(store.captureTree(), Set.copyOf(store.lastVisited()));
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