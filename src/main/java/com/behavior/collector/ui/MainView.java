package com.behavior.collector.ui;

import com.behavior.collector.listeners.EventCollector;
import com.behavior.collector.listeners.KeyboardEventListener;
import com.behavior.collector.listeners.MouseEventListener;
import com.behavior.collector.listeners.WindowEventListener;
import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.model.SessionLabel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main JavaFX View composing the user interface controls, timer, status indicator,
 * and feature preview table.
 */
public class MainView {
    private final Stage stage;
    private final EventCollector collector;
    private final SessionController controller;

    private TextField userIdField;
    private ComboBox<SessionLabel> labelComboBox;
    private Spinner<Integer> durationSpinner;
    private Button startButton;
    private Button stopButton;
    private Label timerLabel;
    private Label statusLabel;
    private Label csvPathLabel;
    private TextArea typingArea;
    private FeaturePreviewTable featureTable;

    private KeyboardEventListener keyListener;

    public MainView(Stage stage) {
        this.stage = stage;
        this.collector = new EventCollector();
        this.controller = new SessionController(collector);
    }

    public Parent buildUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");

        // Top Section: Header & Control Bar
        VBox topBox = new VBox(15);
        topBox.getChildren().addAll(createHeader(), createFormPanel());
        root.setTop(topBox);

