package com.securepdfeditor.ui;

import com.securepdfeditor.signing.SigningService;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public class SignaturePanel {
    private static final Logger logger = LoggerFactory.getLogger(SignaturePanel.class);
    
    private final Stage stage;
    private final SigningService signingService;
    private final int pageIndex;
    private final float x, y, width, height;
    
    private Canvas drawCanvas;
    private GraphicsContext gc;
    private boolean isDrawing = false;
    private double lastX, lastY;
    
    public SignaturePanel(SigningService signingService, int pageIndex, float x, float y, float width, float height) {
        this.signingService = signingService;
        this.pageIndex = pageIndex;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        this.stage = new Stage();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setTitle("Add Signature");
        this.stage.setResizable(false);
        
        createUI();
    }
    
    private void createUI() {
        BorderPane root = new BorderPane();
        
        // Create tab pane
        TabPane tabPane = new TabPane();
        
        // Draw tab
        Tab drawTab = new Tab("Draw", createDrawTab());
        drawTab.setClosable(false);
        
        // Upload tab
        Tab uploadTab = new Tab("Upload", createUploadTab());
        uploadTab.setClosable(false);
        
        // Certificate tab
        Tab certificateTab = new Tab("Certificate", createCertificateTab());
        certificateTab.setClosable(false);
        
        tabPane.getTabs().addAll(drawTab, uploadTab, certificateTab);
        root.setCenter(tabPane);
        
        // Bottom buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> stage.close());
        
        buttonBox.getChildren().addAll(cancelBtn);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
    }
    
    private VBox createDrawTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        // Canvas for drawing
        drawCanvas = new Canvas(400, 200);
        gc = drawCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        
        // Drawing event handlers
        drawCanvas.setOnMousePressed(this::handleMousePressed);
        drawCanvas.setOnMouseDragged(this::handleMouseDragged);
        drawCanvas.setOnMouseReleased(this::handleMouseReleased);
        
        // Clear button
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            gc.clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
        });
        
        // Apply button
        Button applyBtn = new Button("Apply Drawn Signature");
        applyBtn.setOnAction(e -> applyDrawnSignature());
        
        vbox.getChildren().addAll(
            new Label("Draw your signature:"),
            drawCanvas,
            new HBox(10, clearBtn, applyBtn)
        );
        
        return vbox;
    }
    
    private VBox createUploadTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        // Image preview
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        
        // File chooser button
        Button chooseBtn = new Button("Choose Image");
        chooseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
            }
        });
        
        // Apply button
        Button applyBtn = new Button("Apply Image Signature");
        applyBtn.setOnAction(e -> {
            if (imageView.getImage() != null) {
                applyImageSignature(Path.of(chooseBtn.getUserData().toString()));
            }
        });
        
        vbox.getChildren().addAll(
            new Label("Upload signature image:"),
            imageView,
            new HBox(10, chooseBtn, applyBtn)
        );
        
        return vbox;
    }
    
    private VBox createCertificateTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        // Keystore file chooser
        Button chooseKeystoreBtn = new Button("Choose PKCS#12 Keystore");
        chooseKeystoreBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PKCS#12 Files", "*.p12", "*.pfx")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                chooseKeystoreBtn.setUserData(file.toPath());
                chooseKeystoreBtn.setText("Keystore: " + file.getName());
            }
        });
        
        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Keystore Password");
        
        // Load keystore button
        Button loadBtn = new Button("Load Keystore");
        loadBtn.setOnAction(e -> {
            if (chooseKeystoreBtn.getUserData() != null && !passwordField.getText().isEmpty()) {
                try {
                    Path keystorePath = (Path) chooseKeystoreBtn.getUserData();
                    signingService.loadKeystore(keystorePath, passwordField.getText());
                    loadBtn.setText("Keystore Loaded: " + signingService.getKeyAlias());
                } catch (Exception ex) {
                    showError("Failed to load keystore: " + ex.getMessage());
                }
            }
        });
        
        // Reason and location fields
        TextField reasonField = new TextField();
        reasonField.setPromptText("Signing Reason");
        TextField locationField = new TextField();
        locationField.setPromptText("Signing Location");
        
        // Apply button
        Button applyBtn = new Button("Apply Digital Signature");
        applyBtn.setOnAction(e -> {
            if (signingService.isKeystoreLoaded()) {
                applyDigitalSignature(reasonField.getText(), locationField.getText());
            } else {
                showError("Please load a keystore first");
            }
        });
        
        vbox.getChildren().addAll(
            new Label("Digital Certificate Signing:"),
            chooseKeystoreBtn,
            new HBox(10, new Label("Password:"), passwordField),
            loadBtn,
            new HBox(10, new Label("Reason:"), reasonField),
            new HBox(10, new Label("Location:"), locationField),
            applyBtn
        );
        
        return vbox;
    }
    
    private void handleMousePressed(MouseEvent event) {
        isDrawing = true;
        lastX = event.getX();
        lastY = event.getY();
    }
    
    private void handleMouseDragged(MouseEvent event) {
        if (isDrawing) {
            gc.strokeLine(lastX, lastY, event.getX(), event.getY());
            lastX = event.getX();
            lastY = event.getY();
        }
    }
    
    private void handleMouseReleased(MouseEvent event) {
        isDrawing = false;
    }
    
    private void applyDrawnSignature() {
        try {
            // Convert canvas to BufferedImage
            BufferedImage signatureImage = SwingFXUtils.fromFXImage(drawCanvas.snapshot(null, null), null);
            signingService.addDrawnSignature(pageIndex, signatureImage, x, y, width, height);
            stage.close();
        } catch (Exception e) {
            showError("Failed to apply drawn signature: " + e.getMessage());
        }
    }
    
    private void applyImageSignature(Path imagePath) {
        try {
            signingService.addImageSignature(pageIndex, imagePath, x, y, width, height);
            stage.close();
        } catch (Exception e) {
            showError("Failed to apply image signature: " + e.getMessage());
        }
    }
    
    private void applyDigitalSignature(String reason, String location) {
        try {
            signingService.signDocument(pageIndex, x, y, width, height, reason, location);
            stage.close();
        } catch (Exception e) {
            showError("Failed to apply digital signature: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
    
    public void show() {
        stage.showAndWait();
    }
}
