package com.securepdfeditor.ui;

import com.securepdfeditor.i18n.I18nService;
import com.securepdfeditor.ocr.OcrService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OcrProgressDialog {
    private static final Logger logger = LoggerFactory.getLogger(OcrProgressDialog.class);
    
    private final Stage stage;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Label detailsLabel;
    private final Button cancelButton;
    private final I18nService i18n = I18nService.getInstance();
    
    private CompletableFuture<List<OcrService.OcrResult>> ocrFuture;
    private boolean cancelled = false;
    
    public OcrProgressDialog(Stage owner) {
        this.stage = new Stage();
        
        // Initialize UI components
        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(400);
        
        statusLabel = new Label(i18n.getString("message.ocrInProgress"));
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        detailsLabel = new Label("");
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        
        cancelButton = new Button(i18n.getString("button.cancel"));
        cancelButton.setOnAction(e -> cancelOcr());
        
        // Layout
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
            statusLabel,
            progressBar,
            detailsLabel,
            cancelButton
        );
        
        // Stage setup
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(i18n.getString("dialog.ocr.title"));
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        
        // Prevent closing with X button during OCR
        stage.setOnCloseRequest(e -> {
            if (ocrFuture != null && !ocrFuture.isDone()) {
                e.consume();
                cancelOcr();
            }
        });
    }
    
    public CompletableFuture<List<OcrService.OcrResult>> showAndWait(OcrService ocrService) {
        // Start OCR operation
        ocrFuture = ocrService.performOcrAsync(this::updateProgress);
        
        // Handle completion
        ocrFuture.whenComplete((results, throwable) -> {
            Platform.runLater(() -> {
                if (cancelled) {
                    return;
                }
                
                if (throwable != null) {
                    logger.error("OCR failed: {}", throwable.getMessage());
                    showError(i18n.getString("error.ocr"), throwable.getMessage());
                } else {
                    showSuccess(results);
                }
                
                stage.close();
            });
        });
        
        // Show dialog
        stage.showAndWait();
        
        return ocrFuture;
    }
    
    private void updateProgress(OcrService.OcrProgress progress) {
        Platform.runLater(() -> {
            if (cancelled) {
                return;
            }
            
            progressBar.setProgress(progress.getProgress());
            statusLabel.setText(progress.getMessage());
            detailsLabel.setText(String.format("%.1f%% complete", progress.getProgress() * 100));
        });
    }
    
    private void cancelOcr() {
        cancelled = true;
        cancelButton.setDisable(true);
        statusLabel.setText("Cancelling...");
        
        if (ocrFuture != null && !ocrFuture.isDone()) {
            ocrFuture.cancel(true);
        }
        
        stage.close();
    }
    
    private void showSuccess(List<OcrService.OcrResult> results) {
        int totalPages = results.size();
        int pagesWithText = (int) results.stream().filter(OcrService.OcrResult::hasText).count();
        
        String message = i18n.getString("message.ocrCompleted");
        String details = String.format("Processed %d pages, found text on %d pages", totalPages, pagesWithText);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18n.getString("dialog.ocr.title"));
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public boolean wasCancelled() {
        return cancelled;
    }
}