        // Center Section: Timer, Controls, Typing Practice Panel & Preview Table
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(15, 0, 15, 0));
        centerBox.getChildren().addAll(createTimerAndControlBar(), createTypingPanel(), createTablePanel());
        root.setCenter(centerBox);

        // Bottom Section: Status Bar & CSV Path Info
        root.setBottom(createStatusBar());

        setupListeners();
        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox(4);
        Label title = new Label("Behavioral Data Collector");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Collect raw biometric events (Mouse & Keyboard) and calculate 12 engineered dataset features");
        subtitle.getStyleClass().add("header-subtitle");
        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private GridPane createFormPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.getStyleClass().add("glass-panel");

        // User ID
        Label userLabel = new Label("User ID:");
        userLabel.getStyleClass().add("field-label");
        userIdField = new TextField("user_01");
        userIdField.setPromptText("e.g. user_101");
        userIdField.setPrefWidth(180);

        // Session Label
        Label labelTag = new Label("Session Label:");
        labelTag.getStyleClass().add("field-label");
        labelComboBox = new ComboBox<>();
        labelComboBox.getItems().addAll(SessionLabel.GENUINE, SessionLabel.FRAUD);
        labelComboBox.setValue(SessionLabel.GENUINE);
        labelComboBox.setPrefWidth(150);

        // Session Duration
        Label durationLabel = new Label("Duration (sec):");
        durationLabel.getStyleClass().add("field-label");
        durationSpinner = new Spinner<>(5, 300, 30);
        durationSpinner.setEditable(true);
        durationSpinner.setPrefWidth(100);

        grid.add(userLabel, 0, 0);
        grid.add(userIdField, 1, 0);
        grid.add(labelTag, 2, 0);
        grid.add(labelComboBox, 3, 0);
        grid.add(durationLabel, 4, 0);
        grid.add(durationSpinner, 5, 0);

        return grid;
    }

    private HBox createTimerAndControlBar() {
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("glass-panel");

        // Timer Display Box
        VBox timerBox = new VBox(2);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.getStyleClass().add("timer-box");
        timerLabel = new Label("30s");
        timerLabel.getStyleClass().add("timer-display");
        timerBox.getChildren().addAll(new Label("REMAINING"), timerLabel);

        // Action Buttons
        startButton = new Button("▶  Start Recording");
        startButton.getStyleClass().add("btn-start");
        startButton.setOnAction(e -> handleStartRecording());

        stopButton = new Button("⏹  Stop Recording");
        stopButton.getStyleClass().add("btn-stop");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> handleStopRecording());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(startButton, stopButton, spacer, timerBox);
        return bar;
    }

    private VBox createTypingPanel() {
        VBox box = new VBox(6);
        Label label = new Label("Typing Practice Area (Type here during recording):");
        label.getStyleClass().add("field-label");

        typingArea = new TextArea();
        typingArea.setPromptText("Type any sample text here during the recording session to capture keystrokes, backspaces, dwell times, and flight times...");
        typingArea.setPrefRowCount(3);
        typingArea.setWrapText(true);

        box.getChildren().addAll(label, typingArea);
        return box;
    }

    private VBox createTablePanel() {
        VBox box = new VBox(8);
        VBox.setVgrow(box, Priority.ALWAYS);

        Label tableTitle = new Label("Feature Preview (12 Engineered Features):");
        tableTitle.getStyleClass().add("field-label");

        featureTable = new FeaturePreviewTable();
        VBox.setVgrow(featureTable, Priority.ALWAYS);

        box.getChildren().addAll(tableTitle, featureTable);
        return box;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("Status: Ready to record session.");
        statusLabel.getStyleClass().add("status-idle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        csvPathLabel = new Label("Output: " + controller.getOutputCsvFile().getName());
        csvPathLabel.getStyleClass().add("header-subtitle");

        Button chooseCsvBtn = new Button("Choose CSV...");
        chooseCsvBtn.setOnAction(e -> handleSelectCsvFile());

        statusBar.getChildren().addAll(statusLabel, spacer, csvPathLabel, chooseCsvBtn);
        return statusBar;
    }

    private void handleStartRecording() {
        String userId = userIdField.getText();
        if (userId == null || userId.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a valid User ID before starting.");
            return;
        }

        try {
            int duration = durationSpinner.getValue();
            controller.startRecording(userId, labelComboBox.getValue(), duration);

            if (keyListener != null) {
                keyListener.reset();
            }

            if (typingArea != null) {
                typingArea.clear();
                typingArea.setDisable(false);
                typingArea.requestFocus();
            }

            startButton.setDisable(true);
            stopButton.setDisable(false);
            userIdField.setDisable(true);
            labelComboBox.setDisable(true);
            durationSpinner.setDisable(true);

            statusLabel.setText("Status: Recording behavioral biometric events...");
            statusLabel.getStyleClass().removeAll("status-idle", "status-success");
            statusLabel.getStyleClass().add("status-recording");

            featureTable.getItems().clear();
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void handleStopRecording() {
        controller.stopRecording();
    }

    private void handleSelectCsvFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV Output File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("behavioral_data.csv");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            controller.setOutputCsvFile(file);
            csvPathLabel.setText("Output: " + file.getName());
        }
    }

    private void setupListeners() {
        // Timer updates
        collector.setOnTimerTick(remainingSeconds -> {
            timerLabel.setText(String.format("%02ds", remainingSeconds));
        });

        // Recording completed callback
        collector.setOnRecordingCompleted(session -> {
            startButton.setDisable(false);
            stopButton.setDisable(true);
            userIdField.setDisable(false);
            labelComboBox.setDisable(false);
            durationSpinner.setDisable(false);
            timerLabel.setText(String.format("%02ds", durationSpinner.getValue()));

            try {
                FeatureVector vector = controller.processCompletedSession(session);
                featureTable.updateFeatures(vector);

                statusLabel.setText("Status: Session completed! Features calculated and exported to CSV.");
                statusLabel.getStyleClass().removeAll("status-recording", "status-idle");
                statusLabel.getStyleClass().add("status-success");
            } catch (Exception e) {
                statusLabel.setText("Status: Error saving CSV - " + e.getMessage());
                showAlert("CSV Export Error", e.getMessage());
            }
        });
    }

    public void attachEventHandlers(Scene scene) {
        MouseEventListener mouseListener = new MouseEventListener(collector);
        this.keyListener = new KeyboardEventListener(collector);
        WindowEventListener windowListener = new WindowEventListener(collector);

        // Attach mouse handlers to root scene
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, mouseListener.getMouseMovedHandler());
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, mouseListener.getMouseClickedHandler());
        scene.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, mouseListener.getScrollHandler());

        // Attach keyboard handlers to root scene
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, keyListener.getKeyPressedHandler());
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, keyListener.getKeyReleasedHandler());

        // Attach keyboard handlers directly to typingArea as well
        if (typingArea != null) {
            typingArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, keyListener.getKeyPressedHandler());
            typingArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, keyListener.getKeyReleasedHandler());
        }

        // Attach window focus listener
        stage.focusedProperty().addListener(windowListener.getFocusChangeListener());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
