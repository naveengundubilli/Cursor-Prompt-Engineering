package com.securepdfeditor;

import com.securepdfeditor.editing.EditAction;
import com.securepdfeditor.editing.EditingService;
import com.securepdfeditor.layout.LayoutService;
import com.securepdfeditor.pdf.PdfService;
import com.securepdfeditor.security.NetworkGuard;
import com.securepdfeditor.signing.SigningService;
import com.securepdfeditor.ui.SignaturePanel;
import com.securepdfeditor.forms.FormsService;
import com.securepdfeditor.navigation.SearchNavigationService;
import com.securepdfeditor.assembly.AssemblyService;
import com.securepdfeditor.security.GlobalExceptionHandler;
import com.securepdfeditor.ui.PrintPreviewDialog;
import com.securepdfeditor.ocr.OcrService;
import com.securepdfeditor.i18n.I18nService;
import com.securepdfeditor.ui.OcrProgressDialog;
import com.securepdfeditor.packaging.PackagingService;
import com.securepdfeditor.ui.PackagingDialog;
import com.securepdfeditor.ui.QualityCheckDialog;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Orientation;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class App extends Application {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private final PdfService pdfService = new PdfService();
	private final EditingService editingService = new EditingService();
	private final SigningService signingService = new SigningService();
	private final LayoutService layoutService = new LayoutService();
	private final FormsService formsService = new FormsService();
	private final SearchNavigationService searchService = new SearchNavigationService();
	private final AssemblyService assemblyService = new AssemblyService();
	private final com.securepdfeditor.workspace.WorkspaceService workspaceService = new com.securepdfeditor.workspace.WorkspaceService();
	private final com.securepdfeditor.history.HistoryService historyService = new com.securepdfeditor.history.HistoryService();
	private final OcrService ocrService = new OcrService(pdfService);
	private final I18nService i18nService = I18nService.getInstance();
	private final PackagingService packagingService = PackagingService.getInstance();

	private final ImageView imageView = new ImageView();
	private final ScrollPane scrollPane = new ScrollPane();
	private final StackPane centeredView = new StackPane(); // For centered display
	private final Label statusLabel = new Label("Ready");
	private final Label pageLabel = new Label("Page 0 / 0");
	
	// Bottom toolbar components
	private TextField pageNumberField;
	private Label totalPagesLabel;
	private ComboBox<String> zoomCombo;

	
	// Side panel for layers/objects
	private final ListView<EditAction> layersList = new ListView<>();

	private double currentZoomPercent = 75.0; // default 75%
	private int currentPageIndex = 0;

	private record ViewerState(int pageIndex, double zoomPercent) {}
	private final Deque<ViewerState> undoStack = new ArrayDeque<>();
	private final Deque<ViewerState> redoStack = new ArrayDeque<>();

	@Override
	public void start(Stage stage) {
		// Install global exception handler
		GlobalExceptionHandler.getInstance().install();
		
		initializeSecurity();

		BorderPane root = buildUi(stage);
		Scene scene = new Scene(root, 1400, 900);
		stage.setTitle("Secure Offline PDF Editor - Adobe-Style Interface");
		stage.setScene(scene);
		stage.show();
	}

	private void initializeSecurity() {
		boolean offline = Boolean.getBoolean("app.offline") || "true".equalsIgnoreCase(System.getProperty("offline", "false"));
		NetworkGuard.AppPolicy.setOffline(offline);
		logger.info("Starting in {} mode", offline ? "offline" : "online");
	}

	private BorderPane buildUi(Stage stage) {
		BorderPane root = new BorderPane();

		MenuBar menuBar = buildMenuBar(stage);
		root.setTop(menuBar);

		// Main content area
		HBox contentArea = new HBox();
		
		// Left side panel for layers
		VBox sidePanel = buildSidePanel();
		sidePanel.setPrefWidth(250);
		sidePanel.setMinWidth(200);
		
		// Center PDF viewer with centering
		setupCenteredView();
		HBox.setHgrow(centeredView, Priority.ALWAYS);
		
		contentArea.getChildren().addAll(sidePanel, centeredView);
		root.setCenter(contentArea);

		// Bottom toolbar with zoom and page controls
		HBox bottomToolbar = buildBottomToolbar();
		root.setBottom(bottomToolbar);

		return root;
	}

	private MenuBar buildMenuBar(Stage stage) {
		MenuBar menuBar = new MenuBar();

		// File Menu
		Menu fileMenu = new Menu("File");
		MenuItem openItem = new MenuItem("Open...");
		openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		openItem.setOnAction(e -> doOpen(stage));
		MenuItem saveItem = new MenuItem("Save");
		saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		saveItem.setOnAction(e -> doSave(stage));
		MenuItem saveAsItem = new MenuItem("Save As...");
		saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		saveAsItem.setOnAction(e -> doSaveAs(stage));
		MenuItem importPages = new MenuItem("Import PDF Pages...");
		importPages.setOnAction(e -> {
			if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
			java.util.List<File> files = fc.showOpenMultipleDialog(stage);
			if (files == null || files.isEmpty()) return;
			try {
				java.util.List<java.nio.file.Path> paths = files.stream().map(File::toPath).toList();
				assemblyService.mergeIntoCurrent(paths);
				renderCurrentPage();
				updateStatus();
			} catch (Exception ex) { showError("Import failed: " + ex.getMessage()); }
		});
		MenuItem exportItem = new MenuItem("Export...");
		exportItem.setOnAction(e -> showExportDialog(stage));
		MenuItem exportPdfAItem = new MenuItem("Export as PDF/A...");
		exportPdfAItem.setOnAction(e -> showPdfAExportDialog(stage));
		MenuItem saveWorkspaceItem = new MenuItem("Save Workspace...");
		saveWorkspaceItem.setOnAction(e -> saveWorkspace(stage));
		MenuItem loadWorkspaceItem = new MenuItem("Load Workspace...");
		loadWorkspaceItem.setOnAction(e -> loadWorkspace(stage));
		MenuItem printItem = new MenuItem("Print...");
		printItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
		printItem.setOnAction(e -> showPrintDialog());
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.setOnAction(e -> stage.close());
		fileMenu.getItems().addAll(openItem, saveItem, saveAsItem, new SeparatorMenuItem(), 
			importPages, exportItem, exportPdfAItem, new SeparatorMenuItem(), 
			saveWorkspaceItem, loadWorkspaceItem, new SeparatorMenuItem(), 
			printItem, new SeparatorMenuItem(), exitItem);

		// Edit Menu
		Menu editMenu = new Menu("Edit");
		MenuItem undoItem = new MenuItem("Undo");
		undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		undoItem.setOnAction(e -> undo());
		MenuItem redoItem = new MenuItem("Redo");
		redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
		redoItem.setOnAction(e -> redo());
		MenuItem cutItem = new MenuItem("Cut");
		cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
		cutItem.setOnAction(e -> cut());
		MenuItem copyItem = new MenuItem("Copy");
		copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
		copyItem.setOnAction(e -> copy());
		MenuItem pasteItem = new MenuItem("Paste");
		pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
		pasteItem.setOnAction(e -> paste());
		MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
		deleteItem.setOnAction(e -> delete());
		MenuItem preferencesItem = new MenuItem("Preferences...");
		preferencesItem.setOnAction(e -> showPreferencesDialog());
		editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(), 
			cutItem, copyItem, pasteItem, deleteItem, new SeparatorMenuItem(), preferencesItem);

		// View Menu
		Menu viewMenu = new Menu("View");
		MenuItem zoomInItem = new MenuItem("Zoom In");
		zoomInItem.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));
		zoomInItem.setOnAction(e -> setZoom(currentZoomPercent + 25));
		MenuItem zoomOutItem = new MenuItem("Zoom Out");
		zoomOutItem.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
		zoomOutItem.setOnAction(e -> setZoom(currentZoomPercent - 25));
		MenuItem fitPageItem = new MenuItem("Fit Page");
		fitPageItem.setOnAction(e -> setZoom(100));
		MenuItem fitWidthItem = new MenuItem("Fit Width");
		fitWidthItem.setOnAction(e -> setZoom(75));
		MenuItem singlePageItem = new MenuItem("Single Page");
		singlePageItem.setOnAction(e -> setSinglePageMode());
		MenuItem continuousItem = new MenuItem("Continuous Scroll");
		continuousItem.setOnAction(e -> setContinuousMode());
		MenuItem prevPageItem = new MenuItem("Previous Page");
		prevPageItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_UP));
		prevPageItem.setOnAction(e -> goToPage(currentPageIndex - 1));
		MenuItem nextPageItem = new MenuItem("Next Page");
		nextPageItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_DOWN));
		nextPageItem.setOnAction(e -> goToPage(currentPageIndex + 1));
		viewMenu.getItems().addAll(zoomInItem, zoomOutItem, new SeparatorMenuItem(), 
			fitPageItem, fitWidthItem, new SeparatorMenuItem(), singlePageItem, continuousItem, new SeparatorMenuItem(),
			prevPageItem, nextPageItem);

		// Tools Menu
		Menu toolsMenu = new Menu("Tools");
		
		// Text Tools
		MenuItem textItem = new MenuItem("Add Text");
		textItem.setOnAction(e -> addText());
		
		// Image Tools
		MenuItem imageItem = new MenuItem("Insert Image");
		imageItem.setOnAction(e -> insertImage(stage));
		
		// Shape Tools
		MenuItem rectItem = new MenuItem("Draw Rectangle");
		rectItem.setOnAction(e -> drawRectangle());
		MenuItem circleItem = new MenuItem("Draw Circle");
		circleItem.setOnAction(e -> drawCircle());
		MenuItem lineItem = new MenuItem("Draw Line");
		lineItem.setOnAction(e -> drawLine());
		
		// Annotation Tools
		MenuItem highlightItem = new MenuItem("Add Highlight");
		highlightItem.setOnAction(e -> addHighlight());
		MenuItem underlineItem = new MenuItem("Add Underline");
		underlineItem.setOnAction(e -> addUnderline());
		MenuItem stickyNoteItem = new MenuItem("Add Sticky Note");
		stickyNoteItem.setOnAction(e -> addStickyNote());
		
		// Redaction Tools
		MenuItem redactItem = new MenuItem("Add Redaction");
		redactItem.setOnAction(e -> addRedaction());
		MenuItem clearRedactItem = new MenuItem("Clear Redactions");
		clearRedactItem.setOnAction(e -> clearRedactions());
		
		// OCR Tools
		MenuItem ocrItem = new MenuItem("Run OCR");
		ocrItem.setOnAction(e -> runOcr(stage));
		
		// Layout Tools
		MenuItem gridItem = new MenuItem("Toggle Grid");
		gridItem.setOnAction(e -> toggleGrid());
		MenuItem snapItem = new MenuItem("Toggle Snap");
		snapItem.setOnAction(e -> toggleSnap());
		
		// Page Management
		MenuItem addPageItem = new MenuItem("Add Page");
		addPageItem.setOnAction(e -> addPage());
		MenuItem deletePageItem = new MenuItem("Delete Page");
		deletePageItem.setOnAction(e -> deletePage());
		
		toolsMenu.getItems().addAll(
			textItem, imageItem, new SeparatorMenuItem(),
			rectItem, circleItem, lineItem, new SeparatorMenuItem(),
			highlightItem, underlineItem, stickyNoteItem, new SeparatorMenuItem(),
			redactItem, clearRedactItem, ocrItem, new SeparatorMenuItem(),
			gridItem, snapItem, new SeparatorMenuItem(),
			addPageItem, deletePageItem
		);

		// Forms Menu
		Menu formsMenu = new Menu("Forms");
		MenuItem addTextField = new MenuItem("Add Text Field");
		addTextField.setOnAction(e -> {
			if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
			try { formsService.createTextField("text1", currentPageIndex, 100, 700, 200, 20); }
			catch (Exception ex) { showError("Add text field failed: " + ex.getMessage()); }
		});
		MenuItem addCheckbox = new MenuItem("Add Checkbox");
		addCheckbox.setOnAction(e -> {
			if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
			try { formsService.createCheckBox("check1", currentPageIndex, 100, 670, 16); }
			catch (Exception ex) { showError("Add checkbox failed: " + ex.getMessage()); }
		});
		MenuItem addRadio = new MenuItem("Add Radio Button");
		addRadio.setOnAction(e -> {
			if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
			try { formsService.createRadioButton("radio1", currentPageIndex, 100, 640, 16, "A"); }
			catch (Exception ex) { showError("Add radio failed: " + ex.getMessage()); }
		});
		MenuItem fillFormItem = new MenuItem("Fill Form...");
		fillFormItem.setOnAction(e -> showFillFormDialog());
		MenuItem validateFormItem = new MenuItem("Validate Form");
		validateFormItem.setOnAction(e -> validateForm());
		MenuItem exportJson = new MenuItem("Export Data to JSON...");
		exportJson.setOnAction(e -> {
			if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
			File target = fc.showSaveDialog(stage); if (target == null) return;
			try { formsService.exportDataToJson(target.toPath()); }
			catch (Exception ex) { showError("Export JSON failed: " + ex.getMessage()); }
		});
		MenuItem exportXml = new MenuItem("Export Data to XML...");
		exportXml.setOnAction(e -> {
			if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
			File target = fc.showSaveDialog(stage); if (target == null) return;
			try { formsService.exportDataToXml(target.toPath()); }
			catch (Exception ex) { showError("Export XML failed: " + ex.getMessage()); }
		});
		formsMenu.getItems().addAll(addTextField, addCheckbox, addRadio, new SeparatorMenuItem(), 
			fillFormItem, validateFormItem, new SeparatorMenuItem(), exportJson, exportXml);

		// Sign Menu
		Menu signMenu = new Menu("Sign");
		MenuItem drawSignatureItem = new MenuItem("Draw Signature...");
		drawSignatureItem.setOnAction(e -> showSignaturePanel());
		MenuItem uploadSignatureItem = new MenuItem("Upload Signature Image...");
		uploadSignatureItem.setOnAction(e -> uploadSignatureImage(stage));
		MenuItem certificateSignItem = new MenuItem("Certificate Signing...");
		certificateSignItem.setOnAction(e -> showCertificateSigningDialog(stage));
		MenuItem validateSignaturesItem = new MenuItem("Validate Signatures");
		validateSignaturesItem.setOnAction(e -> validateSignatures());
		signMenu.getItems().addAll(drawSignatureItem, uploadSignatureItem, certificateSignItem, 
			new SeparatorMenuItem(), validateSignaturesItem);

		// Window Menu
		Menu windowMenu = new Menu("Window");
		MenuItem layersPanelItem = new MenuItem("Show Layers Panel");
		layersPanelItem.setOnAction(e -> toggleLayersPanel());
		MenuItem bookmarksPanelItem = new MenuItem("Show Bookmarks Panel");
		bookmarksPanelItem.setOnAction(e -> toggleBookmarksPanel());
		MenuItem tocPanelItem = new MenuItem("Show Table of Contents");
		tocPanelItem.setOnAction(e -> showTOCPanel());
		MenuItem findPanelItem = new MenuItem("Show Find Panel");
		findPanelItem.setOnAction(e -> showFindPanel());
		windowMenu.getItems().addAll(layersPanelItem, bookmarksPanelItem, tocPanelItem, findPanelItem);

		// Build Menu
		Menu buildMenu = new Menu("Build");
		MenuItem buildPackageItem = new MenuItem("Build Package");
		buildPackageItem.setOnAction(e -> showBuildPackageDialog(stage));
		MenuItem qualityChecksItem = new MenuItem("Quality Checks & Updates");
		qualityChecksItem.setOnAction(e -> showQualityChecksDialog(stage));
		buildMenu.getItems().addAll(buildPackageItem, qualityChecksItem);

		// Help Menu
		Menu helpMenu = new Menu("Help");
		MenuItem aboutItem = new MenuItem("About");
		aboutItem.setOnAction(e -> showAboutDialog());
		MenuItem userGuideItem = new MenuItem("User Guide");
		userGuideItem.setOnAction(e -> showUserGuide());
		helpMenu.getItems().addAll(aboutItem, userGuideItem);

		menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, formsMenu, signMenu, windowMenu, buildMenu, helpMenu);
		return menuBar;
	}

	private void setupCenteredView() {
		// Create a StackPane to center the image
		centeredView.setAlignment(Pos.CENTER);
		centeredView.getChildren().add(imageView);
		
		// Add the StackPane to the ScrollPane
		scrollPane.setContent(centeredView);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		
		// Ensure the image stays centered
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);
	}
	
	private VBox buildSidePanel() {
		VBox panel = new VBox(10);
		panel.setPadding(new Insets(10));
		panel.setStyle("-fx-background-color: #f0f0f0;");
		
		Label titleLabel = new Label("Layers & Objects");
		titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		
		layersList.setPrefHeight(300);
		layersList.setCellFactory(param -> new ListCell<EditAction>() {
			@Override
			protected void updateItem(EditAction item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(String.format("%s (Page %d)", item.getType(), item.getPageIndex() + 1));
				}
			}
		});
		
		// Context menu for layers
		layersList.setContextMenu(createLayersContextMenu());
		
		Button refreshBtn = new Button("Refresh");
		refreshBtn.setOnAction(e -> refreshLayersList());
		
		panel.getChildren().addAll(titleLabel, layersList, refreshBtn);
		return panel;
	}
	
	private ContextMenu createLayersContextMenu() {
		ContextMenu contextMenu = new ContextMenu();
		
		MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setOnAction(e -> {
			EditAction selected = layersList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				deleteSelectedObject(selected);
			}
		});
		
		MenuItem propertiesItem = new MenuItem("Properties");
		propertiesItem.setOnAction(e -> {
			EditAction selected = layersList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				showPropertiesDialog(selected);
			}
		});

		MenuItem bringForwardItem = new MenuItem("Bring Forward");
		bringForwardItem.setOnAction(e -> {
			EditAction selected = layersList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				editingService.bringForward(selected);
				refreshLayersList();
			}
		});

		MenuItem bringBackwardItem = new MenuItem("Bring Backward");
		bringBackwardItem.setOnAction(e -> {
			EditAction selected = layersList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				editingService.bringBackward(selected);
				refreshLayersList();
			}
		});

		MenuItem bringToFrontItem = new MenuItem("Bring to Front");
		bringToFrontItem.setOnAction(e -> {
			EditAction selected = layersList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				editingService.bringToFront(selected);
				refreshLayersList();
			}
		});

		MenuItem sendToBackItem = new MenuItem("Send to Back");
		sendToBackItem.setOnAction(e -> {
			EditAction selected = layersList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				editingService.sendToBack(selected);
				refreshLayersList();
			}
		});
		
		contextMenu.getItems().addAll(
			deleteItem, new SeparatorMenuItem(), 
			propertiesItem, new SeparatorMenuItem(),
			bringForwardItem, bringBackwardItem, new SeparatorMenuItem(),
			bringToFrontItem, sendToBackItem
		);
		return contextMenu;
	}

	private void deleteSelectedObject(EditAction action) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Object");
		alert.setHeaderText("Delete " + action.getType() + "?");
		alert.setContentText("This action cannot be undone.");
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			// For now, just remove from history - in a full implementation,
			// we would need to remove the actual content from the PDF
			editingService.getEditHistory().remove(action);
			refreshLayersList();
			updateStatus();
		}
	}
	
	private void showPropertiesDialog(EditAction action) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Object Properties");
		alert.setHeaderText("Properties for " + action.getType());
		
		StringBuilder content = new StringBuilder();
		content.append(String.format("Type: %s\n", action.getType()));
		content.append(String.format("Page: %d\n", action.getPageIndex() + 1));
		content.append(String.format("Position: (%.1f, %.1f)\n", action.getX(), action.getY()));
		content.append(String.format("Size: %.1f x %.1f\n", action.getWidth(), action.getHeight()));
		
		if (action.getFontSize() > 0) {
			content.append(String.format("Font Size: %.1f\n", action.getFontSize()));
		}
		if (action.getStrokeWidth() > 0) {
			content.append(String.format("Stroke Width: %.1f\n", action.getStrokeWidth()));
		}
		if (action.getContent() != null && !action.getContent().isEmpty()) {
			content.append(String.format("Content: %s\n", action.getContent()));
		}
		
		alert.setContentText(content.toString());
		alert.showAndWait();
	}
	
	private void refreshLayersList() {
		layersList.getItems().clear();
		layersList.getItems().addAll(editingService.getEditHistory());
	}

	private HBox buildBottomToolbar() {
		HBox toolbar = new HBox(10);
		toolbar.setPadding(new Insets(6, 10, 6, 10));
		toolbar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

		// Page navigation section
		Label pageLabel = new Label("Page:");
		pageLabel.setStyle("-fx-font-weight: bold;");
		
		Button prevPageBtn = new Button("◀");
		prevPageBtn.setTooltip(new Tooltip("Previous Page"));
		prevPageBtn.setOnAction(e -> goToPage(currentPageIndex - 1));
		prevPageBtn.setPrefWidth(30);
		
		TextField pageNumberField = new TextField("1");
		pageNumberField.setPrefWidth(50);
		pageNumberField.setAlignment(Pos.CENTER);
		pageNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.matches("\\d*")) {
				pageNumberField.setText(newVal.replaceAll("[^\\d]", ""));
			}
		});
		pageNumberField.setOnAction(e -> {
			try {
				int pageNum = Integer.parseInt(pageNumberField.getText());
				if (pageNum > 0 && pageNum <= pdfService.getPageCount()) {
					goToPage(pageNum - 1);
				}
			} catch (NumberFormatException ex) {
				// Ignore invalid input
			}
		});
		
		Label ofLabel = new Label("of");
		Label totalPagesLabel = new Label("0");
		totalPagesLabel.setStyle("-fx-font-weight: bold;");
		
		Button nextPageBtn = new Button("▶");
		nextPageBtn.setTooltip(new Tooltip("Next Page"));
		nextPageBtn.setOnAction(e -> goToPage(currentPageIndex + 1));
		nextPageBtn.setPrefWidth(30);

		// Zoom section
		Separator zoomSeparator = new Separator(Orientation.VERTICAL);
		zoomSeparator.setPadding(new Insets(0, 10, 0, 10));
		
		Label zoomLabel = new Label("Zoom:");
		zoomLabel.setStyle("-fx-font-weight: bold;");
		
		Button zoomOutBtn = new Button("-");
		zoomOutBtn.setTooltip(new Tooltip("Zoom Out"));
		zoomOutBtn.setOnAction(e -> setZoom(currentZoomPercent - 25));
		zoomOutBtn.setPrefWidth(30);
		
		ComboBox<String> zoomCombo = new ComboBox<>();
		zoomCombo.getItems().addAll("25%", "50%", "75%", "100%", "125%", "150%", "200%", "300%");
		zoomCombo.setValue("75%");
		zoomCombo.setPrefWidth(80);
		zoomCombo.setOnAction(e -> {
			String selected = zoomCombo.getValue();
			if (selected != null) {
				double zoom = Double.parseDouble(selected.replace("%", ""));
				setZoom(zoom);
			}
		});
		
		Button zoomInBtn = new Button("+");
		zoomInBtn.setTooltip(new Tooltip("Zoom In"));
		zoomInBtn.setOnAction(e -> setZoom(currentZoomPercent + 25));
		zoomInBtn.setPrefWidth(30);

		// Fit buttons
		Separator fitSeparator = new Separator(Orientation.VERTICAL);
		fitSeparator.setPadding(new Insets(0, 10, 0, 10));
		
		Button fitPageBtn = new Button("Fit Page");
		fitPageBtn.setTooltip(new Tooltip("Fit to Page"));
		fitPageBtn.setOnAction(e -> setZoom(100));
		fitPageBtn.setPrefWidth(70);
		
		Button fitWidthBtn = new Button("Fit Width");
		fitWidthBtn.setTooltip(new Tooltip("Fit to Width"));
		fitWidthBtn.setOnAction(e -> setZoom(75));
		fitWidthBtn.setPrefWidth(70);

		// Status section (right side)
		HBox statusSection = new HBox(10);
		HBox.setHgrow(statusSection, Priority.ALWAYS);
		statusSection.setAlignment(Pos.CENTER_RIGHT);
		
		statusSection.getChildren().addAll(statusLabel, new Separator(Orientation.VERTICAL), pageLabel);

		// Add all components to toolbar
		toolbar.getChildren().addAll(
			pageLabel, prevPageBtn, pageNumberField, ofLabel, totalPagesLabel, nextPageBtn,
			zoomSeparator,
			zoomLabel, zoomOutBtn, zoomCombo, zoomInBtn,
			fitSeparator,
			fitPageBtn, fitWidthBtn,
			statusSection
		);

		// Store references for updates
		this.pageNumberField = pageNumberField;
		this.totalPagesLabel = totalPagesLabel;
		this.zoomCombo = zoomCombo;

		return toolbar;
	}



	private void doOpen(Stage stage) {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
		File chosen = fc.showOpenDialog(stage);
		if (chosen == null) return;

		Path file = chosen.toPath();
		
		// Validate signatures if present
		if (signingService.validateSignaturesOnOpen(file)) {
			logger.info("Signature validation passed");
		} else {
			logger.warn("Signature validation failed or warnings found");
		}
		
		// Check if PDF is password protected before attempting to open
		if (pdfService.isPasswordProtected(file)) {
			String pwd = promptPassword("This PDF is password protected. Enter password:");
			if (pwd == null) return;
			try {
				pdfService.open(file, pwd);
				afterDocumentLoaded();
			} catch (Exception e) {
				showError("Failed to open PDF with password: " + e.getMessage());
			}
		} else {
			try {
				pdfService.open(file, null);
				afterDocumentLoaded();
			} catch (InvalidPasswordException ex) {
				// Fallback: if we get password exception despite checking, prompt for password
				String pwd = promptPassword("This PDF requires a password. Enter password:");
				if (pwd == null) return;
				try {
					pdfService.open(file, pwd);
					afterDocumentLoaded();
				} catch (Exception e) {
					showError("Failed to open PDF with password: " + e.getMessage());
				}
			} catch (Exception e) {
				showError("Failed to open PDF: " + e.getMessage());
			}
		}
	}

	private void afterDocumentLoaded() {
		currentPageIndex = 0;
		currentZoomPercent = 75.0;
		undoStack.clear();
		redoStack.clear();
		
		// Set up services with the loaded document
		PDDocument document = pdfService.getDocument();
		editingService.setDocument(document);
		signingService.setDocument(document);
		layoutService.setDocument(document);
		formsService.setDocument(document);
		searchService.setDocument(document);
		assemblyService.setDocument(document);
		
		renderCurrentPage();
		updateStatus();
		refreshLayersList();
	}

	private void showFindDialog() {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Find");
		dialog.setHeaderText("Enter search text:");
		var res = dialog.showAndWait();
		if (res.isEmpty() || res.get().trim().isEmpty()) return;
		try {
			var matches = searchService.search(res.get().trim());
			if (matches.isEmpty()) {
				Alert a = new Alert(Alert.AlertType.INFORMATION, "No matches found", ButtonType.OK);
				a.setHeaderText("Find"); a.showAndWait();
				return;
			}
			// Jump to first match for simplicity
			goToPage(matches.get(0).pageIndex);
			statusLabel.setText("Found " + matches.size() + " match(es)");
		} catch (Exception ex) {
			showError("Find failed: " + ex.getMessage());
		}
	}

	private void doSave(Stage stage) {
		try {
			if (pdfService.getCurrentPath() != null) {
				pdfService.save();
				statusLabel.setText("Saved: " + pdfService.getCurrentPath().getFileName());
				return;
			}
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
			File target = fc.showSaveDialog(stage);
			if (target == null) return;
			pdfService.saveAs(target.toPath());
			statusLabel.setText("Saved: " + target.getName());
		} catch (Exception e) {
			showError("Save failed: " + e.getMessage());
		}
	}

	private void doSaveAs(Stage stage) {
		try {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
			File target = fc.showSaveDialog(stage);
			if (target == null) return;
			pdfService.saveAs(target.toPath());
			statusLabel.setText("Saved As: " + target.getName());
		} catch (Exception e) {
			showError("Save As failed: " + e.getMessage());
		}
	}

	private void showExportDialog(Stage stage) {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Export");
		alert.setHeaderText("Choose export format:");
		alert.setContentText("Export as PDF or form data?");
		
		ButtonType pdfButton = new ButtonType("PDF");
		ButtonType formButton = new ButtonType("Form Data");
		ButtonType cancelButton = ButtonType.CANCEL;
		
		alert.getButtonTypes().setAll(pdfButton, formButton, cancelButton);
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == pdfButton) {
				doSaveAs(stage);
			} else if (result.get() == formButton) {
				showFormExportDialog(stage);
			}
		}
	}

	private void showFormExportDialog(Stage stage) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Export Form Data");
		alert.setHeaderText("Choose format:");
		alert.setContentText("JSON or XML?");
		
		ButtonType jsonButton = new ButtonType("JSON");
		ButtonType xmlButton = new ButtonType("XML");
		ButtonType cancelButton = ButtonType.CANCEL;
		
		alert.getButtonTypes().setAll(jsonButton, xmlButton, cancelButton);
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent()) {
			if (result.get() == jsonButton) {
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
				File target = fc.showSaveDialog(stage);
				if (target != null) {
					try { formsService.exportDataToJson(target.toPath()); }
					catch (Exception ex) { showError("Export JSON failed: " + ex.getMessage()); }
				}
			} else if (result.get() == xmlButton) {
				FileChooser fc = new FileChooser();
				fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
				File target = fc.showSaveDialog(stage);
				if (target != null) {
					try { formsService.exportDataToXml(target.toPath()); }
					catch (Exception ex) { showError("Export XML failed: " + ex.getMessage()); }
				}
			}
		}
	}

	private void showPrintDialog() {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		
		Stage currentStage = (Stage) imageView.getScene().getWindow();
		PrintPreviewDialog previewDialog = new PrintPreviewDialog(
			currentStage, 
			pdfService, 
			0, 
			pdfService.getPageCount() - 1
		);
		previewDialog.showAndWait();
	}
	
	private void showPdfAExportDialog(Stage stage) {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		try {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF/A Files", "*.pdf"));
			fc.setInitialFileName("document_pdfa.pdf");
			File target = fc.showSaveDialog(stage);
			if (target == null) return;
			pdfService.saveAsPdfA(target.toPath());
			showInfo("Exported as PDF/A: " + target.getName());
		} catch (Exception e) {
			showError("PDF/A export failed: " + e.getMessage());
		}
	}
	
	private void saveWorkspace(Stage stage) {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		try {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Workspace Files", "*.sopdfe"));
			fc.setInitialFileName("workspace.sopdfe");
			File target = fc.showSaveDialog(stage);
			if (target == null) return;
			
			workspaceService.saveWorkspace(
				target.toPath(),
				pdfService.getCurrentPath(),
				editingService.getEditHistory(),
				pdfService.getRedactionOverlays()
			);
			showInfo("Workspace saved: " + target.getName());
		} catch (Exception e) {
			showError("Save workspace failed: " + e.getMessage());
		}
	}
	
	private void loadWorkspace(Stage stage) {
		try {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Workspace Files", "*.sopdfe"));
			File source = fc.showOpenDialog(stage);
			if (source == null) return;
			
			com.securepdfeditor.workspace.WorkspaceService.WorkspaceData data = 
				workspaceService.loadWorkspace(source.toPath());
			
			// Load the PDF
			pdfService.open(data.getPdfPath(), null);
			
			// Restore edit history and redactions
			if (data.getEditHistory() != null) {
				editingService.restoreHistory(data.getEditHistory());
			}
			if (data.getRedactions() != null) {
				pdfService.restoreRedactions(data.getRedactions());
			}
			
			renderCurrentPage();
			updateStatus();
			showInfo("Workspace loaded: " + source.getName());
		} catch (Exception e) {
			showError("Load workspace failed: " + e.getMessage());
		}
	}
	
	private void showInfo(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
		alert.setHeaderText("Information");
		alert.showAndWait();
	}

	// Editing methods
	private void addText() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add Text");
		dialog.setHeaderText("Enter text to add:");
		dialog.setContentText("Text:");
		
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && !result.get().trim().isEmpty()) {
			try {
				editingService.addText(currentPageIndex, result.get(), 100, 100, 12, java.awt.Color.BLACK);
				renderCurrentPage();
				refreshLayersList();
				updateStatus();
			} catch (Exception e) {
				showError("Failed to add text: " + e.getMessage());
			}
		}
	}
	
	private void insertImage(Stage stage) {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
		File chosen = fc.showOpenDialog(stage);
		if (chosen == null) return;
		
		try {
			editingService.insertImage(currentPageIndex, chosen.toPath(), 100, 100, 200, 150);
			renderCurrentPage();
			refreshLayersList();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to insert image: " + e.getMessage());
		}
	}
	
	private void drawRectangle() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		try {
			editingService.drawRectangle(currentPageIndex, 100, 100, 200, 100, java.awt.Color.YELLOW, java.awt.Color.BLACK, 2);
			renderCurrentPage();
			refreshLayersList();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to draw rectangle: " + e.getMessage());
		}
	}
	
	private void drawCircle() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		try {
			editingService.drawCircle(currentPageIndex, 200, 200, 50, java.awt.Color.CYAN, java.awt.Color.BLUE, 2);
			renderCurrentPage();
			refreshLayersList();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to draw circle: " + e.getMessage());
		}
	}
	
	private void drawLine() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		try {
			editingService.drawLine(currentPageIndex, 50, 50, 300, 300, java.awt.Color.RED, 3);
			renderCurrentPage();
			refreshLayersList();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to draw line: " + e.getMessage());
		}
	}
	
	private void addHighlight() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		try {
			editingService.addHighlight(currentPageIndex, 100, 100, 200, 20, java.awt.Color.YELLOW);
			renderCurrentPage();
			refreshLayersList();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to add highlight: " + e.getMessage());
		}
	}
	
	private void addUnderline() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		try {
			editingService.addUnderline(currentPageIndex, 100, 120, 200, 5, java.awt.Color.BLUE);
			renderCurrentPage();
			refreshLayersList();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to add underline: " + e.getMessage());
		}
	}

	private void addStickyNote() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add Sticky Note");
		dialog.setHeaderText("Enter note text:");
		dialog.setContentText("Note:");
		
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && !result.get().trim().isEmpty()) {
			try {
				editingService.addStickyNote(currentPageIndex, 100, 100, result.get(), java.awt.Color.YELLOW);
				renderCurrentPage();
				refreshLayersList();
				updateStatus();
			} catch (Exception e) {
				showError("Failed to add sticky note: " + e.getMessage());
			}
		}
	}
	
	private void showSignaturePanel() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		SignaturePanel panel = new SignaturePanel(signingService, currentPageIndex, 100, 100, 200, 100);
		panel.show();
		renderCurrentPage();
		updateStatus();
	}

	private void addRedaction() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		// Simple redaction: add a redaction rectangle to the current page
		// In a real implementation, this would be triggered by mouse selection
		Rectangle redactionRect = new Rectangle(100, 100, 200, 50); // Example rectangle
		try {
			pdfService.addRedaction(currentPageIndex, redactionRect);
			renderCurrentPage(); // Re-render to show redaction
			updateStatus();
		} catch (Exception e) {
			showError("Failed to add redaction: " + e.getMessage());
		}
	}

	private void clearRedactions() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Clear Redactions");
		alert.setHeaderText("Clear all redactions?");
		alert.setContentText("This will remove all redaction overlays from the current document.");
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			pdfService.clearAllRedactions();
			renderCurrentPage();
			updateStatus();
		}
	}
	
	private void runOcr(Stage stage) {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		if (!ocrService.isInitialized()) {
			showError("OCR service not initialized. Please check tessdata installation.");
			return;
		}
		
		OcrProgressDialog progressDialog = new OcrProgressDialog(stage);
		
		progressDialog.showAndWait(ocrService).thenAccept(results -> {
			if (!progressDialog.wasCancelled() && results != null) {
				try {
					ocrService.injectOcrTextLayer(results);
					showInfo("OCR completed successfully");
				} catch (IOException e) {
					showError("OCR failed: " + e.getMessage());
				}
			}
		});
	}

	private void toggleGrid() {
		boolean enabled = !layoutService.isGridEnabled();
		layoutService.setGridEnabled(enabled);
		statusLabel.setText("Grid " + (enabled ? "enabled" : "disabled"));
	}

	private void toggleSnap() {
		boolean enabled = !layoutService.isSnapEnabled();
		layoutService.setSnapEnabled(enabled);
		statusLabel.setText("Snap " + (enabled ? "enabled" : "disabled"));
	}

	private void addPage() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		try {
			layoutService.addPage();
			renderCurrentPage();
			updateStatus();
		} catch (Exception e) {
			showError("Failed to add page: " + e.getMessage());
		}
	}

	private void deletePage() {
		if (!pdfService.isOpen()) {
			showError("No PDF document open");
			return;
		}
		
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Page");
		alert.setHeaderText("Delete current page?");
		alert.setContentText("This action cannot be undone.");
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				layoutService.deletePage(currentPageIndex);
				if (currentPageIndex >= layoutService.getPageCount()) {
					currentPageIndex = Math.max(0, layoutService.getPageCount() - 1);
				}
				renderCurrentPage();
				updateStatus();
			} catch (Exception e) {
				showError("Failed to delete page: " + e.getMessage());
			}
		}
	}

	private void goToPage(int newIndex) {
		if (!pdfService.isOpen()) return;
		if (newIndex < 0 || newIndex >= pdfService.getPageCount()) return;
		pushUndo();
		currentPageIndex = newIndex;
		renderCurrentPage();
		updateStatus();
	}

	private void setZoom(double newZoomPercent) {
		if (!pdfService.isOpen()) return;
		double clamped = Math.max(25.0, Math.min(300.0, newZoomPercent));
		if (Math.abs(clamped - currentZoomPercent) < 0.01) return;
		pushUndo();
		currentZoomPercent = clamped;
		renderCurrentPage();
		updateStatus();
	}

	private void pushUndo() {
		undoStack.push(new ViewerState(currentPageIndex, currentZoomPercent));
		redoStack.clear();
	}

	private void undo() {
		if (undoStack.isEmpty()) return;
		redoStack.push(new ViewerState(currentPageIndex, currentZoomPercent));
		ViewerState prev = undoStack.pop();
		currentPageIndex = prev.pageIndex();
		currentZoomPercent = prev.zoomPercent();
		renderCurrentPage();
		updateStatus();
	}

	private void redo() {
		if (redoStack.isEmpty()) return;
		undoStack.push(new ViewerState(currentPageIndex, currentZoomPercent));
		ViewerState next = redoStack.pop();
		currentPageIndex = next.pageIndex();
		currentZoomPercent = next.zoomPercent();
		renderCurrentPage();
		updateStatus();
	}

	private void renderCurrentPage() {
		try {
			BufferedImage bi = pdfService.renderPage(currentPageIndex, currentZoomPercent / 100.0);
			Image fx = SwingFXUtils.toFXImage(bi, null);
			imageView.setImage(fx);
			imageView.setPreserveRatio(true);
		} catch (Exception e) {
			showError("Render failed: " + e.getMessage());
		}
	}

	private void updateStatus() {
		int total = pdfService.isOpen() ? pdfService.getPageCount() : 0;
		pageLabel.setText("Page " + (currentPageIndex + 1) + " / " + total);
		
		// Update toolbar components if they exist
		if (pageNumberField != null) {
			pageNumberField.setText(String.valueOf(currentPageIndex + 1));
		}
		if (totalPagesLabel != null) {
			totalPagesLabel.setText(String.valueOf(total));
		}
		if (zoomCombo != null) {
			zoomCombo.setValue(String.format("%.0f%%", currentZoomPercent));
		}
		
		int redactionCount = pdfService.getRedactionCount();
		int signatureCount = signingService.getSignatureCount();
		int editCount = editingService.getHistorySize();
		String status = String.format("Zoom: %.0f%%", currentZoomPercent);
		if (redactionCount > 0) {
			status += String.format(" | Redactions: %d", redactionCount);
		}
		if (signatureCount > 0) {
			status += String.format(" | Signatures: %d", signatureCount);
		}
		if (editCount > 0) {
			status += String.format(" | Edits: %d", editCount);
		}
		statusLabel.setText(status);
	}

	private void showError(String msg) {
		logger.warn(msg);
		Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
		alert.setHeaderText("Error");
		alert.showAndWait();
	}

	private String promptPassword(String header) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setHeaderText(header);
		dialog.setTitle("Password Required");
		dialog.getEditor().setPromptText("Password");
		Optional<String> res = dialog.showAndWait();
		return res.orElse(null);
	}

	private void cut() {
		// Placeholder for cut functionality
		logger.info("Cut operation requested");
	}

	private void copy() {
		// Placeholder for copy functionality
		logger.info("Copy operation requested");
	}

	private void paste() {
		// Placeholder for paste functionality
		logger.info("Paste operation requested");
	}

	private void delete() {
		// Placeholder for delete functionality
		logger.info("Delete operation requested");
	}

	private void showPreferencesDialog() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Preferences");
		alert.setHeaderText("Application Preferences");
		alert.setContentText("Preferences dialog would open here in a full implementation.");
		alert.showAndWait();
	}

	private void setSinglePageMode() {
		logger.info("Switched to single page mode");
		statusLabel.setText("Single Page Mode");
	}

	private void setContinuousMode() {
		logger.info("Switched to continuous scroll mode");
		statusLabel.setText("Continuous Scroll Mode");
	}

	private void setTool(String tool) {
		logger.info("Selected tool: {}", tool);
		statusLabel.setText("Tool: " + tool);
	}

	private void showFillFormDialog() {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Fill Form Field");
		dialog.setHeaderText("Enter field name and value:");
		dialog.setContentText("Field Name:");
		
		Optional<String> fieldName = dialog.showAndWait();
		if (fieldName.isPresent() && !fieldName.get().trim().isEmpty()) {
			TextInputDialog valueDialog = new TextInputDialog();
			valueDialog.setTitle("Fill Form Field");
			valueDialog.setHeaderText("Enter value for field: " + fieldName.get());
			valueDialog.setContentText("Value:");
			
			Optional<String> value = valueDialog.showAndWait();
			if (value.isPresent()) {
				try {
					formsService.fillTextField(fieldName.get(), value.get());
					renderCurrentPage();
					updateStatus();
				} catch (Exception ex) {
					showError("Failed to fill field: " + ex.getMessage());
				}
			}
		}
	}

	private void validateForm() {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		try {
			java.util.List<String> missing = formsService.validateRequiredFields("name", "email");
			if (missing.isEmpty()) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Form Validation");
				alert.setHeaderText("Validation Result");
				alert.setContentText("All required fields are filled.");
				alert.showAndWait();
			} else {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Form Validation");
				alert.setHeaderText("Missing Required Fields");
				alert.setContentText("Missing fields: " + String.join(", ", missing));
				alert.showAndWait();
			}
		} catch (Exception ex) {
			showError("Form validation failed: " + ex.getMessage());
		}
	}

	private void uploadSignatureImage(Stage stage) {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
		File chosen = fc.showOpenDialog(stage);
		if (chosen == null) return;
		
		try {
			signingService.addImageSignature(currentPageIndex, chosen.toPath(), 100, 100, 200, 100);
			renderCurrentPage();
			updateStatus();
		} catch (Exception ex) {
			showError("Failed to add image signature: " + ex.getMessage());
		}
	}

	private void showCertificateSigningDialog(Stage stage) {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Keystore Files", "*.p12", "*.pfx"));
		File keystore = fc.showOpenDialog(stage);
		if (keystore == null) return;
		
		TextInputDialog pwdDialog = new TextInputDialog();
		pwdDialog.setTitle("Certificate Signing");
		pwdDialog.setHeaderText("Enter keystore password:");
		pwdDialog.setContentText("Password:");
		
		Optional<String> password = pwdDialog.showAndWait();
		if (password.isPresent()) {
			try {
				signingService.loadKeystore(keystore.toPath(), password.get());
				signingService.signDocument(currentPageIndex, 100, 100, 200, 100, "Approved", "Office");
				renderCurrentPage();
				updateStatus();
			} catch (Exception ex) {
				showError("Certificate signing failed: " + ex.getMessage());
			}
		}
	}

	private void validateSignatures() {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		try {
			boolean valid = signingService.verifySignatures();
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Signature Validation");
			alert.setHeaderText("Validation Result");
			alert.setContentText(valid ? "All signatures are valid." : "Some signatures are invalid.");
			alert.showAndWait();
		} catch (Exception ex) {
			showError("Signature validation failed: " + ex.getMessage());
		}
	}

	private void toggleLayersPanel() {
		// Toggle layers panel visibility
		logger.info("Toggle layers panel");
	}

	private void toggleBookmarksPanel() {
		// Toggle bookmarks panel visibility
		logger.info("Toggle bookmarks panel");
	}

	private void showTOCPanel() {
		if (!pdfService.isOpen()) { showError("No PDF document open"); return; }
		try {
			java.util.List<String> entries = new java.util.ArrayList<>();
			for (int i = 0; i < pdfService.getPageCount(); i++) {
				entries.add("Page " + (i + 1));
			}
			searchService.generateSimpleTOC(entries);
			renderCurrentPage();
			updateStatus();
		} catch (Exception ex) {
			showError("TOC generation failed: " + ex.getMessage());
		}
	}

	private void showFindPanel() {
		showFindDialog();
	}

	private void showAboutDialog() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("Secure Offline PDF Editor");
		alert.setContentText("Version 1.0\nMilestone 3\n\nA secure, offline PDF editor with advanced features.");
		alert.showAndWait();
	}

	private void showUserGuide() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("User Guide");
		alert.setHeaderText("User Guide");
		alert.setContentText("User guide would open here in a full implementation.");
		alert.showAndWait();
	}
	
	private void showBuildPackageDialog(Stage stage) {
		PackagingDialog dialog = new PackagingDialog(stage);
		dialog.showAndWait();
	}
	
	private void showQualityChecksDialog(Stage stage) {
		QualityCheckDialog dialog = new QualityCheckDialog(stage);
		dialog.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
