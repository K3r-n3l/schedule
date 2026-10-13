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
    private static final double MAX_H_SPACING = 110;
    private static final double VERTICAL_GAP = 80;
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

    public TreeCanvasPanel() {
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        widthProperty().addListener((obs, o, n) -> redraw());
        heightProperty().addListener((obs, o, n) -> redraw());
    }

    public void update(TreeSnapshot<?, ?> snapshot, Set<?> visitedKeys) {
        this.snapshot = snapshot;
        this.highlighted = visitedKeys;
        redraw();
    }

    private void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<? extends NodeInfo<?, ?>> nodes = snapshot == null ? List.of() : snapshot.nodes();
        if (nodes.isEmpty()) return;

        int n = nodes.size();
        int maxDepth = nodes.stream().mapToInt(NodeInfo::depth).max().orElse(0);

        // --- Шаг по x: не шире MAX_H_SPACING; центрируем, пока помещается ---
        double availableWidth = canvas.getWidth() - 2 * NODE_RADIUS;
        double xStep = n == 1 ? 0 : Math.min(availableWidth / (n - 1), MAX_H_SPACING);
        double startX = (canvas.getWidth() - xStep * (n - 1)) / 2;

        // --- Шаг по y: базовый VERTICAL_GAP; сжимаем, только если глубокое дерево не влезает ---
        double availableHeight = canvas.getHeight() - TOP_MARGIN - NODE_RADIUS - GROUP_FONT_SIZE * 2;
        double yStep = maxDepth == 0 ? 0 : Math.min(VERTICAL_GAP, availableHeight / maxDepth);

        // --- Координаты ---
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            NodeInfo<?, ?> node = nodes.get(i);
            xs[i] = startX + xStep * i;
            ys[i] = TOP_MARGIN + yStep * node.depth();
        }

        // --- Рёбра ---
        g.setStroke(COLOR_EDGE);
        for (int[] edge : snapshot.edges()) {
            g.strokeLine(xs[edge[0]], ys[edge[0]], xs[edge[1]], ys[edge[1]]);
        }

        // --- Узлы ---
        for (int i = 0; i < n; i++) {
            NodeInfo<?, ?> node = nodes.get(i);
            boolean visited = highlighted.contains(node.key());

            g.setFill(visited ? COLOR_NODE_VISITED : COLOR_NODE);
            g.fillOval(xs[i] - NODE_RADIUS, ys[i] - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);
            g.setStroke(COLOR_EDGE);
            g.strokeOval(xs[i] - NODE_RADIUS, ys[i] - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);

            // Время — в центре круга
            g.setFill(COLOR_TEXT);
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(timeLabel(node.value()), xs[i], ys[i] + 4);

            // Группа — мелко под кругом
            g.setFill(COLOR_GROUP_TEXT);
            g.setFont(Font.font(GROUP_FONT_SIZE));
            g.fillText(groupLabel(node.value()), xs[i], ys[i] + NODE_RADIUS + GROUP_FONT_SIZE);
            g.setFont(Font.getDefault());   // вернуть шрифт для следующих узлов
        }
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