package ru.university.schedule.serializer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import ru.university.schedule.serializer.LoadResult.Skipped;

import java.util.List;

public final class SkippedDialog {
    private SkippedDialog() { }

    public static void show(Window owner, List<Skipped> skipped) {
        TableView<Skipped> table = new TableView<>(FXCollections.observableArrayList(skipped));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Skipped, Integer> numberCol = new TableColumn<>("№ объекта");
        numberCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().number()));
        numberCol.setMaxWidth(100);

        TableColumn<Skipped, String> reasonCol = new TableColumn<>("Причина");
        reasonCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().reason()));

        table.getColumns().addAll(numberCol, reasonCol);
        table.setPrefSize(560, 320);

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Пропущенные объекты");
        dialog.setHeaderText("Пропущено объектов: " + skipped.size()
                + " (номер — порядок объекта в файле)");
        dialog.getDialogPane().setContent(new VBox(8, new Label("Эти записи не попали в расписание:"), table));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.setResizable(true);
        dialog.showAndWait();
    }
}
