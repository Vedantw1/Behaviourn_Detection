package com.behavior.collector.ui;

import com.behavior.collector.model.FeatureVector;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom TableView component for rendering the 12 calculated behavioral features.
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class FeaturePreviewTable extends TableView<FeaturePreviewTable.FeatureRow> {

    public static record FeatureRow(String featureName, String value, String unit) {}

    public FeaturePreviewTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new javafx.scene.control.Label("No session recorded yet. Start recording to preview features."));

        TableColumn<FeatureRow, String> nameCol = new TableColumn<>("Feature Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().featureName()));
        nameCol.setPrefWidth(220);

        TableColumn<FeatureRow, String> valCol = new TableColumn<>("Calculated Value");
        valCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().value()));
        valCol.setPrefWidth(160);

        TableColumn<FeatureRow, String> unitCol = new TableColumn<>("Unit / Metric");
        unitCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        unitCol.setPrefWidth(140);

        getColumns().addAll(nameCol, valCol, unitCol);
    }

    public void updateFeatures(FeatureVector vector) {
        if (vector == null) {
            getItems().clear();
            return;
        }

        List<FeatureRow> rows = new ArrayList<>();
        rows.add(new FeatureRow("Average Mouse Speed", String.format("%.2f", vector.avgMouseSpeed()), "px / sec"));
        rows.add(new FeatureRow("Mouse Acceleration", String.format("%.2f", vector.mouseAcceleration()), "px / sec²"));
        rows.add(new FeatureRow("Click Frequency", String.format("%.2f", vector.clickFrequency()), "clicks / sec"));
        rows.add(new FeatureRow("Scroll Speed", String.format("%.2f", vector.scrollSpeed()), "events / sec"));
        rows.add(new FeatureRow("Average Dwell Time", String.format("%.2f", vector.avgDwellTime()), "ms"));
        rows.add(new FeatureRow("Average Flight Time", String.format("%.2f", vector.avgFlightTime()), "ms"));
        rows.add(new FeatureRow("Typing Speed", String.format("%.2f", vector.typingSpeed()), "keys / sec"));
        rows.add(new FeatureRow("Backspace Count", String.valueOf(vector.backspaceCount()), "count"));
        rows.add(new FeatureRow("Idle Time", String.format("%.2f", vector.idleTime()), "seconds"));
        rows.add(new FeatureRow("Session Duration", String.format("%.2f", vector.sessionDuration()), "seconds"));
        rows.add(new FeatureRow("Window Switch Count", String.valueOf(vector.windowSwitchCount()), "count"));
        rows.add(new FeatureRow("Active Time Ratio", String.format("%.2f", vector.activeTimeRatio()), "ratio (0-1)"));

        ObservableList<FeatureRow> data = FXCollections.observableArrayList(rows);
        setItems(data);
    }
}
