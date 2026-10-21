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
    private static final double H_SPACING = 80;
    private static final double VERTICAL_GAP = 80;
    private static final double NODE_RADIUS = 18;
    private static final double GROUP_FONT_SIZE = 10;
    private static final double DATE_FONT_SIZE = 8;

    private static final Color COLOR_EDGE = Color.GRAY;
    private static final Color COLOR_NODE = Color.LIGHTSTEELBLUE;
    private static final Color COLOR_NODE_VISITED = Color.GOLD;
    private static final Color COLOR_TEXT = Color.BLACK;
    private static final Color COLOR_DATE_TEXT = Color.DIMGRAY;

    private final Canvas canvas = new Canvas();
    private TreeSnapshot<?, ?> snapshot = null;
    private Set<?> highlighted = Set.of();

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

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                initialLoad = true;
                redraw();
            }
            requestFocus();
        });

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

        setOnScroll(e -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double mouseX = e.getX();
            double mouseY = e.getY();

            double unscaledX = (mouseX - translateX) / scale;
            double unscaledY = (mouseY - translateY) / scale;

            scale *= zoomFactor;
            if (scale < 0.02) scale = 0.02;
            if (scale > 5.0) scale = 5.0;

            translateX = mouseX - unscaledX * scale;
            translateY = mouseY - unscaledY * scale;

            redraw();
        });
    }

    public void update(TreeSnapshot<?, ?> snapshot, Set<?> visitedKeys) {
        this.snapshot = snapshot;
        this.highlighted = visitedKeys;
        this.initialLoad = true;
        redraw();
    }

    private void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<? extends NodeInfo<?, ?>> nodes = snapshot == null ? List.of() : snapshot.nodes();
        if (nodes.isEmpty()) return;

        int n = nodes.size();
        int maxDepth = nodes.stream().mapToInt(NodeInfo::depth).max().orElse(0);

        double treeWidth = (n - 1) * H_SPACING + 2 * NODE_RADIUS + 40;
        double treeHeight = maxDepth * VERTICAL_GAP + 2 * NODE_RADIUS + TOP_MARGIN;

        if (initialLoad) {
            double availableWidth = canvas.getWidth() - 40;
            double availableHeight = canvas.getHeight() - 40;

            double scaleX = availableWidth / treeWidth;
            double scaleY = availableHeight / treeHeight;

            scale = Math.min(1.0, Math.min(scaleX, scaleY));

            translateX = (canvas.getWidth() - treeWidth * scale) / 2;
            translateY = (canvas.getHeight() - treeHeight * scale) / 2;

            initialLoad = false;
        }

        g.save();
        g.translate(translateX, translateY);
        g.scale(scale, scale);
        g.setLineWidth(1.0 / scale);

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            NodeInfo<?, ?> node = nodes.get(i);
            xs[i] = NODE_RADIUS + 20 + H_SPACING * i;
            ys[i] = TOP_MARGIN + VERTICAL_GAP * node.depth();
        }

        g.setStroke(COLOR_EDGE);
        for (int[] edge : snapshot.edges()) {
            g.strokeLine(xs[edge[0]], ys[edge[0]], xs[edge[1]], ys[edge[1]]);
        }

        g.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < n; i++) {
            NodeInfo<?, ?> node = nodes.get(i);
            boolean visited = highlighted.contains(node.key());

            g.setFill(visited ? COLOR_NODE_VISITED : COLOR_NODE);
            g.fillOval(xs[i] - NODE_RADIUS, ys[i] - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);
            g.setStroke(COLOR_EDGE);
            g.strokeOval(xs[i] - NODE_RADIUS, ys[i] - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);

            if (scale > 0.4) {
                g.setFill(COLOR_TEXT);
                g.setFont(Font.font(GROUP_FONT_SIZE));
                g.fillText(groupLabel(node.value()), xs[i], ys[i] + 4);
            }

            if (scale > 0.5) {
                g.setFill(COLOR_DATE_TEXT);
                g.setFont(Font.font(DATE_FONT_SIZE));
                g.fillText(dateLabel(node.value()),
                        xs[i], ys[i] + NODE_RADIUS + DATE_FONT_SIZE + 2);
            }
        }

        g.restore();
    }

    private String groupLabel(Object value) {
        return value instanceof Lesson lesson ? lesson.getGroup() : "";
    }

    private String dateLabel(Object value) {
        return value instanceof Lesson lesson
                ? DisplayFormat.DATE_TIME.format(lesson.getDateTime())
                : String.valueOf(value);
    }
}