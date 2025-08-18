package com.securepdfeditor.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {
	@TempDir
	Path tempDir;

	@Test
	void openAndRender() throws Exception {
		Path pdf = tempDir.resolve("sample.pdf");
		createSimplePdf(pdf);

		PdfService svc = new PdfService();
		svc.open(pdf, null);
		assertTrue(svc.isOpen());
		assertEquals(1, svc.getPageCount());
		BufferedImage img = svc.renderPage(0, 1.0);
		assertNotNull(img);
		assertTrue(img.getWidth() > 0 && img.getHeight() > 0);
	}

	@Test
	void passwordProtectedOpen() throws Exception {
		Path pdf = tempDir.resolve("protected.pdf");
		createPasswordPdf(pdf, "secret");

		PdfService svc = new PdfService();
		assertThrows(IOException.class, () -> svc.open(pdf, null));
		svc.open(pdf, "secret");
		assertEquals(1, svc.getPageCount());
	}

	@Test
	void detectPasswordProtectedPdf() throws Exception {
		Path plainPdf = tempDir.resolve("plain.pdf");
		createSimplePdf(plainPdf);
		
		Path protectedPdf = tempDir.resolve("protected.pdf");
		createPasswordPdf(protectedPdf, "secret");

		PdfService svc = new PdfService();
		assertFalse(svc.isPasswordProtected(plainPdf));
		assertTrue(svc.isPasswordProtected(protectedPdf));
	}

	@Test
	void redactionOverlayPersistence() throws Exception {
		Path pdf = tempDir.resolve("sample.pdf");
		createSimplePdf(pdf);

		PdfService svc = new PdfService();
		svc.open(pdf, null);
		
		// Add redactions
		Rectangle rect1 = new Rectangle(100, 100, 50, 25);
		Rectangle rect2 = new Rectangle(200, 200, 75, 30);
		svc.addRedaction(0, rect1);
		svc.addRedaction(0, rect2);
		
		// Verify redactions are stored
		List<Rectangle> redactions = svc.getRedactions(0);
		assertEquals(2, redactions.size());
		assertTrue(redactions.contains(rect1));
		assertTrue(redactions.contains(rect2));
		
		// Verify redaction count
		assertEquals(2, svc.getRedactionCount());
		
		// Verify redactions are applied to rendered image
		BufferedImage img = svc.renderPage(0, 1.0);
		assertNotNull(img);
		
		// Clear redactions
		svc.clearRedactions(0);
		assertEquals(0, svc.getRedactions(0).size());
		assertEquals(0, svc.getRedactionCount());
	}

	@Test
	void clearAllRedactions() throws Exception {
		Path pdf = tempDir.resolve("sample.pdf");
		createSimplePdf(pdf);

		PdfService svc = new PdfService();
		svc.open(pdf, null);
		
		// Add redactions to multiple pages (if we had multiple pages)
		svc.addRedaction(0, new Rectangle(100, 100, 50, 25));
		assertEquals(1, svc.getRedactionCount());
		
		// Clear all redactions
		svc.clearAllRedactions();
		assertEquals(0, svc.getRedactionCount());
		assertTrue(svc.getRedactions(0).isEmpty());
	}

	private static void createSimplePdf(Path out) throws IOException {
		try (PDDocument doc = new PDDocument()) {
			doc.addPage(new PDPage());
			doc.save(out.toFile());
		}
	}

	private static void createPasswordPdf(Path out, String password) throws IOException {
		try (PDDocument doc = new PDDocument()) {
			doc.addPage(new PDPage());
			AccessPermission ap = new AccessPermission();
			StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, ap);
			spp.setEncryptionKeyLength(256);
			doc.protect(spp);
			doc.save(out.toFile());
		}
	}
}
