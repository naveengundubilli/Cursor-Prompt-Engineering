package com.securepdfeditor.creation;

import com.securepdfeditor.security.CryptoService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service for creating new PDF documents with templates and styling.
 * Supports various page sizes, templates, and text styling options.
 */
public class CreationService {
    private static final Logger logger = Logger.getLogger(CreationService.class.getName());
    
    private final CryptoService cryptoService;
    private final Map<String, TextStyle> textStyles;
    private final Map<String, Template> templates;
    
    public CreationService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
        this.textStyles = initializeDefaultStyles();
        this.templates = initializeTemplates();
    }
    
    /**
     * Page size options for PDF creation
     */
    public enum PageSize {
        A4("A4", PDRectangle.A4, 595, 842),
        LETTER("Letter", PDRectangle.LETTER, 612, 792),
        LEGAL("Legal", PDRectangle.LEGAL, 612, 1008),
        A3("A3", PDRectangle.A3, 842, 1191),
        A5("A5", PDRectangle.A5, 420, 595);
        
        private final String name;
        private final PDRectangle rectangle;
        private final float width;
        private final float height;
        
        PageSize(String name, PDRectangle rectangle, float width, float height) {
            this.name = name;
            this.rectangle = rectangle;
            this.width = width;
            this.height = height;
        }
        
        public String getName() { return name; }
        public PDRectangle getRectangle() { return rectangle; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
    }
    
    /**
     * Orientation options
     */
    public enum Orientation {
        PORTRAIT("Portrait"),
        LANDSCAPE("Landscape");
        
        private final String name;
        
        Orientation(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
    }
    
    /**
     * Create a new blank PDF document
     */
    public PDDocument createBlankDocument(PageSize pageSize, Orientation orientation, int pageCount) throws IOException {
        PDDocument document = new PDDocument();
        
        PDRectangle pageRect = pageSize.getRectangle();
        if (orientation == Orientation.LANDSCAPE) {
            pageRect = new PDRectangle(pageRect.getHeight(), pageRect.getWidth());
        }
        
        for (int i = 0; i < pageCount; i++) {
            PDPage page = new PDPage(pageRect);
            document.addPage(page);
        }
        
        logger.info("Created blank PDF with " + pageCount + " pages, size: " + pageSize.getName() + 
                   ", orientation: " + orientation.getName());
        return document;
    }
    
    /**
     * Create a new PDF from a template
     */
    public PDDocument createFromTemplate(String templateName, int pageCount) throws IOException {
        Template template = templates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        PDDocument document = new PDDocument();
        
        for (int i = 0; i < pageCount; i++) {
            PDPage page = template.createPage();
            document.addPage(page);
        }
        
        logger.info("Created PDF from template '" + templateName + "' with " + pageCount + " pages");
        return document;
    }
    
    /**
     * Save document (encryption not implemented in this version)
     */
    public void saveDocument(PDDocument document, Path filePath, String password) throws IOException {
        // Note: PDF encryption not implemented in this version
        document.save(filePath.toFile());
        logger.info("Saved PDF to: " + filePath);
    }
    
    /**
     * Get available page sizes
     */
    public List<PageSize> getAvailablePageSizes() {
        return List.of(PageSize.values());
    }
    
    /**
     * Get available templates
     */
    public List<String> getAvailableTemplates() {
        return new ArrayList<>(templates.keySet());
    }
    
    /**
     * Get available text styles
     */
    public List<String> getAvailableTextStyles() {
        return new ArrayList<>(textStyles.keySet());
    }
    
    /**
     * Add a custom text style
     */
    public void addTextStyle(String name, TextStyle style) {
        textStyles.put(name, style);
        logger.info("Added custom text style: " + name);
    }
    
    /**
     * Get a text style by name
     */
    public TextStyle getTextStyle(String name) {
        return textStyles.get(name);
    }
    
    /**
     * Initialize default text styles
     */
    private Map<String, TextStyle> initializeDefaultStyles() {
        Map<String, TextStyle> styles = new HashMap<>();
        
        styles.put("Heading 1", new TextStyle(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24, Color.BLACK, TextAlignment.LEFT));
        styles.put("Heading 2", new TextStyle(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18, Color.BLACK, TextAlignment.LEFT));
        styles.put("Heading 3", new TextStyle(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14, Color.BLACK, TextAlignment.LEFT));
        styles.put("Body", new TextStyle(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12, Color.BLACK, TextAlignment.LEFT));
        styles.put("Caption", new TextStyle(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10, Color.GRAY, TextAlignment.CENTER));
        styles.put("Title", new TextStyle(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36, Color.BLACK, TextAlignment.CENTER));
        
        return styles;
    }
    
    /**
     * Initialize built-in templates
     */
    private Map<String, Template> initializeTemplates() {
        Map<String, Template> templateMap = new HashMap<>();
        
        templateMap.put("Blank", new BlankTemplate());
        templateMap.put("Lined Page", new LinedPageTemplate());
        templateMap.put("Grid Page", new GridPageTemplate());
        templateMap.put("Title Page", new TitlePageTemplate());
        templateMap.put("Meeting Notes", new MeetingNotesTemplate());
        
        return templateMap;
    }
    
    /**
     * Text alignment options
     */
    public enum TextAlignment {
        LEFT, CENTER, RIGHT, JUSTIFY
    }
}
