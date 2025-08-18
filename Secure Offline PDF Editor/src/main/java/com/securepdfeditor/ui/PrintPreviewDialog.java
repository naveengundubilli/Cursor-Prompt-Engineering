package com.securepdfeditor.ui;

import com.securepdfeditor.pdf.PdfService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Print preview dialog with multi-page navigation and zoom controls.
 */
public class PrintPreviewDialog {
    private static final Logger logger = LoggerFactory.getLogger(PrintPreviewDialog.class);
    
    private final Stage stage;
    private final PdfService pdfService;
    private final int startPage;
    private final int endPage;
    
    private ImageView previewImageView;
    private Label pageLabel;
    private ComboBox<String> zoomCombo;
    private CheckBox printAllPagesCheckBox;
    private CheckBox printCurrentPageCheckBox;
    private Spinner<Integer> fromPageSpinner;
    private Spinner<Integer> toPageSpinner;
    
    private int currentPreviewPage;
    private double currentZoom = 1.0;
    
    public PrintPreviewDialog(Stage owner, PdfService pdfService, int startPage, int endPage) {
        this.stage = new Stage();
        this.pdfService = pdfService;
        this.startPage = startPage;
        this.endPage = endPage;
        this.currentPreviewPage = startPage;
        
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Print Preview");
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        buildUI();
        loadPreviewPage(currentPreviewPage);
    }
    
    private void buildUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Top controls
        HBox topControls = buildTopControls();
        
        // Preview area
        ScrollPane scrollPane = buildPreviewArea();
        
        // Bottom controls
        HBox bottomControls = buildBottomControls();
        
        root.getChildren().addAll(topControls, scrollPane, bottomControls);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    private HBox buildTopControls() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        // Page navigation
        Button prevPageBtn = new Button("◀ Previous");
        prevPageBtn.setOnAction(e -> navigatePage(-1));
        
        pageLabel = new Label();
        pageLabel.setStyle("-fx-font-weight: bold;");
        
        Button nextPageBtn = new Button("Next ▶");
        nextPageBtn.setOnAction(e -> navigatePage(1));
        
        // Zoom controls
        Label zoomLabel = new Label("Zoom:");
        zoomCombo = new ComboBox<>();
        zoomCombo.getItems().addAll("25%", "50%", "75%", "100%", "125%", "150%", "200%");
        zoomCombo.setValue("100%");
        zoomCombo.setOnAction(e -> {
            String selected = zoomCombo.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    double zoom = Double.parseDouble(selected.replace("%", "")) / 100.0;
                    setZoom(zoom);
                } catch (NumberFormatException ex) {
                    logger.warn("Invalid zoom value: {}", selected);
                }
            }
        });
        
        controls.getChildren().addAll(prevPageBtn, pageLabel, nextPageBtn, new Separator(), zoomLabel, zoomCombo);
        
        return controls;
    }
    
    private ScrollPane buildPreviewArea() {
        previewImageView = new ImageView();
        previewImageView.setPreserveRatio(true);
        previewImageView.setSmooth(true);
        
        ScrollPane scrollPane = new ScrollPane(previewImageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f0f0f0;");
        
        return scrollPane;
    }
    
    private HBox buildBottomControls() {
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        
        // Print options
        VBox printOptions = new VBox(5);
        printOptions.setAlignment(Pos.CENTER_LEFT);
        
        printAllPagesCheckBox = new CheckBox("Print all pages");
        printAllPagesCheckBox.setSelected(true);
        printAllPagesCheckBox.setOnAction(e -> updatePageRangeControls());
        
        printCurrentPageCheckBox = new CheckBox("Print current page only");
        printCurrentPageCheckBox.setOnAction(e -> updatePageRangeControls());
        
        // Page range controls
        HBox pageRangeBox = new HBox(5);
        pageRangeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label fromLabel = new Label("From page:");
        fromPageSpinner = new Spinner<>(1, pdfService.getPageCount(), startPage + 1);
        fromPageSpinner.setEditable(true);
        fromPageSpinner.setPrefWidth(80);
        
        Label toLabel = new Label("To page:");
        toPageSpinner = new Spinner<>(1, pdfService.getPageCount(), endPage + 1);
        toPageSpinner.setEditable(true);
        toPageSpinner.setPrefWidth(80);
        
        pageRangeBox.getChildren().addAll(fromLabel, fromPageSpinner, toLabel, toPageSpinner);
        
        printOptions.getChildren().addAll(printAllPagesCheckBox, printCurrentPageCheckBox, pageRangeBox);
        
        // Buttons
        Button printBtn = new Button("Print");
        printBtn.setDefaultButton(true);
        printBtn.setOnAction(e -> handlePrint());
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> stage.close());
        
        controls.getChildren().addAll(printOptions, new Separator(), printBtn, cancelBtn);
        
        updatePageRangeControls();
        return controls;
    }
    
    private void updatePageRangeControls() {
        boolean useRange = !printAllPagesCheckBox.isSelected() && !printCurrentPageCheckBox.isSelected();
        fromPageSpinner.setDisable(printAllPagesCheckBox.isSelected() || printCurrentPageCheckBox.isSelected());
        toPageSpinner.setDisable(printAllPagesCheckBox.isSelected() || printCurrentPageCheckBox.isSelected());
        
        if (printCurrentPageCheckBox.isSelected()) {
            fromPageSpinner.getValueFactory().setValue(currentPreviewPage + 1);
            toPageSpinner.getValueFactory().setValue(currentPreviewPage + 1);
        }
    }
    
    private void navigatePage(int direction) {
        int newPage = currentPreviewPage + direction;
        if (newPage >= startPage && newPage <= endPage) {
            currentPreviewPage = newPage;
            loadPreviewPage(currentPreviewPage);
        }
    }
    
    private void setZoom(double zoom) {
        currentZoom = zoom;
        loadPreviewPage(currentPreviewPage);
    }
    
    private void loadPreviewPage(int pageIndex) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return pdfService.renderPage(pageIndex, currentZoom);
            } catch (IOException e) {
                logger.error("Failed to render preview page {}", pageIndex, e);
                return null;
            }
        }).thenAcceptAsync(image -> {
            if (image != null) {
                Image fxImage = convertToFxImage(image);
                previewImageView.setImage(fxImage);
                pageLabel.setText(String.format("Page %d of %d", pageIndex + 1, pdfService.getPageCount()));
            } else {
                previewImageView.setImage(null);
                pageLabel.setText("Failed to load preview");
            }
        }, Platform::runLater);
    }
    
    private Image convertToFxImage(BufferedImage bufferedImage) {
        return javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);
    }
    
    private void handlePrint() {
        try {
            int fromPage, toPage;
            
            if (printAllPagesCheckBox.isSelected()) {
                fromPage = 0;
                toPage = pdfService.getPageCount() - 1;
            } else if (printCurrentPageCheckBox.isSelected()) {
                fromPage = currentPreviewPage;
                toPage = currentPreviewPage;
            } else {
                fromPage = fromPageSpinner.getValue() - 1;
                toPage = toPageSpinner.getValue() - 1;
            }
            
            // Validate page range
            if (fromPage < 0 || toPage >= pdfService.getPageCount() || fromPage > toPage) {
                showError("Invalid page range");
                return;
            }
            
            pdfService.printDocument(fromPage, toPage);
            showInfo("Print job sent successfully");
            stage.close();
            
        } catch (Exception e) {
            logger.error("Print failed", e);
            showError("Print failed: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Print Error");
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Print Information");
        alert.showAndWait();
    }
    
    public void showAndWait() {
        stage.showAndWait();
    }
}
