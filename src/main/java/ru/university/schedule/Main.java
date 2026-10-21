package ru.university.schedule;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ru.university.schedule.csv.CsvLoader;
import ru.university.schedule.csv.CsvSaver;
import ru.university.schedule.gui.*;
import ru.university.schedule.model.Editable;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.store.AvlLessonStore;
import ru.university.schedule.store.LessonKey;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Main extends Application
        implements TablePanel.Actions, VisualPanel.Actions {

    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 700;
    private static final String APP_TITLE = "Расписание занятий";
    private static final int MAX_SKIPPED_IN_DIALOG = 20;

    private final AvlLessonStore store = new AvlLessonStore();

    private Stage stage;
    private TablePanel tablePanel;
    private VisualPanel visualPanel;
    private Path currentFile;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        tablePanel = new TablePanel(this);
        visualPanel = new VisualPanel(this);

        Tab visualTab = new Tab("Визуализация", visualPanel);
        visualTab.setClosable(false);
        Tab tableTab = new Tab("Таблица расписания", tablePanel);
        tableTab.setClosable(false);

        Scene scene = new Scene(new TabPane(visualTab, tableTab), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.show();

        refreshView();
    }

    private void refreshView() {
        tablePanel.replaceAll(store.snapshot());
        visualPanel.update(store.captureTree(), Set.copyOf(store.lastVisited()));
    }

    @Override
    public void add() {
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

    @Override
    public void edit(Lesson selected) {
        if (selected == null) {
            return;
        }
        if (!(selected instanceof Editable)) {
            Dialogs.info(stage, "Редактирование недоступно",
                    "«" + selected.kind().displayName + "» — read-only запись, изменение запрещено.");
            return;
        }
        LessonDialog.showForEdit(stage, selected).ifPresent(updated -> {
            if (store.removeByKey(LessonKey.of(selected))) {
                try {
                    store.add(updated);
                    status("Изменено: " + updated.getSubject());
                } catch (IllegalArgumentException e) {
                    store.add(selected);   // откат: возвращаем запись как была
                    Dialogs.error(stage, "Ключ занят",
                            "Перемещение невозможно: слот уже занят другим занятием.");
                }
            } else {
                Dialogs.error(stage, "Ошибка", "Запись исчезла из хранилища — обновите данные.");
            }
            refreshView();
        });
    }

    @Override
    public void loadCsv() {
        CsvFiles.chooseOpen(stage, "Загрузить CSV").ifPresent(this::importCsv);
    }

    @Override
    public void saveCsv() {
        CsvFiles.chooseSave(stage, "Сохранить CSV").ifPresent(chosen -> {
            Path path = CsvFiles.ensureExtension(chosen);
            try {
                new CsvSaver().save(path, store.snapshot());
                currentFile = path;
                updateTitle();
                status("Сохранено записей: " + store.size());
            } catch (IOException e) {
                Dialogs.error(stage, "Не удалось сохранить файл", e.getMessage());
            }
        });
    }


    @Override
    public void findByKey() {
        FindDialog.show(stage, "Поиск занятия").ifPresent(input -> {
            LessonKey key = new LessonKey(input.dateTime(), input.group());
            store.findByKey(key).ifPresentOrElse(
                    lesson -> Dialogs.info(stage, "Найдено", lesson.toString()),
                    () -> Dialogs.info(stage, "Не найдено",
                            "Занятия " + key + " нет в хранилище"));
            refreshView();
        });
    }

    @Override
    public void findNearest() {
        FindDialog.show(stage, "Ближайшее занятие группы").ifPresent(input -> {
            String group = input.group();
            store.findNearest(input.dateTime(), group).ifPresentOrElse(
                    lesson -> Dialogs.info(stage, "Ближайшее у группы " + group,
                            DisplayFormat.DATE_TIME.format(lesson.getDateTime())
                                    + " — " + lesson.getSubject()),
                    () -> Dialogs.info(stage, "Ничего не найдено",
                            "У группы " + group + " занятий нет"));
            refreshView();
        });
    }

    @Override
    public void removeByKey() {
        FindDialog.show(stage, "Удаление занятия").ifPresent(input -> {
            LessonKey key = new LessonKey(input.dateTime(), input.group());
            status(store.removeByKey(key)
                    ? "Удалено: " + key
                    : "Ключ " + key + " отсутствовал — ничего не удалено");
            refreshView();
        });
    }

    @Override
    public void generate() {
        GenerateDialog.show(stage).ifPresent(params -> {
            try {
                List<Lesson> generated = new Generator()
                        .generate(params.count(), params.startDay(), params.days());

                store.clear();

                int added = 0;
                for (Lesson lesson : generated) {
                        store.add(lesson);
                        added++;
                }
                currentFile = null;

                status("Сгенерировано и добавлено: " + added);
            } catch (IllegalArgumentException e) {
                Dialogs.error(stage, "Генерация невозможна", e.getMessage());
            } finally {
                refreshView();
            }
        });
    }


    private void importCsv(Path path) {
        try {
            CsvLoader.LoadResult result = new CsvLoader().load(path);
            store.clear();

            int duplicates = 0;
            for (Lesson lesson : result.lessons()) {
                try {
                    store.add(lesson);
                } catch (IllegalArgumentException e) {
                    duplicates++;
                }
            }

            currentFile = path;
            updateTitle();
            String summary = "Загружено занятий: " + (result.lessons().size() - duplicates);
            if (duplicates > 0) {
                summary += ", пропущено дубликатов: " + duplicates;
            }
            status(summary);

            if (result.hasSkipped()) {
                showSkippedReport(result.skipped());
            }
            refreshView();
        } catch (IOException e) {
            Dialogs.error(stage, "Не удалось прочитать файл", e.getMessage());
        }
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
        tablePanel.setStatus(message);
    }
}