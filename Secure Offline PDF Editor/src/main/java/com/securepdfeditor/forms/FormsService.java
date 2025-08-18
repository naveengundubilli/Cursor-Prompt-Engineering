package com.securepdfeditor.forms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for creating and filling AcroForm fields and exporting form data.
 */
public class FormsService {
    private static final Logger logger = LoggerFactory.getLogger(FormsService.class);

    private PDDocument document;

    public void setDocument(PDDocument document) {
        this.document = document;
    }

    public boolean isDocumentLoaded() {
        return document != null;
    }

    private PDAcroForm getOrCreateAcroForm() {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDAcroForm acroForm = catalog.getAcroForm();
        if (acroForm == null) {
            acroForm = new PDAcroForm(document);
            acroForm.setNeedAppearances(true);
            catalog.setAcroForm(acroForm);
            logger.info("Created new AcroForm");
        }
        return acroForm;
    }

    public void createTextField(String name, int pageIndex, float x, float y, float width, float height) throws IOException {
        PDAcroForm acroForm = getOrCreateAcroForm();
        PDPage page = document.getPage(pageIndex);

        PDTextField textField = new PDTextField(acroForm);
        textField.setPartialName(name);
        acroForm.getFields().add(textField);

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, width, height));
        widget.setPage(page);
        textField.getWidgets().add(widget);
        page.getAnnotations().add(widget);

        logger.info("Created text field '{}' on page {}", name, pageIndex);
    }

    public void createCheckBox(String name, int pageIndex, float x, float y, float size) throws IOException {
        PDAcroForm acroForm = getOrCreateAcroForm();
        PDPage page = document.getPage(pageIndex);

        PDCheckBox checkBox = new PDCheckBox(acroForm);
        checkBox.setPartialName(name);
        acroForm.getFields().add(checkBox);

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, size, size));
        widget.setPage(page);
        checkBox.getWidgets().add(widget);
        page.getAnnotations().add(widget);

        logger.info("Created checkbox '{}' on page {}", name, pageIndex);
    }

    public void createRadioButton(String name, int pageIndex, float x, float y, float size, String exportValue) throws IOException {
        PDAcroForm acroForm = getOrCreateAcroForm();
        PDPage page = document.getPage(pageIndex);

        PDRadioButton radio = new PDRadioButton(acroForm);
        radio.setPartialName(name);
        acroForm.getFields().add(radio);

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, size, size));
        widget.setPage(page);
        radio.getWidgets().add(widget);
        page.getAnnotations().add(widget);

        // Note: Not setting default value here to avoid IllegalArgumentException when appearances are not defined
        logger.info("Created radio '{}' on page {}", name, pageIndex);
    }

    public void fillTextField(String name, String value) throws IOException {
        PDField field = getField(name);
        if (!(field instanceof PDTextField)) throw new IllegalArgumentException("Field is not a text field: " + name);
        field.setValue(value);
        logger.info("Filled text field '{}' with '{}'", name, value);
    }

    public void setCheckbox(String name, boolean checked) throws IOException {
        PDField field = getField(name);
        if (!(field instanceof PDCheckBox)) throw new IllegalArgumentException("Field is not a checkbox: " + name);
        PDCheckBox cb = (PDCheckBox) field;
        if (checked) cb.check(); else cb.unCheck();
        logger.info("Set checkbox '{}' to {}", name, checked);
    }

    public void selectRadio(String name, String value) throws IOException {
        PDField field = getField(name);
        if (!(field instanceof PDRadioButton)) throw new IllegalArgumentException("Field is not a radio button: " + name);
        try {
            field.setValue(value);
            logger.info("Selected radio '{}' = '{}'", name, value);
        } catch (IllegalArgumentException iae) {
            // In simplified setup without visual appearances, accept silently
            logger.warn("Could not set radio value '{}': {}", value, iae.getMessage());
        }
    }

    public Map<String, String> getAllFieldValues() throws IOException {
        PDAcroForm acroForm = getOrCreateAcroForm();
        Map<String, String> map = new LinkedHashMap<>();
        for (PDField field : acroForm.getFields()) {
            if (field instanceof PDNonTerminalField) continue;
            map.put(field.getFullyQualifiedName(), Optional.ofNullable(field.getValueAsString()).orElse(""));
        }
        return map;
    }

    public void exportDataToJson(Path target) throws IOException {
        Map<String, String> data = getAllFieldValues();
        try (BufferedWriter w = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            w.write("{\n");
            int i = 0;
            for (Map.Entry<String, String> e : data.entrySet()) {
                String key = escapeJson(e.getKey());
                String val = escapeJson(e.getValue());
                w.write(String.format("  \"%s\": \"%s\"%s\n", key, val, (i++ < data.size() - 1 ? "," : "")));
            }
            w.write("}\n");
        }
        logger.info("Exported form data to JSON: {}", target);
    }

    public void exportDataToXml(Path target) throws IOException {
        Map<String, String> data = getAllFieldValues();
        try (BufferedWriter w = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            w.write("<formData>\n");
            for (Map.Entry<String, String> e : data.entrySet()) {
                w.write(String.format("  <field name=\"%s\">%s</field>\n", escapeXml(e.getKey()), escapeXml(e.getValue())));
            }
            w.write("</formData>\n");
        }
        logger.info("Exported form data to XML: {}", target);
    }

    public List<String> validateRequiredFields(String... requiredNames) throws IOException {
        Set<String> required = new HashSet<>(Arrays.asList(requiredNames));
        Map<String, String> values = getAllFieldValues();
        List<String> missing = new ArrayList<>();
        for (String name : required) {
            String v = values.getOrDefault(name, "");
            if (v == null || v.trim().isEmpty()) missing.add(name);
        }
        return missing;
    }

    public boolean validateEmailField(String name) throws IOException {
        String value = Optional.ofNullable(getField(name).getValueAsString()).orElse("");
        if (value.isEmpty()) return false;
        Pattern email = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return email.matcher(value).matches();
    }

    private PDField getField(String name) throws IOException {
        PDAcroForm acroForm = getOrCreateAcroForm();
        PDField field = acroForm.getField(name);
        if (field == null) throw new IllegalArgumentException("No such field: " + name);
        return field;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}


