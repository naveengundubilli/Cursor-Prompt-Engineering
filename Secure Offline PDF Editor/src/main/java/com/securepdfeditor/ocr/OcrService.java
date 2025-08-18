package com.securepdfeditor.ocr;

import com.securepdfeditor.pdf.PdfService;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OcrService {
    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);
    
    private final Tesseract tesseract;
    private final PdfService pdfService;
    private boolean isInitialized = false;
    
    public OcrService(PdfService pdfService) {
        this.pdfService = pdfService;
        this.tesseract = new Tesseract();
        initializeTesseract();
    }
    
    private void initializeTesseract() {
        try {
            // Set data path to bundled tessdata (English only)
            String tessdataPath = getTessdataPath();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("eng");
            
            // Configure OCR settings for better accuracy
            tesseract.setPageSegMode(3); // PSM_AUTO
            tesseract.setOcrEngineMode(3); // OEM_DEFAULT
            
            isInitialized = true;
            logger.info("Tesseract initialized with data path: {}", tessdataPath);
        } catch (Exception e) {
            logger.error("Failed to initialize Tesseract: {}", e.getMessage());
            isInitialized = false;
        }
    }
    
    private String getTessdataPath() {
        // Try to find tessdata in common locations
        String[] possiblePaths = {
            "tessdata", // Local tessdata directory
            "src/main/resources/tessdata", // Bundled tessdata
            System.getProperty("user.home") + "/tessdata", // User home
            "/usr/share/tessdata", // Linux system
            "C:/Program Files/Tesseract-OCR/tessdata" // Windows system
        };
        
        for (String path : possiblePaths) {
            Path tessdataPath = Path.of(path);
            if (tessdataPath.toFile().exists() && tessdataPath.toFile().isDirectory()) {
                return path;
            }
        }
        
        // Fallback to local tessdata
        return "tessdata";
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public CompletableFuture<List<OcrResult>> performOcrAsync(Consumer<OcrProgress> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return performOcr(progressCallback);
            } catch (Exception e) {
                logger.error("OCR failed: {}", e.getMessage());
                throw new RuntimeException("OCR processing failed", e);
            }
        });
    }
    
    public List<OcrResult> performOcr(Consumer<OcrProgress> progressCallback) throws IOException {
        if (!isInitialized) {
            throw new IllegalStateException("OCR service not initialized");
        }
        
        if (!pdfService.isOpen()) {
            throw new IllegalStateException("No PDF document open");
        }
        
        List<OcrResult> results = new ArrayList<>();
        int totalPages = pdfService.getPageCount();
        
        PDFRenderer renderer = new PDFRenderer(pdfService.getDocument());
        
        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            try {
                // Update progress
                if (progressCallback != null) {
                    progressCallback.accept(new OcrProgress(pageIndex + 1, totalPages, 
                        String.format("Processing page %d of %d", pageIndex + 1, totalPages)));
                }
                
                // Render page to image
                PDPage page = pdfService.getDocument().getPage(pageIndex);
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300, ImageType.RGB);
                
                // Perform OCR
                String text = tesseract.doOCR(image);
                
                // Create OCR result
                OcrResult result = new OcrResult(pageIndex, text.trim(), image.getWidth(), image.getHeight());
                results.add(result);
                
                logger.info("OCR completed for page {}: {} characters", pageIndex + 1, text.length());
                
            } catch (TesseractException e) {
                logger.error("OCR failed for page {}: {}", pageIndex + 1, e.getMessage());
                // Add empty result for failed page
                results.add(new OcrResult(pageIndex, "", 0, 0));
            }
        }
        
        if (progressCallback != null) {
            progressCallback.accept(new OcrProgress(totalPages, totalPages, "OCR completed"));
        }
        
        logger.info("OCR completed for {} pages", totalPages);
        return results;
    }
    
    public OcrResult performOcrOnPage(int pageIndex) throws IOException {
        if (!isInitialized) {
            throw new IllegalStateException("OCR service not initialized");
        }
        
        if (!pdfService.isOpen()) {
            throw new IllegalStateException("No PDF document open");
        }
        
        if (pageIndex < 0 || pageIndex >= pdfService.getPageCount()) {
            throw new IllegalArgumentException("Invalid page index: " + pageIndex);
        }
        
        try {
            PDFRenderer renderer = new PDFRenderer(pdfService.getDocument());
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300, ImageType.RGB);
            String text = tesseract.doOCR(image);
            
            OcrResult result = new OcrResult(pageIndex, text.trim(), image.getWidth(), image.getHeight());
            logger.info("OCR completed for page {}: {} characters", pageIndex + 1, text.length());
            return result;
            
        } catch (TesseractException e) {
            logger.error("OCR failed for page {}: {}", pageIndex + 1, e.getMessage());
            throw new IOException("OCR processing failed for page " + (pageIndex + 1), e);
        }
    }
    
    public void injectOcrTextLayer(List<OcrResult> ocrResults) throws IOException {
        if (!pdfService.isOpen()) {
            throw new IllegalStateException("No PDF document open");
        }
        
        // This would inject the OCR text as a selectable text layer
        // Implementation depends on PDFBox capabilities for text layer injection
        logger.info("Injecting OCR text layer for {} pages", ocrResults.size());
        
        // TODO: Implement text layer injection using PDFBox
        // This is a placeholder for the actual implementation
        for (OcrResult result : ocrResults) {
            if (!result.getText().isEmpty()) {
                logger.debug("Would inject text for page {}: {} characters", 
                    result.getPageIndex() + 1, result.getText().length());
            }
        }
    }
    
    public static class OcrResult {
        private final int pageIndex;
        private final String text;
        private final int imageWidth;
        private final int imageHeight;
        
        public OcrResult(int pageIndex, String text, int imageWidth, int imageHeight) {
            this.pageIndex = pageIndex;
            this.text = text;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }
        
        public int getPageIndex() { return pageIndex; }
        public String getText() { return text; }
        public int getImageWidth() { return imageWidth; }
        public int getImageHeight() { return imageHeight; }
        public boolean hasText() { return text != null && !text.trim().isEmpty(); }
        
        @Override
        public String toString() {
            return String.format("OcrResult{page=%d, textLength=%d, size=%dx%d}", 
                pageIndex + 1, text.length(), imageWidth, imageHeight);
        }
    }
    
    public static class OcrProgress {
        private final int currentPage;
        private final int totalPages;
        private final String message;
        
        public OcrProgress(int currentPage, int totalPages, String message) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.message = message;
        }
        
        public int getCurrentPage() { return currentPage; }
        public int getTotalPages() { return totalPages; }
        public String getMessage() { return message; }
        public double getProgress() { 
            return totalPages > 0 ? (double) currentPage / totalPages : 0.0; 
        }
        
        @Override
        public String toString() {
            return String.format("OcrProgress{%d/%d, %.1f%%, %s}", 
                currentPage, totalPages, getProgress() * 100, message);
        }
    }
}
