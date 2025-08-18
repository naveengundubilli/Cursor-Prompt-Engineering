package com.securepdfeditor.security;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;

/**
 * Global exception handler for the Secure PDF Editor application.
 * Provides safe error dialogs and structured logging for uncaught exceptions.
 */
public class GlobalExceptionHandler implements UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static volatile GlobalExceptionHandler instance;
    
    private GlobalExceptionHandler() {}
    
    public static GlobalExceptionHandler getInstance() {
        if (instance == null) {
            synchronized (GlobalExceptionHandler.class) {
                if (instance == null) {
                    instance = new GlobalExceptionHandler();
                }
            }
        }
        return instance;
    }
    
    	/**
	 * Install the global exception handler for both default and JavaFX threads.
	 */
	public void install() {
		Thread.setDefaultUncaughtExceptionHandler(this);
		// Note: Platform.setExceptionHandler is not available in all JavaFX versions
		// The default uncaught exception handler will catch most exceptions
		logger.info("Global exception handler installed");
	}
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        handleException(throwable);
    }
    
    /**
     * Handle exceptions from JavaFX application thread.
     */
    public void handleException(Throwable throwable) {
        if (throwable == null) return;
        
        // Log the exception with full context
        logger.error("Uncaught exception in thread: {}", 
            Thread.currentThread().getName(), throwable);
        
        // Show error dialog on JavaFX thread if possible
        if (Platform.isFxApplicationThread()) {
            showErrorDialog(throwable);
        } else {
            Platform.runLater(() -> showErrorDialog(throwable));
        }
    }
    
    /**
     * Show a safe error dialog with exception details.
     */
    private void showErrorDialog(Throwable throwable) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("An unexpected error occurred");
            alert.setContentText("The application encountered an error and may need to be restarted.");
            
            // Create expandable exception details
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String exceptionText = sw.toString();
            
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);
            
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(false);
            
            // Add buttons
            alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info("User acknowledged error dialog");
            }
        } catch (Exception dialogError) {
            // Fallback: just log if dialog creation fails
            logger.error("Failed to show error dialog", dialogError);
        }
    }
    
    /**
     * Handle specific application errors with custom messages.
     */
    public void handleApplicationError(String title, String message, Throwable cause) {
        logger.error("Application error: {} - {}", title, message, cause);
        
        if (Platform.isFxApplicationThread()) {
            showApplicationErrorDialog(title, message, cause);
        } else {
            Platform.runLater(() -> showApplicationErrorDialog(title, message, cause));
        }
    }
    
    private void showApplicationErrorDialog(String title, String message, Throwable cause) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(message);
            alert.setContentText("Please check the application logs for more details.");
            
            if (cause != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                cause.printStackTrace(pw);
                String exceptionText = sw.toString();
                
                TextArea textArea = new TextArea(exceptionText);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);
                
                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(textArea, 0, 0);
                
                alert.getDialogPane().setExpandableContent(expContent);
                alert.getDialogPane().setExpanded(false);
            }
            
            alert.showAndWait();
        } catch (Exception dialogError) {
            logger.error("Failed to show application error dialog", dialogError);
        }
    }
}
