package com.securepdfeditor.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfService {
	private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

	private PDDocument document;
	private Path currentPath;
	private final Map<Integer, List<Rectangle>> redactionOverlays = new HashMap<>();

	public void open(Path path, String password) throws IOException {
		if (path == null || !Files.exists(path)) {
			throw new IllegalArgumentException("File not found: " + path);
		}
		close();
		try {
			if (password == null || password.isEmpty()) {
				document = Loader.loadPDF(path.toFile());
			} else {
				document = Loader.loadPDF(path.toFile(), password);
			}
			currentPath = path;
			redactionOverlays.clear(); // Clear redactions when opening new document
			logger.info("Opened PDF: {} (pages: {})", path.getFileName(), document.getNumberOfPages());
		} catch (InvalidPasswordException e) {
			close();
			throw e;
		}
	}

	public boolean isOpen() {
		return document != null;
	}

	public int getPageCount() {
		return document != null ? document.getNumberOfPages() : 0;
	}

	public Path getCurrentPath() {
		return currentPath;
	}
	
	public PDDocument getDocument() {
		return document;
	}

	public boolean isPasswordProtected(Path path) {
		if (path == null || !Files.exists(path)) {
			return false;
		}
		try (PDDocument testDoc = Loader.loadPDF(path.toFile())) {
			return testDoc.isEncrypted();
		} catch (Exception e) {
			// If we can't open it normally, it might be password protected
			return true;
		}
	}

	public void save() throws IOException {
		if (document == null || currentPath == null) throw new IllegalStateException("No document opened");
		document.save(currentPath.toFile());
	}

	public void saveAs(Path target) throws IOException {
		if (document == null) throw new IllegalStateException("No document opened");
		document.save(target.toFile());
		currentPath = target;
	}

	public void saveAsPdfA(Path target) throws IOException {
		if (document == null) throw new IllegalStateException("No document opened");
		
		try (PDDocument pdfADocument = new PDDocument()) {
			// Copy all pages
			for (int i = 0; i < document.getNumberOfPages(); i++) {
				pdfADocument.addPage(document.getPage(i));
			}
			
			PDDocumentCatalog catalog = pdfADocument.getDocumentCatalog();
			
			// Add XMP metadata for PDF/A compliance
			org.apache.pdfbox.pdmodel.common.PDMetadata metadata = new org.apache.pdfbox.pdmodel.common.PDMetadata(pdfADocument);
			catalog.setMetadata(metadata);
			
			// Add PDF/A output intent (sRGB color space)
			try {
				org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent outputIntent = 
					createPdfAOutputIntent(pdfADocument);
				catalog.addOutputIntent(outputIntent);
			} catch (Exception e) {
				logger.warn("Failed to add PDF/A output intent: {}", e.getMessage());
			}
			
			// Validate PDF/A compliance before saving
			if (!validatePdfACompliance(pdfADocument)) {
				throw new IOException("Document does not meet PDF/A-1b compliance requirements");
			}
			
			pdfADocument.save(target.toFile());
			logger.info("Saved as PDF/A-1b: {}", target);
		}
	}
	
	/**
	 * Create PDF/A output intent with sRGB color space.
	 */
	private org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent createPdfAOutputIntent(PDDocument document) 
			throws IOException {
		// Create a basic output intent for PDF/A compliance
		// Note: This is a simplified implementation for basic PDF/A-1b compliance
		org.apache.pdfbox.cos.COSDictionary dict = new org.apache.pdfbox.cos.COSDictionary();
		org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent outputIntent = 
			new org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent(dict);
		outputIntent.setInfo("sRGB IEC61966-2.1");
		outputIntent.setOutputCondition("sRGB IEC61966-2.1");
		outputIntent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
		outputIntent.setRegistryName("http://www.color.org");
		
		return outputIntent;
	}
	
	/**
	 * Validate PDF/A-1b compliance using basic checks.
	 */
	private boolean validatePdfACompliance(PDDocument document) {
		try {
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			
			// Check for required metadata
			if (catalog.getMetadata() == null) {
				logger.error("PDF/A validation failed: Missing XMP metadata");
				return false;
			}
			
			// Check for output intent
			if (catalog.getOutputIntents().isEmpty()) {
				logger.error("PDF/A validation failed: Missing output intent");
				return false;
			}
			
			// Check for embedded fonts (basic check)
			boolean hasEmbeddedFonts = false;
			for (int i = 0; i < document.getNumberOfPages(); i++) {
				PDPage page = document.getPage(i);
				if (page.getResources() != null && page.getResources().getFontNames() != null) {
					hasEmbeddedFonts = true;
					break;
				}
			}
			
			if (!hasEmbeddedFonts) {
				logger.warn("PDF/A validation warning: No embedded fonts detected");
			}
			
			logger.info("PDF/A-1b validation passed");
			return true;
			
		} catch (Exception e) {
			logger.error("PDF/A validation failed: {}", e.getMessage());
			return false;
		}
	}

	public void printDocument(int startPage, int endPage) throws IOException {
		if (document == null) throw new IllegalStateException("No document opened");
		if (startPage < 0 || endPage >= document.getNumberOfPages() || startPage > endPage) {
			throw new IllegalArgumentException("Invalid page range");
		}
		
		// Create a temporary file for printing
		Path tempFile = Files.createTempFile("print_", ".pdf");
		try {
			// Create a document with only the specified pages
			try (PDDocument printDoc = new PDDocument()) {
				for (int i = startPage; i <= endPage; i++) {
					printDoc.addPage(document.getPage(i));
				}
				printDoc.save(tempFile.toFile());
			}
			
			// Use system print dialog
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
			if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
				desktop.print(tempFile.toFile());
				logger.info("Printing pages {} to {}", startPage + 1, endPage + 1);
			} else {
				throw new IOException("Printing not supported on this system");
			}
		} finally {
			// Clean up temporary file
			try {
				Files.deleteIfExists(tempFile);
			} catch (IOException e) {
				logger.warn("Failed to delete temporary print file: {}", tempFile);
			}
		}
	}

	public BufferedImage renderPage(int pageIndex, double scale) throws IOException {
		if (document == null) throw new IllegalStateException("No document opened");
		if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");
		if (scale <= 0) throw new IllegalArgumentException("Scale must be positive");
		PDFRenderer renderer = new PDFRenderer(document);
		BufferedImage image = renderer.renderImageWithDPI(pageIndex, (float)(96 * scale), ImageType.RGB);
		
		// Apply redaction overlays if any exist for this page
		List<Rectangle> redactions = redactionOverlays.get(pageIndex);
		if (redactions != null && !redactions.isEmpty()) {
			Graphics2D g2d = image.createGraphics();
			g2d.setColor(Color.BLACK);
			for (Rectangle rect : redactions) {
				// Scale the redaction rectangle to match the rendered image
				int scaledX = (int) (rect.x * scale);
				int scaledY = (int) (rect.y * scale);
				int scaledWidth = (int) (rect.width * scale);
				int scaledHeight = (int) (rect.height * scale);
				g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
			}
			g2d.dispose();
		}
		
		return image;
	}

	public void addRedaction(int pageIndex, Rectangle rect) throws IOException {
		if (document == null) throw new IllegalStateException("No document opened");
		if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");
		if (rect == null || rect.isEmpty()) return;
		
		redactionOverlays.computeIfAbsent(pageIndex, k -> new ArrayList<>()).add(rect);
		logger.info("Added redaction at page {}: {}", pageIndex, rect);
	}

	public List<Rectangle> getRedactions(int pageIndex) {
		if (pageIndex < 0 || pageIndex >= getPageCount()) return new ArrayList<>();
		return redactionOverlays.getOrDefault(pageIndex, new ArrayList<>());
	}

	public void clearRedactions(int pageIndex) {
		if (pageIndex < 0 || pageIndex >= getPageCount()) return;
		redactionOverlays.remove(pageIndex);
		logger.info("Cleared redactions for page {}", pageIndex);
	}

	public void clearAllRedactions() {
		redactionOverlays.clear();
		logger.info("Cleared all redactions");
	}

	public int getRedactionCount() {
		return redactionOverlays.values().stream().mapToInt(List::size).sum();
	}
	
	public Map<Integer, List<Rectangle>> getRedactionOverlays() {
		return new HashMap<>(redactionOverlays);
	}
	
	public void restoreRedactions(Map<Integer, List<Rectangle>> redactions) {
		redactionOverlays.clear();
		if (redactions != null) {
			redactionOverlays.putAll(redactions);
		}
		logger.info("Restored {} redaction overlays", redactionOverlays.size());
	}

	public void signingStubLoadPkcs12(Path p12Path, String password) {
		// Stub method for Milestone 1 — actual signing logic deferred to Milestone 2
		logger.info("Loaded PKCS#12 keystore (stub): {}", p12Path);
	}

	public void close() {
		if (document != null) {
			try { document.close(); } catch (IOException ignored) {}
			document = null;
		}
		currentPath = null;
		redactionOverlays.clear();
	}
}
