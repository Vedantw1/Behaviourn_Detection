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
    private CheckBox continuousCheckBox;

    // Real-Time ML Inference & Biometric Trust Shield Controls
    private Label trustScoreLabel;
    private ProgressBar trustProgressBar;
    private Label predictionBadge;
    private Label predictionDetailsLabel;
    private CheckBox lockOnFraudCheckBox;

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

        // Center Section: Timer, Trust Meter, Typing Practice Panel & Preview Table
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(15, 0, 15, 0));
        centerBox.getChildren().addAll(
                createTimerAndControlBar(),
                createBiometricTrustPanel(),
                createTypingPanel(),
                createTablePanel()
        );
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

        // Continuous Recording Mode Checkbox
        continuousCheckBox = new CheckBox("🔁 Continuous Recording Mode (Auto-repeat 30s sessions)");
        continuousCheckBox.setSelected(true);
        continuousCheckBox.getStyleClass().add("field-label");

        Label systemModeLabel = new Label(
                collector.isGlobalHookActive()
                        ? "🌐 System-Wide Capture: Active (Tracking mouse & keys across all macOS apps)"
                        : "⚠️ System-Wide Capture: Inactive (Grant Accessibility in macOS System Settings)"
        );
        systemModeLabel.setStyle(collector.isGlobalHookActive()
                ? "-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;"
                : "-fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox optionsBox = new HBox(20, continuousCheckBox, systemModeLabel);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(userLabel, 0, 0);
        grid.add(userIdField, 1, 0);
        grid.add(labelTag, 2, 0);
        grid.add(labelComboBox, 3, 0);
        grid.add(durationLabel, 4, 0);
        grid.add(durationSpinner, 5, 0);
        grid.add(optionsBox, 0, 1, 6, 1);

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

    private VBox createBiometricTrustPanel() {
        VBox box = new VBox(10);
        box.getStyleClass().add("trust-panel");

        // Header Row: Title and Live Prediction Status Badge
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("🛡️ Real-Time Biometric Fraud Shield (Active ML Continuous Authentication)");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        predictionBadge = new Label("● WAITING FOR INITIAL SESSION");
        predictionBadge.getStyleClass().add("trust-badge-genuine");
        predictionBadge.setStyle("-fx-text-fill: #94a3b8; -fx-border-color: #64748b; -fx-background-color: rgba(100, 116, 139, 0.15);");

        headerRow.getChildren().addAll(titleLabel, spacer, predictionBadge);

        // Score Row: Progress Bar, Percentage Label, and Threat Lockout Toggle
        HBox scoreRow = new HBox(15);
        scoreRow.setAlignment(Pos.CENTER_LEFT);

        trustProgressBar = new ProgressBar(0.0);
        trustProgressBar.getStyleClass().add("trust-bar");
        trustProgressBar.setPrefWidth(360);
        trustProgressBar.setPrefHeight(18);

        trustScoreLabel = new Label("Biometric Trust Score: --%");
        trustScoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        lockOnFraudCheckBox = new CheckBox("🚨 Threat Lockout Alert (Prompt on Imposter Detection)");
        lockOnFraudCheckBox.setSelected(true);
        lockOnFraudCheckBox.getStyleClass().add("field-label");
        lockOnFraudCheckBox.setStyle("-fx-font-size: 12px;");

        scoreRow.getChildren().addAll(trustProgressBar, trustScoreLabel, spacer2, lockOnFraudCheckBox);

        predictionDetailsLabel = new Label("Confidence: -- | Inference Model: Random Forest & XGBoost Ensemble");
        predictionDetailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        box.getChildren().addAll(headerRow, scoreRow, predictionDetailsLabel);
        return box;
    }

    private void updateTrustPanel(com.behavior.collector.model.BiometricPrediction prediction) {
        if (prediction == null) return;

        double trust = prediction.trustScore();
        trustProgressBar.setProgress(trust / 100.0);
        trustScoreLabel.setText(String.format("Biometric Trust Score: %.1f%%", trust));

        predictionBadge.getStyleClass().removeAll("trust-badge-genuine", "trust-badge-fraud");

        if (prediction.isGenuine()) {
            predictionBadge.setText("🛡️ AUTHENTICATED: GENUINE USER");
            predictionBadge.getStyleClass().add("trust-badge-genuine");
            predictionBadge.setStyle("");
            trustProgressBar.getStyleClass().removeAll("trust-bar-danger");
            trustProgressBar.getStyleClass().add("trust-bar");
            predictionDetailsLabel.setText(String.format(
                    "✅ Verified genuine human biometrics. Model Confidence: %.1f%% (Anomaly Risk: %.1f%%)",
                    prediction.confidence(), prediction.fraudScore()
            ));
        } else {
            predictionBadge.setText("🚨 THREAT DETECTED: IMPOSTER / BOT ATTACK");
            predictionBadge.getStyleClass().add("trust-badge-fraud");
            predictionBadge.setStyle("");
            trustProgressBar.getStyleClass().removeAll("trust-bar");
            trustProgressBar.getStyleClass().add("trust-bar-danger");
            predictionDetailsLabel.setText(String.format(
                    "⚠️ Biometric anomaly detected! Dynamics deviate from baseline. Model Confidence: %.1f%% (Fraud Score: %.1f%%)",
                    prediction.confidence(), prediction.fraudScore()
            ));

            if (lockOnFraudCheckBox != null && lockOnFraudCheckBox.isSelected()) {
                showAlert("🚨 Biometric Security Alert",
                        "Security Threat Detected!\n\n" +
                        "The recent typing cadence, dwell timings, and mouse acceleration do not match " +
                        "the authentic user baseline (Fraud Score: " + String.format("%.1f%%", prediction.fraudScore()) + ").\n\n" +
                        "Action: Stepped-up Multi-Factor Authentication required.");
            }
        }
    }

    private VBox createTypingPanel() {
        VBox box = new VBox(6);
        Label label = new Label("Typing Practice Area (Optional - System-wide capture is active):");
        label.getStyleClass().add("field-label");

        typingArea = new TextArea();
        typingArea.setPromptText("System-wide capture is active! You can type here OR switch to ANY other app (Chrome, VS Code, Notes, Slack, etc.) — all your keystrokes, flight times, and mouse movements are captured automatically across macOS...");
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
            if (continuousCheckBox != null) {
                continuousCheckBox.setDisable(true);
            }

            statusLabel.setText("Status: Recording behavioral biometric events...");
            statusLabel.getStyleClass().removeAll("status-idle", "status-success");
            statusLabel.getStyleClass().add("status-recording");

            featureTable.getItems().clear();
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void handleStopRecording() {
        if (continuousCheckBox != null) {
            continuousCheckBox.setSelected(false);
            continuousCheckBox.setDisable(false);
        }
        controller.stopRecording();
    }

    public void autoStartRecording() {
        if (!collector.isRecording()) {
            handleStartRecording();
        }
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
            boolean isContinuous = continuousCheckBox != null && continuousCheckBox.isSelected();

            if (!isContinuous) {
                startButton.setDisable(false);
                stopButton.setDisable(true);
                userIdField.setDisable(false);
                labelComboBox.setDisable(false);
                durationSpinner.setDisable(false);
                if (continuousCheckBox != null) {
                    continuousCheckBox.setDisable(false);
                }
            }
            timerLabel.setText(String.format("%02ds", durationSpinner.getValue()));

            try {
                FeatureVector vector = controller.processCompletedSession(session);
                featureTable.updateFeatures(vector);

                // Run real-time machine learning inference
                com.behavior.collector.model.BiometricPrediction prediction = controller.predictSession(vector);
                if (prediction != null) {
                    updateTrustPanel(prediction);
                }

                if (isContinuous) {
                    String authStatus = (prediction != null && prediction.isGenuine()) ? "Authenticated (Genuine)" : "Anomaly Flagged";
                    statusLabel.setText(String.format("Status: Saved & Evaluated (%s, Trust: %.1f%%). Repeating...",
                            authStatus, (prediction != null ? prediction.trustScore() : 100.0)));
                    statusLabel.getStyleClass().removeAll("status-recording", "status-idle");
                    statusLabel.getStyleClass().add("status-success");

                    // Trigger next session automatically
                    javafx.application.Platform.runLater(this::handleStartRecording);
                } else {
                    statusLabel.setText("Status: Session completed! Features calculated, exported, and verified by ML model.");
                    statusLabel.getStyleClass().removeAll("status-recording", "status-idle");
                    statusLabel.getStyleClass().add("status-success");
                }
            } catch (Exception e) {
                statusLabel.setText("Status: Error saving dataset row - " + e.getMessage());
                showAlert("CSV Export Error", e.getMessage());
            }
        });
    }

    public void attachEventHandlers(Scene scene) {
        // If system-wide JNativeHook is active, it captures all keyboard & mouse events globally across the OS.
        // We only attach local scene handlers as a fallback if system-wide hooks are inactive (e.g. permission denied)
        if (!collector.isGlobalHookActive()) {
            MouseEventListener mouseListener = new MouseEventListener(collector);
            this.keyListener = new KeyboardEventListener(collector);

            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, mouseListener.getMouseMovedHandler());
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, mouseListener.getMouseClickedHandler());
            scene.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, mouseListener.getScrollHandler());

            scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, keyListener.getKeyPressedHandler());
            scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, keyListener.getKeyReleasedHandler());

            if (typingArea != null) {
                typingArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, keyListener.getKeyPressedHandler());
                typingArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, keyListener.getKeyReleasedHandler());
            }
        }

        WindowEventListener windowListener = new WindowEventListener(collector);
        // Attach window focus listener
        stage.focusedProperty().addListener(windowListener.getFocusChangeListener());

        // Gracefully clean up native hooks when window is closed
        stage.setOnCloseRequest(e -> collector.shutdownGlobalHooks());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
