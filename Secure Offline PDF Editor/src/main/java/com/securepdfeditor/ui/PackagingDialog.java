package com.securepdfeditor.ui;

import com.securepdfeditor.packaging.PackagingService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class PackagingDialog {
    private static final Logger logger = LoggerFactory.getLogger(PackagingDialog.class);
    
    private final Stage stage;
    private final PackagingService packagingService;
    private final TextField outputDirField;
    private final TextField appNameField;
    private final TextField versionField;
    private final TextArea descriptionArea;
    private final TextField vendorField;
    private final TextField copyrightField;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Button buildButton;
    private final Button cancelButton;
    
    public PackagingDialog(Stage owner) {
        this.stage = new Stage();
        this.packagingService = PackagingService.getInstance();
        
        // Initialize UI components
        this.outputDirField = new TextField();
        this.appNameField = new TextField("Secure Offline PDF Editor");
        this.versionField = new TextField("1.0.0");
        this.descriptionArea = new TextArea("A secure, offline PDF editor with advanced features");
        this.vendorField = new TextField("Secure PDF Editor Team");
        this.copyrightField = new TextField("Copyright 2024");
        this.progressBar = new ProgressBar(0);
        this.statusLabel = new Label("Ready to build package");
        this.buildButton = new Button("Build Package");
        this.cancelButton = new Button("Cancel");
        
        setupUI(owner);
        setupEventHandlers();
    }
    
    private void setupUI(Stage owner) {
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Build Package");
        stage.setResizable(false);
        
        // Create layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(500);
        
        // Package options section
        Label optionsLabel = new Label("Package Options");
        optionsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(10);
        optionsGrid.setVgap(10);
        
        // Output directory
        optionsGrid.add(new Label("Output Directory:"), 0, 0);
        HBox outputDirBox = new HBox(5);
        outputDirField.setPrefWidth(300);
        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> browseOutputDirectory());
        outputDirBox.getChildren().addAll(outputDirField, browseButton);
        optionsGrid.add(outputDirBox, 1, 0);
        
        // App name
        optionsGrid.add(new Label("App Name:"), 0, 1);
        optionsGrid.add(appNameField, 1, 1);
        
        // Version
        optionsGrid.add(new Label("Version:"), 0, 2);
        optionsGrid.add(versionField, 1, 2);
        
        // Description
        optionsGrid.add(new Label("Description:"), 0, 3);
        descriptionArea.setPrefRowCount(3);
        optionsGrid.add(descriptionArea, 1, 3);
        
        // Vendor
        optionsGrid.add(new Label("Vendor:"), 0, 4);
        optionsGrid.add(vendorField, 1, 4);
        
        // Copyright
        optionsGrid.add(new Label("Copyright:"), 0, 5);
        optionsGrid.add(copyrightField, 1, 5);
        
        // Progress section
        Label progressLabel = new Label("Build Progress");
        progressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        progressBar.setPrefWidth(460);
        progressBar.setVisible(false);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, buildButton);
        
        // Add all components
        root.getChildren().addAll(
            optionsLabel,
            optionsGrid,
            new Separator(),
            progressLabel,
            statusLabel,
            progressBar,
            new Separator(),
            buttonBox
        );
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    private void setupEventHandlers() {
        buildButton.setOnAction(e -> buildPackage());
        cancelButton.setOnAction(e -> stage.close());
        
        // Set default output directory
        String userHome = System.getProperty("user.home");
        outputDirField.setText(userHome + File.separator + "Desktop" + File.separator + "dist");
    }
    
    private void browseOutputDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Directory");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            outputDirField.setText(selected.getAbsolutePath());
        }
    }
    
    private void buildPackage() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Disable build button and show progress
        buildButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate progress
        statusLabel.setText("Building package...");
        
        // Create packaging options
        PackagingService.PackagingOptions options = new PackagingService.PackagingOptions();
        options.setOutputDirectory(outputDirField.getText());
        options.setAppName(appNameField.getText());
        options.setVersion(versionField.getText());
        options.setDescription(descriptionArea.getText());
        options.setVendor(vendorField.getText());
        options.setCopyright(copyrightField.getText());
        
        // Build package asynchronously
        CompletableFuture<PackagingService.PackagingResult> future = packagingService.buildPackage(options);
        
        future.thenAccept(result -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                buildButton.setDisable(false);
                
                if (result.isSuccess()) {
                    statusLabel.setText("Package built successfully!");
                    showSuccessDialog(result);
                    stage.close();
                } else {
                    statusLabel.setText("Build failed: " + result.getErrorMessage());
                    showErrorDialog("Build Failed", result.getErrorMessage());
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                buildButton.setDisable(false);
                statusLabel.setText("Build failed: " + throwable.getMessage());
                showErrorDialog("Build Failed", throwable.getMessage());
            });
            return null;
        });
    }
    
    private boolean validateInputs() {
        if (outputDirField.getText().trim().isEmpty()) {
            showErrorDialog("Validation Error", "Output directory is required");
            return false;
        }
        
        if (appNameField.getText().trim().isEmpty()) {
            showErrorDialog("Validation Error", "App name is required");
            return false;
        }
        
        if (versionField.getText().trim().isEmpty()) {
            showErrorDialog("Validation Error", "Version is required");
            return false;
        }
        
        return true;
    }
    
    private void showSuccessDialog(PackagingService.PackagingResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Build Successful");
        alert.setHeaderText("Package built successfully!");
        
        StringBuilder content = new StringBuilder();
        content.append("Package created: ").append(result.getPackageFile().getFileName()).append("\n\n");
        
        PackagingService.PackageInfo info = result.getPackageInfo();
        if (info != null) {
            content.append("Platform: ").append(info.getPlatform()).append("\n");
            content.append("Version: ").append(info.getVersion()).append("\n");
            content.append("Size: ").append(formatFileSize(info.getTotalSize())).append("\n");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public void showAndWait() {
        stage.showAndWait();
    }
}
