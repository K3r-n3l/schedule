package ru.university.schedule.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.store.AvlStore.NodeInfo;
import ru.university.schedule.store.AvlStore.TreeSnapshot;

import java.util.List;
import java.util.Set;

public class TreeCanvasPanel extends Pane {
    private static final double TOP_MARGIN = 30;
    private static final double H_SPACING = 80; // Фиксированный горизонтальный шаг
    private static final double VERTICAL_GAP = 80; // Фиксированный вертикальный шаг
    private static final double NODE_RADIUS = 18;
    private static final double GROUP_FONT_SIZE = 9;

    private static final Color COLOR_EDGE = Color.GRAY;
    private static final Color COLOR_NODE = Color.LIGHTSTEELBLUE;
    private static final Color COLOR_NODE_VISITED = Color.GOLD;
    private static final Color COLOR_TEXT = Color.BLACK;
    private static final Color COLOR_GROUP_TEXT = Color.DIMGRAY;

    private final Canvas canvas = new Canvas();
    private TreeSnapshot<?, ?> snapshot = null;
    private Set<?> highlighted = Set.of();

    // Параметры трансформации для зума и панорамирования
    private double scale = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private double lastMouseX, lastMouseY;
    private boolean initialLoad = true;

    public TreeCanvasPanel() {
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        widthProperty().addListener((obs, o, n) -> redraw());
        heightProperty().addListener((obs, o, n) -> redraw());

        setFocusTraversable(true);

        // Сброс вида при двойном клике
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                initialLoad = true;
                redraw();
            }
            requestFocus();
        });

        // Панорамирование (перетаскивание)
        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        setOnMouseDragged(e -> {
            translateX += (e.getX() - lastMouseX) / scale;
            translateY += (e.getY() - lastMouseY) / scale;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            redraw();
        });

        // Масштабирование (колесико мыши)
        setOnScroll(e -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double mouseX = e.getX();
            double mouseY = e.getY();

            // Координаты мыши в неотмасштабированном пространстве
            double unscaledX = (mouseX - translateX) / scale;
            double unscaledY = (mouseY - translateY) / scale;

            scale *= zoomFactor;
            // Ограничения зума
            if (scale < 0.02) scale = 0.02;
            if (scale > 5.0) scale = 5.0;

            // Корректировка смещения, чтобы точка под курсором осталась на месте
            translateX = mouseX - unscaledX * scale;
            translateY = mouseY - unscaledY * scale;

            redraw();
        });
    }

    public void update(TreeSnapshot<?, ?> snapshot, Set<?> visitedKeys) {
        this.snapshot = snapshot;
        this.highlighted = visitedKeys;
        this.initialLoad = true; // Сбрасываем вид при обновлении данных
        redraw();
    }

    private void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<? extends NodeInfo<?, ?>> nodes = snapshot == null ? List.of() : snapshot.nodes();
        if (nodes.isEmpty()) return;

        int n = nodes.size();
        int maxDepth = nodes.stream().mapToInt(NodeInfo::depth).max().orElse(0);

        // Вычисляем полные размеры дерева с учетом фиксированных отступов
        double treeWidth = (n - 1) * H_SPACING + 2 * NODE_RADIUS + 40;
        double treeHeight = maxDepth * VERTICAL_GAP + 2 * NODE_RADIUS + TOP_MARGIN;

        if (initialLoad) {
            // Автоматическая подгонка под экран при первой загрузке
            double availableWidth = canvas.getWidth() - 40;
            double availableHeight = canvas.getHeight() - 40;

            double scaleX = availableWidth / treeWidth;
            double scaleY = availableHeight / treeHeight;

            // Не увеличиваем сильнее 1.0, только уменьшаем если не влезает
            scale = Math.min(1.0, Math.min(scaleX, scaleY));

            // Центрируем дерево
            translateX = (canvas.getWidth() - treeWidth * scale) / 2;
            translateY = (canvas.getHeight() - treeHeight * scale) / 2;

            initialLoad = false;
        }

        g.save();
        g.translate(translateX, translateY);
        g.scale(scale, scale);

        // Делаем линии всегда толщиной 1 пиксель на экране, независимо от зума
        g.setLineWidth(1.0 / scale);

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            NodeInfo<?, ?> node = nodes.get(i);
            xs[i] = NODE_RADIUS + 20 + H_SPACING * i;
            ys[i] = TOP_MARGIN + VERTICAL_GAP * node.depth();
        }

        // Рисуем связи
        g.setStroke(COLOR_EDGE);
        for (int[] edge : snapshot.edges()) {
            g.strokeLine(xs[edge[0]], ys[edge[0]], xs[edge[1]], ys[edge[1]]);
        }

        // Рисуем узлы
        for (int i = 0; i < n; i++) {
            NodeInfo<?, ?> node = nodes.get(i);
            boolean visited = highlighted.contains(node.key());

            g.setFill(visited ? COLOR_NODE_VISITED : COLOR_NODE);
            g.fillOval(xs[i] - NODE_RADIUS, ys[i] - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);
            g.setStroke(COLOR_EDGE);
            g.strokeOval(xs[i] - NODE_RADIUS, ys[i] - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);

            // Level of Detail (LOD): Скрываем текст, если сильно отдалились
            if (scale > 0.5) {
                g.setFill(COLOR_TEXT);
                g.setTextAlign(TextAlignment.CENTER);
                g.fillText(timeLabel(node.value()), xs[i], ys[i] + 4);
            }

            if (scale > 0.3) {
                g.setFill(COLOR_GROUP_TEXT);
                g.setFont(Font.font(GROUP_FONT_SIZE));
                g.fillText(groupLabel(node.value()), xs[i], ys[i] + NODE_RADIUS + GROUP_FONT_SIZE);
                g.setFont(Font.getDefault());
            }
        }

        g.restore();
    }

    private String timeLabel(Object value) {
        return value instanceof Lesson lesson
                ? DisplayFormat.TIME_ONLY.format(lesson.getDateTime())
                : String.valueOf(value);
    }

    private String groupLabel(Object value) {
        return value instanceof Lesson lesson ? lesson.getGroup() : "";
    }
}