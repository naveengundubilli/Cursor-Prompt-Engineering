package com.securepdfeditor.forms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FormsServiceTest {
    private PDDocument doc;
    private FormsService forms;

    @BeforeEach
    void setUp() throws Exception {
        doc = new PDDocument();
        doc.addPage(new PDPage());
        forms = new FormsService();
        forms.setDocument(doc);
    }

    @AfterEach
    void tearDown() throws Exception {
        doc.close();
    }

    @Test
    void createFieldsAndExport() throws Exception {
        forms.createTextField("name", 0, 100, 700, 200, 20);
        forms.createCheckBox("agree", 0, 100, 670, 16);
        forms.createRadioButton("opt", 0, 100, 640, 16, "A");

        forms.fillTextField("name", "Alice");
        forms.setCheckbox("agree", true);
        forms.selectRadio("opt", "A");

        Path json = Files.createTempFile("form", ".json");
        forms.exportDataToJson(json);
        assertTrue(Files.size(json) > 0);

        Path xml = Files.createTempFile("form", ".xml");
        forms.exportDataToXml(xml);
        assertTrue(Files.size(xml) > 0);
    }
}


