package com.securepdfeditor.editing;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditingService {
    private static final Logger logger = LoggerFactory.getLogger(EditingService.class);

    private PDDocument document;
    private final List<EditAction> editHistory = new ArrayList<>();
    private final List<EditAction> selectedObjects = new ArrayList<>();
    private Rectangle2D selectionBounds = null;

    public void setDocument(PDDocument document) {
        this.document = document;
    }

    public boolean isDocumentLoaded() {
        return document != null;
    }

    // Text Editing
    public void addText(int pageIndex, String text, float x, float y, float fontSize, Color color) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);
        PDFont font = new org.apache.pdfbox.pdmodel.font.PDType1Font(Standard14Fonts.FontName.HELVETICA);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.setFont(font, fontSize);
            contentStream.setNonStrokingColor(convertToPDColor(color));
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        }

        EditAction action = new EditAction(EditType.TEXT_ADD, pageIndex, text, x, y, fontSize, color);
        editHistory.add(action);
        logger.info("Added text to page {}: '{}' at ({}, {})", pageIndex, text, x, y);
    }

    public void deleteText(int pageIndex, float x, float y, float width, float height) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);

        // Create a white rectangle to cover the text area
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.setNonStrokingColor(convertToPDColor(Color.WHITE));
            contentStream.addRect(x, y, width, height);
            contentStream.fill();
        }

        EditAction action = new EditAction(EditType.TEXT_DELETE, pageIndex, null, x, y, width, height);
        editHistory.add(action);
        logger.info("Deleted text area on page {}: ({}, {}) {}x{}", pageIndex, x, y, width, height);
    }

    // Image Insertion
    public void insertImage(int pageIndex, Path imagePath, float x, float y, float width, float height) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);
        PDImageXObject image = PDImageXObject.createFromFile(imagePath.toString(), document);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.drawImage(image, x, y, width, height);
        }

        EditAction action = new EditAction(EditType.IMAGE_INSERT, pageIndex, imagePath.toString(), x, y, width, height);
        editHistory.add(action);
        logger.info("Inserted image on page {}: {} at ({}, {}) {}x{}", pageIndex, imagePath.getFileName(), x, y, width, height);
    }

    // Shape Drawing
    public void drawRectangle(int pageIndex, float x, float y, float width, float height, Color fillColor, Color strokeColor, float strokeWidth) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            if (fillColor != null) {
                contentStream.setNonStrokingColor(convertToPDColor(fillColor));
                contentStream.addRect(x, y, width, height);
                contentStream.fill();
            }

            if (strokeColor != null && strokeWidth > 0) {
                contentStream.setStrokingColor(convertToPDColor(strokeColor));
                contentStream.setLineWidth(strokeWidth);
                contentStream.addRect(x, y, width, height);
                contentStream.stroke();
            }
        }

        EditAction action = new EditAction(EditType.SHAPE_RECTANGLE, pageIndex, null, x, y, width, height);
        action.setColors(fillColor, strokeColor);
        action.setStrokeWidth(strokeWidth);
        editHistory.add(action);
        logger.info("Drew rectangle on page {}: ({}, {}) {}x{}", pageIndex, x, y, width, height);
    }

    public void drawCircle(int pageIndex, float centerX, float centerY, float radius, Color fillColor, Color strokeColor, float strokeWidth) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            if (fillColor != null) {
                contentStream.setNonStrokingColor(convertToPDColor(fillColor));
                // Draw circle using bezier curves
                drawCirclePath(contentStream, centerX, centerY, radius);
                contentStream.fill();
            }

            if (strokeColor != null && strokeWidth > 0) {
                contentStream.setStrokingColor(convertToPDColor(strokeColor));
                contentStream.setLineWidth(strokeWidth);
                drawCirclePath(contentStream, centerX, centerY, radius);
                contentStream.stroke();
            }
        }

        EditAction action = new EditAction(EditType.SHAPE_CIRCLE, pageIndex, null, centerX, centerY, radius, radius);
        action.setColors(fillColor, strokeColor);
        action.setStrokeWidth(strokeWidth);
        editHistory.add(action);
        logger.info("Drew circle on page {}: center ({}, {}) radius {}", pageIndex, centerX, centerY, radius);
    }

    private void drawCirclePath(PDPageContentStream contentStream, float centerX, float centerY, float radius) throws IOException {
        // Draw circle using bezier curves (approximation)
        float kappa = 0.5522848f;
        float x = centerX - radius;
        float y = centerY - radius;
        float w = radius * 2;
        float h = radius * 2;

        contentStream.moveTo(x + radius, y);
        contentStream.curveTo(x + radius + kappa * radius, y, x + w, y + radius - kappa * radius, x + w, y + radius);
        contentStream.curveTo(x + w, y + radius + kappa * radius, x + radius + kappa * radius, y + h, x + radius, y + h);
        contentStream.curveTo(x + radius - kappa * radius, y + h, x, y + radius + kappa * radius, x, y + radius);
        contentStream.curveTo(x, y + radius - kappa * radius, x + radius - kappa * radius, y, x + radius, y);
    }

    public void drawLine(int pageIndex, float x1, float y1, float x2, float y2, Color color, float strokeWidth) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.setStrokingColor(convertToPDColor(color));
            contentStream.setLineWidth(strokeWidth);
            contentStream.moveTo(x1, y1);
            contentStream.lineTo(x2, y2);
            contentStream.stroke();
        }

        EditAction action = new EditAction(EditType.SHAPE_LINE, pageIndex, null, x1, y1, x2 - x1, y2 - y1);
        action.setColors(null, color);
        action.setStrokeWidth(strokeWidth);
        editHistory.add(action);
        logger.info("Drew line on page {}: ({}, {}) to ({}, {})", pageIndex, x1, y1, x2, y2);
    }

    // Annotation Tools using PDAnnotationTextMarkup via subclass
    public void addHighlight(int pageIndex, float x, float y, float width, float height, Color color) throws IOException {
        addTextMarkup(pageIndex, x, y, width, height, color, "Highlight", EditType.ANNOTATION_HIGHLIGHT);
    }

    public void addUnderline(int pageIndex, float x, float y, float width, float height, Color color) throws IOException {
        addTextMarkup(pageIndex, x, y, width, height, color, "Underline", EditType.ANNOTATION_UNDERLINE);
    }

    public void addStrikethrough(int pageIndex, float x, float y, float width, float height, Color color) throws IOException {
        addTextMarkup(pageIndex, x, y, width, height, color, "StrikeOut", EditType.ANNOTATION_STRIKETHROUGH);
    }

    public void addStickyNote(int pageIndex, float x, float y, String noteText, Color color) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);
        PDAnnotationText stickyNote = new PDAnnotationText();
        
        PDRectangle rect = new PDRectangle(x, y, 20, 20); // Small icon size
        stickyNote.setRectangle(rect);
        stickyNote.setColor(convertToPDColor(color));
        stickyNote.setContents(noteText);
        stickyNote.setTitlePopup("Note");
        stickyNote.setOpen(false); // Start closed
        
        page.getAnnotations().add(stickyNote);

        EditAction action = new EditAction(EditType.ANNOTATION_STICKY_NOTE, pageIndex, noteText, x, y, 20, 20);
        action.setColors(color, null);
        editHistory.add(action);
        logger.info("Added sticky note on page {}: '{}' at ({}, {})", pageIndex, noteText, x, y);
    }

    private void addTextMarkup(int pageIndex, float x, float y, float width, float height, Color color, String subtype, EditType type) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");

        PDPage page = document.getPage(pageIndex);
        PDAnnotationTextMarkup markup = new PublicTextMarkup(subtype);

        PDRectangle rect = new PDRectangle(x, y, width, height);
        markup.setRectangle(rect);
        markup.setColor(convertToPDColor(color));
        // QuadPoints order: x1 y2 x2 y2 x1 y1 x2 y1
        float[] quads = new float[]{x, y + height, x + width, y + height, x, y, x + width, y};
        markup.setQuadPoints(quads);

        page.getAnnotations().add(markup);

        EditAction action = new EditAction(type, pageIndex, null, x, y, width, height);
        action.setColors(color, null);
        editHistory.add(action);
        logger.info("Added {} on page {}: ({}, {}) {}x{}", subtype, pageIndex, x, y, width, height);
    }

    // Multi-object Selection
    public void startSelection() {
        selectedObjects.clear();
        selectionBounds = null;
    }

    public void addToSelection(EditAction action) {
        if (!selectedObjects.contains(action)) {
            selectedObjects.add(action);
            updateSelectionBounds();
        }
    }

    public void removeFromSelection(EditAction action) {
        selectedObjects.remove(action);
        updateSelectionBounds();
    }

    public void clearSelection() {
        selectedObjects.clear();
        selectionBounds = null;
    }

    public List<EditAction> getSelectedObjects() {
        return new ArrayList<>(selectedObjects);
    }

    public Rectangle2D getSelectionBounds() {
        return selectionBounds;
    }

    private void updateSelectionBounds() {
        if (selectedObjects.isEmpty()) {
            selectionBounds = null;
            return;
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (EditAction action : selectedObjects) {
            minX = Math.min(minX, action.getX());
            minY = Math.min(minY, action.getY());
            maxX = Math.max(maxX, action.getX() + action.getWidth());
            maxY = Math.max(maxY, action.getY() + action.getHeight());
        }

        selectionBounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean isInSelectionBounds(double x, double y) {
        return selectionBounds != null && selectionBounds.contains(x, y);
    }

    // Layer Management
    public void bringForward(EditAction action) {
        int index = editHistory.indexOf(action);
        if (index < editHistory.size() - 1) {
            editHistory.remove(index);
            editHistory.add(index + 1, action);
            logger.info("Brought forward: {}", action);
        }
    }

    public void bringBackward(EditAction action) {
        int index = editHistory.indexOf(action);
        if (index > 0) {
            editHistory.remove(index);
            editHistory.add(index - 1, action);
            logger.info("Brought backward: {}", action);
        }
    }

    public void bringToFront(EditAction action) {
        editHistory.remove(action);
        editHistory.add(action);
        logger.info("Brought to front: {}", action);
    }

    public void sendToBack(EditAction action) {
        editHistory.remove(action);
        editHistory.add(0, action);
        logger.info("Sent to back: {}", action);
    }

    // History Management
    public List<EditAction> getEditHistory() {
        return new ArrayList<>(editHistory);
    }

    public void clearHistory() {
        editHistory.clear();
        selectedObjects.clear();
        selectionBounds = null;
        logger.info("Cleared edit history");
    }

    	public int getHistorySize() {
		return editHistory.size();
	}
	
	public void restoreHistory(List<EditAction> history) {
		editHistory.clear();
		if (history != null) {
			editHistory.addAll(history);
		}
		logger.info("Restored {} edit actions", editHistory.size());
	}

    // Utility methods
    public Rectangle2D getTextBounds(String text, PDFont font, float fontSize) throws IOException {
        // Simplified bounds calculation
        float width = font.getStringWidth(text) * fontSize / 1000f;
        float height = font.getFontDescriptor().getFontBoundingBox().getHeight() * fontSize / 1000f;
        return new Rectangle2D.Float(0, 0, width, height);
    }

    private PDColor convertToPDColor(Color color) {
        return new PDColor(new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f}, PDDeviceRGB.INSTANCE);
    }

    public enum EditType {
        TEXT_ADD, TEXT_DELETE,
        IMAGE_INSERT,
        SHAPE_RECTANGLE, SHAPE_CIRCLE, SHAPE_LINE,
        ANNOTATION_HIGHLIGHT, ANNOTATION_UNDERLINE, ANNOTATION_STRIKETHROUGH, ANNOTATION_STICKY_NOTE
    }
}
