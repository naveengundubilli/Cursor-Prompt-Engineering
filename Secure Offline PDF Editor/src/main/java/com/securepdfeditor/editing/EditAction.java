package com.securepdfeditor.editing;

import java.awt.Color;
import java.io.Serializable;

public class EditAction implements Serializable {
    private final EditingService.EditType type;
    private final int pageIndex;
    private final String content;
    private final float x, y, width, height;
    private final float fontSize;
    private final Color textColor;
    private Color fillColor, strokeColor;
    private float strokeWidth;
    
    public EditAction(EditingService.EditType type, int pageIndex, String content, float x, float y, float width, float height) {
        this.type = type;
        this.pageIndex = pageIndex;
        this.content = content;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fontSize = 0;
        this.textColor = null;
    }
    
    public EditAction(EditingService.EditType type, int pageIndex, String content, float x, float y, float fontSize, Color textColor) {
        this.type = type;
        this.pageIndex = pageIndex;
        this.content = content;
        this.x = x;
        this.y = y;
        this.width = 0;
        this.height = 0;
        this.fontSize = fontSize;
        this.textColor = textColor;
    }
    
    public void setColors(Color fillColor, Color strokeColor) {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
    }
    
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    // Getters
    public EditingService.EditType getType() { return type; }
    public int getPageIndex() { return pageIndex; }
    public String getContent() { return content; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getFontSize() { return fontSize; }
    public Color getTextColor() { return textColor; }
    public Color getFillColor() { return fillColor; }
    public Color getStrokeColor() { return strokeColor; }
    public float getStrokeWidth() { return strokeWidth; }
    
    @Override
    public String toString() {
        return String.format("EditAction{type=%s, page=%d, content='%s', pos=(%.1f,%.1f), size=(%.1f,%.1f)}", 
            type, pageIndex, content, x, y, width, height);
    }
}
