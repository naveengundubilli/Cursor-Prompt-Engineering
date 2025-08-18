package com.securepdfeditor.ui;

import com.securepdfeditor.packaging.PackagingService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class QualityCheckDialog {
    private static final Logger logger = LoggerFactory.getLogger(QualityCheckDialog.class);
    
    private final Stage stage;
    private final PackagingService packagingService;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final TextArea resultsArea;
    private final Button checkUpdatesButton;
    private final Button runQualityChecksButton;
    private final Button closeButton;
    
    public QualityCheckDialog(Stage owner) {
        this.stage = new Stage();
        this.packagingService = PackagingService.getInstance();
        
        // Initialize UI components
        this.progressBar = new ProgressBar(0);
        this.statusLabel = new Label("Ready to run checks");
        this.resultsArea = new TextArea();
        this.checkUpdatesButton = new Button("Check for Updates");
        this.runQualityChecksButton = new Button("Run Quality Checks");
        this.closeButton = new Button("Close");
        
        setupUI(owner);
        setupEventHandlers();
    }
    
    private void setupUI(Stage owner) {
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Quality Checks & Updates");
        stage.setResizable(true);
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        
        // Create layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Quality Checks & Updates");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(checkUpdatesButton, runQualityChecksButton);
        
        // Progress section
        Label progressLabel = new Label("Progress");
        progressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        progressBar.setPrefWidth(560);
        progressBar.setVisible(false);
        
        // Results section
        Label resultsLabel = new Label("Results");
        resultsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        resultsArea.setPrefRowCount(15);
        resultsArea.setEditable(false);
        resultsArea.setWrapText(true);
        
        // Close button
        HBox closeBox = new HBox();
        closeBox.setAlignment(Pos.CENTER_RIGHT);
        closeBox.getChildren().add(closeButton);
        
        // Add all components
        root.getChildren().addAll(
            titleLabel,
            buttonBox,
            new Separator(),
            progressLabel,
            statusLabel,
            progressBar,
            new Separator(),
            resultsLabel,
            resultsArea,
            closeBox
        );
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    private void setupEventHandlers() {
        checkUpdatesButton.setOnAction(e -> checkForUpdates());
        runQualityChecksButton.setOnAction(e -> runQualityChecks());
        closeButton.setOnAction(e -> stage.close());
    }
    
    private void checkForUpdates() {
        // Disable buttons and show progress
        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Checking for updates...");
        resultsArea.clear();
        
        CompletableFuture<PackagingService.UpdateCheckResult> future = packagingService.checkForUpdates();
        
        future.thenAccept(result -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                setButtonsEnabled(true);
                
                if (result.getErrorMessage() != null) {
                    statusLabel.setText("Update check failed");
                    resultsArea.setText("Error: " + result.getErrorMessage());
                } else {
                    statusLabel.setText("Update check completed");
                    
                    StringBuilder results = new StringBuilder();
                    results.append("=== Update Check Results ===\n\n");
                    results.append("Current Version: ").append(result.getCurrentVersion()).append("\n");
                    results.append("Latest Version: ").append(result.getLatestVersion()).append("\n");
                    results.append("Status: ").append(result.getMessage()).append("\n\n");
                    
                    if (result.isUpdateAvailable()) {
                        results.append("A new version is available for download.\n");
                        results.append("Please visit the official website to download the latest version.\n");
                    } else {
                        results.append("Your application is up to date.\n");
                    }
                    
                    resultsArea.setText(results.toString());
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                setButtonsEnabled(true);
                statusLabel.setText("Update check failed");
                resultsArea.setText("Error: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    private void runQualityChecks() {
        // Disable buttons and show progress
        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Running quality checks...");
        resultsArea.clear();
        
        CompletableFuture<PackagingService.QualityCheckResult> future = packagingService.runQualityChecks();
        
        future.thenAccept(result -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                setButtonsEnabled(true);
                
                if (result.getErrorMessage() != null) {
                    statusLabel.setText("Quality checks failed");
                    resultsArea.setText("Error: " + result.getErrorMessage());
                } else {
                    statusLabel.setText("Quality checks completed");
                    
                    StringBuilder results = new StringBuilder();
                    results.append("=== Quality Check Results ===\n\n");
                    
                    if (result.isPassed()) {
                        results.append("✅ All quality checks passed!\n\n");
                    } else {
                        results.append("❌ Some quality issues found:\n\n");
                    }
                    
                    if (result.getIssues() != null && !result.getIssues().isEmpty()) {
                        results.append("Issues Found:\n");
                        results.append("=============\n\n");
                        
                        for (PackagingService.QualityIssue issue : result.getIssues()) {
                            String severityIcon = issue.isCritical() ? "🔴" : 
                                                issue.isHigh() ? "🟡" : "🟢";
                            
                            results.append(severityIcon).append(" ").append(issue.getType()).append("\n");
                            results.append("   Severity: ").append(issue.getSeverity()).append("\n");
                            results.append("   Description: ").append(issue.getDescription()).append("\n");
                            if (issue.getLocation() != null) {
                                results.append("   Location: ").append(issue.getLocation()).append("\n");
                            }
                            results.append("\n");
                        }
                    } else {
                        results.append("No issues found. All quality checks passed successfully!\n\n");
                    }
                    
                    results.append("Quality checks included:\n");
                    results.append("- SpotBugs static analysis\n");
                    results.append("- OWASP dependency check\n");
                    results.append("- Security scan\n");
                    
                    resultsArea.setText(results.toString());
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                setButtonsEnabled(true);
                statusLabel.setText("Quality checks failed");
                resultsArea.setText("Error: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    private void setButtonsEnabled(boolean enabled) {
        checkUpdatesButton.setDisable(!enabled);
        runQualityChecksButton.setDisable(!enabled);
    }
    
    public void showAndWait() {
        stage.showAndWait();
    }
}
