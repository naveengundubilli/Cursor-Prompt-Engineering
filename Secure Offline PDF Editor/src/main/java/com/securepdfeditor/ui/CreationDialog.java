package com.securepdfeditor.ui;

import com.securepdfeditor.creation.CreationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Dialog for creating new PDF documents with various options.
 */
public class CreationDialog {
    
    private final CreationService creationService;
    private final Stage parentStage;
    
    public CreationDialog(CreationService creationService, Stage parentStage) {
        this.creationService = creationService;
        this.parentStage = parentStage;
    }
    
    /**
     * Show the PDF creation dialog
     */
    public Optional<Path> showDialog() {
        Dialog<Path> dialog = new Dialog<>();
        dialog.setTitle("Create New PDF");
        dialog.setHeaderText("Create a new PDF document");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the custom content
        VBox content = createDialogContent();
        dialog.getDialogPane().setContent(content);
        
        // Set the result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return createPdf();
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Create the dialog content
     */
    private VBox createDialogContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);
        
        // Page Size Section
        Label sizeLabel = new Label("Page Size:");
        ComboBox<CreationService.PageSize> sizeComboBox = new ComboBox<>();
        sizeComboBox.getItems().addAll(creationService.getAvailablePageSizes());
        sizeComboBox.setValue(CreationService.PageSize.A4);
        sizeComboBox.setPrefWidth(200);
        
        // Orientation Section
        Label orientationLabel = new Label("Orientation:");
        ComboBox<CreationService.Orientation> orientationComboBox = new ComboBox<>();
        orientationComboBox.getItems().addAll(CreationService.Orientation.values());
        orientationComboBox.setValue(CreationService.Orientation.PORTRAIT);
        orientationComboBox.setPrefWidth(200);
        
        // Page Count Section
        Label pageCountLabel = new Label("Number of Pages:");
        Spinner<Integer> pageCountSpinner = new Spinner<>(1, 100, 1);
        pageCountSpinner.setEditable(true);
        pageCountSpinner.setPrefWidth(100);
        
        // Template Section
        Label templateLabel = new Label("Template:");
        ComboBox<String> templateComboBox = new ComboBox<>();
        templateComboBox.getItems().addAll(creationService.getAvailableTemplates());
        templateComboBox.setValue("Blank");
        templateComboBox.setPrefWidth(200);
        
        // Template description
        Label templateDescription = new Label();
        templateDescription.setWrapText(true);
        templateDescription.setMaxWidth(300);
        templateDescription.setStyle("-fx-text-fill: gray;");
        
        // Update template description when selection changes
        templateComboBox.setOnAction(e -> {
            String selectedTemplate = templateComboBox.getValue();
            if (selectedTemplate != null) {
                // Get template description (simplified for now)
                String description = getTemplateDescription(selectedTemplate);
                templateDescription.setText(description);
            }
        });
        
        // Initial template description
        templateDescription.setText(getTemplateDescription("Blank"));
        
        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(sizeLabel, 0, 0);
        grid.add(sizeComboBox, 1, 0);
        
        grid.add(orientationLabel, 0, 1);
        grid.add(orientationComboBox, 1, 1);
        
        grid.add(pageCountLabel, 0, 2);
        grid.add(pageCountSpinner, 1, 2);
        
        grid.add(templateLabel, 0, 3);
        grid.add(templateComboBox, 1, 3);
        
        grid.add(templateDescription, 1, 4);
        
        content.getChildren().addAll(grid);
        
        return content;
    }
    
    /**
     * Get template description
     */
    private String getTemplateDescription(String templateName) {
        switch (templateName) {
            case "Blank":
                return "A clean blank page with no pre-defined content";
            case "Lined Page":
                return "A lined paper template with horizontal lines and red left margin";
            case "Grid Page":
                return "A grid paper template with fine grid lines for drawing and diagrams";
            case "Title Page":
                return "A professional title page template with centered title, subtitle, and author areas";
            case "Meeting Notes":
                return "A meeting notes template with sections for agenda, attendees, action items, and notes";
            default:
                return "Template description not available";
        }
    }
    
    /**
     * Create the PDF based on user selections
     */
    private Path createPdf() {
        try {
            // Show file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF As");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("new-document.pdf");
            
            File selectedFile = fileChooser.showSaveDialog(parentStage);
            if (selectedFile == null) {
                return null;
            }
            
            Path filePath = selectedFile.toPath();
            
            // For now, create a simple blank document
            // In a full implementation, you would get the values from the dialog controls
            PDDocument document = creationService.createBlankDocument(
                CreationService.PageSize.A4,
                CreationService.Orientation.PORTRAIT,
                1
            );
            
            creationService.saveDocument(document, filePath, null);
            document.close();
            
            return filePath;
            
        } catch (IOException e) {
            showError("Error creating PDF", "Failed to create PDF: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
