package ru.university.schedule.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Editable;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import java.util.List;
import java.util.function.Function;

public final class TablePanel extends VBox {
    public interface Actions {
        void add();
        void edit(Lesson selected);
        void loadCsv();
        void saveCsv();
    }

    private static final String STYLE_CANCELLED_ROW = "-fx-background-color: #ffd9d9;";
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final double STATUS_SPACING = 12;
    private static final Insets STATUS_PADDING = new Insets(8);

    private final ObservableList<Lesson> items = FXCollections.observableArrayList();
    private final TableView<Lesson> table = new TableView<>(items);
    private final Label statusLabel = new Label("Готово");
    private final Actions actions;

    public TablePanel(Actions actions) {
        this.actions = actions;
        getChildren().addAll(buildToolbar(), buildTable(), buildStatusBar());
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private ToolBar buildToolbar() {
        Button editButton = button("Изменить",
                () -> actions.edit(table.getSelectionModel().getSelectedItem()));
        editButton.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, was, item) -> editButton.setDisable(!(item instanceof Editable)));

        return new ToolBar(
                button("Загрузить CSV", actions::loadCsv),
                button("Сохранить CSV", actions::saveCsv),
                new Separator(),
                button("Добавить", actions::add),
                editButton);
    }

    private TableView<Lesson> buildTable() {
        table.setPlaceholder(new Label("Нет данных — загрузите CSV или добавьте занятия"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(
                column("Тип", lesson -> lesson.kind().displayName),
                column("Дата и время",
                        lesson -> DisplayFormat.DATE_TIME.format(lesson.getDateTime())),
                column("Группа", Lesson::getGroup),
                column("Дисциплина", Lesson::getSubject),
                column("Аудитория", Lesson::getRoom),
                column("Преподаватель", Lesson::getTeacher),
                column("Поток", lesson -> lesson instanceof Lecture lecture
                        ? lecture.getStream() : ""),
                column("Подгруппа", lesson -> lesson instanceof Practice practice
                        ? String.valueOf(practice.getSubgroup()) : ""));
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Lesson item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(empty || item == null
                        ? "" : (item instanceof Cancelled ? STYLE_CANCELLED_ROW : ""));
            }
        });
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == DOUBLE_CLICK_COUNT
                    && table.getSelectionModel().getSelectedItem() != null) {
                actions.edit(table.getSelectionModel().getSelectedItem());
            }
        });
        return table;
    }

    private HBox buildStatusBar() {
        Label countLabel = new Label();
        countLabel.textProperty().bind(
                Bindings.format("Записей: %d", Bindings.size(items)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(STATUS_SPACING, countLabel, spacer, statusLabel);
        bar.setPadding(STATUS_PADDING);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private static TableColumn<Lesson, String> column(String title,
                                                      Function<Lesson, String> extractor) {
        TableColumn<Lesson, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(extractor.apply(cd.getValue())));
        return col;
    }

    private static Button button(String text, Runnable action) {
        Button b = new Button(text);
        b.setOnAction(e -> action.run());
        return b;
    }

    public void replaceAll(List<Lesson> lessons) {
        items.setAll(lessons);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}