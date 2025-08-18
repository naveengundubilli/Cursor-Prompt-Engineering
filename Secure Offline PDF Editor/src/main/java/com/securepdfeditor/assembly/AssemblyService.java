package com.securepdfeditor.assembly;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PageExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service for merge, split, and insert operations.
 */
public class AssemblyService {
    private static final Logger logger = LoggerFactory.getLogger(AssemblyService.class);

    private PDDocument document;

    public void setDocument(PDDocument document) { this.document = document; }
    public boolean isDocumentLoaded() { return document != null; }

    // Append source PDFs to current document
    public void mergeIntoCurrent(List<Path> sources) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        PDFMergerUtility merger = new PDFMergerUtility();
        for (Path src : sources) {
            try (PDDocument srcDoc = Loader.loadPDF(src.toFile())) {
                merger.appendDocument(document, srcDoc);
                logger.info("Merged {} pages from {}", srcDoc.getNumberOfPages(), src.getFileName());
            }
        }
    }

    // Split current document range into a new file (1-based inclusive indices)
    public void splitRangeToFile(int startPage, int endPage, Path targetFile) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (startPage < 1 || endPage < startPage || endPage > document.getNumberOfPages()) {
            throw new IllegalArgumentException("Invalid page range");
        }
        PageExtractor extractor = new PageExtractor(document, startPage, endPage);
        try (PDDocument out = extractor.extract()) {
            out.save(targetFile.toFile());
            logger.info("Wrote split document pages {}-{} to {}", startPage, endPage, targetFile.getFileName());
        }
    }

    // Insert pages from source at the end of current document (simple import)
    public void insertAllFromSourceAtEnd(Path source) throws IOException {
        mergeIntoCurrent(List.of(source));
    }
}


