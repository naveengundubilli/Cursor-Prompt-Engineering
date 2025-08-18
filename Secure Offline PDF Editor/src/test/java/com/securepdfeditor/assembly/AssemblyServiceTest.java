package com.securepdfeditor.assembly;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AssemblyServiceTest {
    @Test
    void splitAndMerge() throws Exception {
        Path src1 = Files.createTempFile("pdf1", ".pdf");
        Path src2 = Files.createTempFile("pdf2", ".pdf");
        try (PDDocument d1 = new PDDocument(); PDDocument d2 = new PDDocument()) {
            d1.addPage(new PDPage()); d1.addPage(new PDPage()); d1.save(src1.toFile());
            d2.addPage(new PDPage()); d2.save(src2.toFile());
        }

        try (PDDocument target = new PDDocument()) {
            AssemblyService svc = new AssemblyService();
            svc.setDocument(target);
            svc.mergeIntoCurrent(java.util.List.of(src1, src2));
            assertEquals(3, target.getNumberOfPages());

            Path out = Files.createTempFile("split", ".pdf");
            svc.splitRangeToFile(2, 3, out);
            assertTrue(Files.size(out) > 0);
        }
    }
}


