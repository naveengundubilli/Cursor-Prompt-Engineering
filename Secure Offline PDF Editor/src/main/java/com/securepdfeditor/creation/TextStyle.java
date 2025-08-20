package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;

/**
 * Represents a text style with font, size, color, and alignment properties.
 */
public class TextStyle {
    private final PDFont font;
    private final float fontSize;
    private final Color color;
    private final CreationService.TextAlignment alignment;
    private final float lineSpacing;
    private final float spacingBefore;
    private final float spacingAfter;
    
    public TextStyle(PDFont font, float fontSize, Color color, CreationService.TextAlignment alignment) {
        this(font, fontSize, color, alignment, 1.2f, 0f, 0f);
    }
    
    public TextStyle(PDFont font, float fontSize, Color color, CreationService.TextAlignment alignment,
                    float lineSpacing, float spacingBefore, float spacingAfter) {
        this.font = font;
        this.fontSize = fontSize;
        this.color = color;
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
        this.spacingBefore = spacingBefore;
        this.spacingAfter = spacingAfter;
    }
    
    public PDFont getFont() { return font; }
    public float getFontSize() { return fontSize; }
    public Color getColor() { return color; }
    public CreationService.TextAlignment getAlignment() { return alignment; }
    public float getLineSpacing() { return lineSpacing; }
    public float getSpacingBefore() { return spacingBefore; }
    public float getSpacingAfter() { return spacingAfter; }
    
    /**
     * Create a copy of this style with modified properties
     */
    public TextStyle withFont(PDFont font) {
        return new TextStyle(font, fontSize, color, alignment, lineSpacing, spacingBefore, spacingAfter);
    }
    
    public TextStyle withFontSize(float fontSize) {
        return new TextStyle(font, fontSize, color, alignment, lineSpacing, spacingBefore, spacingAfter);
    }
    
    public TextStyle withColor(Color color) {
        return new TextStyle(font, fontSize, color, alignment, lineSpacing, spacingBefore, spacingAfter);
    }
    
    public TextStyle withAlignment(CreationService.TextAlignment alignment) {
        return new TextStyle(font, fontSize, color, alignment, lineSpacing, spacingBefore, spacingAfter);
    }
    
    public TextStyle withLineSpacing(float lineSpacing) {
        return new TextStyle(font, fontSize, color, alignment, lineSpacing, spacingBefore, spacingAfter);
    }
    
    public TextStyle withSpacing(float spacingBefore, float spacingAfter) {
        return new TextStyle(font, fontSize, color, alignment, lineSpacing, spacingBefore, spacingAfter);
    }
}

