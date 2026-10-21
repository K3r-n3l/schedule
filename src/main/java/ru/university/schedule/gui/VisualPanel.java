package ru.university.schedule.gui;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.university.schedule.store.AvlStore.TreeSnapshot;

import java.util.Set;

public final class VisualPanel extends VBox {
    public interface Actions {
        void loadCsv();
        void add();
        void findByKey();
        void findNearest();
        void removeByKey();
        void generate();
    }

    private final TreeCanvasPanel canvas = new TreeCanvasPanel();

    public VisualPanel(Actions actions) {
        ToolBar toolbar = new ToolBar(
                button("Загрузить CSV", actions::loadCsv),
                button("Добавить", actions::add),
                button("Найти по ключу", actions::findByKey),
                button("Ближайшее занятие группы", actions::findNearest),
                button("Удалить по ключу", actions::removeByKey),
                button("Сгенерировать", actions::generate));

        VBox.setVgrow(canvas, Priority.ALWAYS);
        getChildren().addAll(toolbar, canvas);
    }

    public void update(TreeSnapshot<?, ?> snapshot, Set<?> visitedKeys) {
        canvas.update(snapshot, visitedKeys);
    }

    private static Button button(String text, Runnable action) {
        Button b = new Button(text);
        b.setOnAction(e -> action.run());
        return b;
    }
}