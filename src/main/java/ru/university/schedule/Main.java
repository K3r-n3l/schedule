package ru.university.schedule;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ru.university.schedule.gui.*;
import ru.university.schedule.model.Editable;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.serializer.LoadResult;
import ru.university.schedule.gui.SkippedDialog;
import ru.university.schedule.store.AvlLessonStore;
import ru.university.schedule.store.LessonKey;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Main extends Application
        implements TablePanel.Actions, VisualPanel.Actions {

    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 700;
    private static final String APP_TITLE = "Расписание занятий";

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

    @Override
    public void load() {
        Files.chooseOpen(stage, "Загрузка расписания").ifPresent(choice -> {
            try {
                LoadResult result = choice.format().serializer().loadWithReport(choice.path());

                store.clear();
                int added = 0;
                List<LoadResult.Skipped> skipped = new ArrayList<>(result.skipped());
                for (int i = 0; i < result.lessons().size(); i++) {
                    Lesson lesson = result.lessons().get(i);
                    try {
                        store.add(lesson);
                        added++;
                    } catch (IllegalArgumentException e) {
                        skipped.add(new LoadResult.Skipped(result.numbers().get(i),
                                "ключ занят: " + LessonKey.of(lesson)));
                    }
                }
                skipped.sort(Comparator.comparingInt(LoadResult.Skipped::number));

                currentFile = choice.path();
                updateTitle();
                status("Загружено: " + added + (skipped.isEmpty() ? "" : ", пропущено: " + skipped.size()));

                if (!skipped.isEmpty()) {
                    SkippedDialog.show(stage, skipped);
                }
            } catch (RuntimeException e) {
                Dialogs.error(stage, "Ошибка загрузки", e.getMessage());
                System.out.println(e.getMessage());
            } finally {
                refreshView();
            }
        });
    }

    @Override
    public void save() {
        Files.chooseSave(stage, "Сохранение расписания").ifPresent(choice -> {
            try {
                List<Lesson> snapshot = store.snapshot();
                choice.format().serializer().save(snapshot, choice.path());
                currentFile = choice.path();
                updateTitle();
                status("Сохранено: " + snapshot.size() + " (" + choice.format().name() + ")");
            } catch (RuntimeException e) {
                Dialogs.error(stage, "Ошибка сохранения", e.getMessage());
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

    private void updateTitle() {
        stage.setTitle(currentFile == null
                ? APP_TITLE
                : APP_TITLE + " — " + currentFile.getFileName());
    }

    private void status(String message) {
        tablePanel.setStatus(message);
    }
}