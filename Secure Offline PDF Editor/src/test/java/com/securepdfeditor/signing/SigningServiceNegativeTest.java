package com.securepdfeditor.signing;

import com.securepdfeditor.pdf.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SigningServiceNegativeTest {
    
    private SigningService signingService;
    private PdfService pdfService;
    private Path testKeystorePath;
    
    @BeforeEach
    void setUp() throws Exception {
        signingService = new SigningService();
        pdfService = new PdfService();
        
        // Create a test PDF document and save it
        PDDocument document = new PDDocument();
        document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
        Path testPdfPath = Path.of("test_document.pdf");
        document.save(testPdfPath.toFile());
        document.close();
        
        // Open the PDF through PdfService
        pdfService.open(testPdfPath, null);
        signingService.setDocument(pdfService.getDocument());
        
        // Create a test keystore
        testKeystorePath = createTestKeystore();
    }
    
    private Path createTestKeystore() throws IOException {
        // Create a simple test keystore for testing
        Path keystorePath = Path.of("test_keystore.p12");
        
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, "testpass".toCharArray());
            
            // Generate a key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            // Create a self-signed certificate (simplified)
            X509Certificate cert = createSelfSignedCertificate(keyPair);
            
            // Store in keystore
            keyStore.setKeyEntry("testkey", keyPair.getPrivate(), "testpass".toCharArray(), new X509Certificate[]{cert});
            
            // Save keystore
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(keystorePath.toFile())) {
                keyStore.store(fos, "testpass".toCharArray());
            }
            
            return keystorePath;
        } catch (Exception e) {
            throw new IOException("Failed to create test keystore", e);
        }
    }
    
    private X509Certificate createSelfSignedCertificate(KeyPair keyPair) throws Exception {
        // This is a placeholder - in a real implementation, you'd create a proper certificate
        // For testing purposes, we'll just return null and handle the exception
        throw new UnsupportedOperationException("Certificate creation not implemented for tests");
    }
    
    @Test
    void testLoadKeystoreWithWrongPassword() {
        assertThrows(IOException.class, () -> {
            signingService.loadKeystore(testKeystorePath, "wrongpass");
        }, "Loading keystore with wrong password should throw exception");
    }
    
    @Test
    void testLoadKeystoreWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.loadKeystore(testKeystorePath, null);
        }, "Loading keystore with null password should throw exception");
    }
    
    @Test
    void testLoadKeystoreWithEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.loadKeystore(testKeystorePath, "");
        }, "Loading keystore with empty password should throw exception");
    }
    
    @Test
    void testLoadNonExistentKeystore() {
        Path nonExistentPath = Path.of("non_existent_keystore.p12");
        assertThrows(IOException.class, () -> {
            signingService.loadKeystore(nonExistentPath, "testpass");
        }, "Loading non-existent keystore should throw exception");
    }
    
    @Test
    void testLoadInvalidKeystoreFile() throws IOException {
        Path invalidKeystorePath = Path.of("invalid_keystore.p12");
        java.nio.file.Files.write(invalidKeystorePath, "not a keystore".getBytes());
        
        assertThrows(IOException.class, () -> {
            signingService.loadKeystore(invalidKeystorePath, "testpass");
        }, "Loading invalid keystore file should throw exception");
        
        // Clean up
        java.nio.file.Files.deleteIfExists(invalidKeystorePath);
    }
    
    @Test
    void testSignDocumentWithoutKeystore() {
        // Test signing document without loading keystore first
        assertThrows(IllegalStateException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, "test reason", "test location");
        }, "Signing without keystore should throw exception");
    }
    
    @Test
    void testSignDocumentWithNullReason() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with null reason
        signingService.loadKeystore(testKeystorePath, "testpass");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, null, "test location");
        }, "Signing with null reason should throw exception");
    }
    
    @Test
    void testSignDocumentWithEmptyReason() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with empty reason
        signingService.loadKeystore(testKeystorePath, "testpass");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, "", "test location");
        }, "Signing with empty reason should throw exception");
    }
    
    @Test
    void testSignDocumentWithNullLocation() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with null location
        signingService.loadKeystore(testKeystorePath, "testpass");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, "test reason", null);
        }, "Signing with null location should throw exception");
    }
    
    @Test
    void testSignDocumentWithEmptyLocation() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with empty location
        signingService.loadKeystore(testKeystorePath, "testpass");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, "test reason", "");
        }, "Signing with empty location should throw exception");
    }
    
    @Test
    void testSignDocumentWithVeryLongReason() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with very long reason
        signingService.loadKeystore(testKeystorePath, "testpass");
        String longReason = "A".repeat(10000);
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, longReason, "test location");
        }, "Signing with very long reason should throw exception");
    }
    
    @Test
    void testSignDocumentWithVeryLongLocation() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with very long location
        signingService.loadKeystore(testKeystorePath, "testpass");
        String longLocation = "A".repeat(10000);
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, "test reason", longLocation);
        }, "Signing with very long location should throw exception");
    }
    
    @Test
    void testSignDocumentWithSpecialCharacters() throws IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException, java.security.cert.CertificateException {
        // Test signing with special characters in reason/location
        signingService.loadKeystore(testKeystorePath, "testpass");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.signDocument(0, 100, 100, 100, 50, "test@#$%^&*()", "location@#$%^&*()");
        }, "Signing with special characters should throw exception");
    }
    
    @Test
    void testVerifySignaturesOnUnsignedDocument() {
        // Test signature verification on unsigned document
        boolean result = signingService.verifySignatures();
        assertTrue(result, "Unsigned document should return true for verification");
    }
    
    @Test
    void testVerifySignaturesOnCorruptedDocument() throws IOException {
        // Test signature verification on corrupted document
        // Create a corrupted PDF by writing invalid content
        Path corruptedPath = Path.of("corrupted_document.pdf");
        java.nio.file.Files.write(corruptedPath, "invalid pdf content".getBytes());
        
        pdfService.open(corruptedPath, null);
        
        assertThrows(IllegalStateException.class, () -> {
            signingService.verifySignatures();
        }, "Verifying signatures on corrupted document should throw exception");
        
        // Clean up
        java.nio.file.Files.deleteIfExists(corruptedPath);
    }
    
    @Test
    void testAddDrawnSignatureWithNullImage() {
        // Test adding drawn signature with null image
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addDrawnSignature(0, null, 100, 100, 100, 50);
        }, "Adding null drawn signature should throw exception");
    }
    
    @Test
    void testAddDrawnSignatureWithInvalidCoordinates() {
        // Test adding drawn signature with invalid coordinates
        java.awt.image.BufferedImage testImage = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addDrawnSignature(0, testImage, -100, 100, 100, 50);
        }, "Adding signature with negative X coordinate should throw exception");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addDrawnSignature(0, testImage, 100, -100, 100, 50);
        }, "Adding signature with negative Y coordinate should throw exception");
    }
    
    @Test
    void testAddImageSignatureWithNullPath() {
        // Test adding image signature with null path
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addImageSignature(0, null, 100, 100, 100, 50);
        }, "Adding image signature with null path should throw exception");
    }
    
    @Test
    void testAddImageSignatureWithNonExistentFile() {
        // Test adding image signature with non-existent file
        Path nonExistentPath = Path.of("non_existent_image.png");
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addImageSignature(0, nonExistentPath, 100, 100, 100, 50);
        }, "Adding image signature with non-existent file should throw exception");
    }
    
    @Test
    void testAddImageSignatureWithInvalidFile() throws IOException {
        // Test adding image signature with invalid image file
        Path invalidImagePath = Path.of("invalid_image.png");
        java.nio.file.Files.write(invalidImagePath, "not an image".getBytes());
        
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addImageSignature(0, invalidImagePath, 100, 100, 100, 50);
        }, "Adding image signature with invalid file should throw exception");
        
        // Clean up
        java.nio.file.Files.deleteIfExists(invalidImagePath);
    }
    
    @Test
    void testClearKeystoreWhenNotLoaded() {
        // Test clearing keystore when none is loaded
        assertDoesNotThrow(() -> {
            signingService.clearKeystore();
        }, "Clearing keystore when none loaded should not throw exception");
    }
    
    @Test
    void testGetSignatureCountOnUnsignedDocument() {
        // Test getting signature count on unsigned document
        int count = signingService.getSignatureCount();
        assertEquals(0, count, "Unsigned document should have 0 signatures");
    }
    
    @Test
    void testGetSignatureCountOnCorruptedDocument() throws IOException {
        // Test getting signature count on corrupted document
        Path corruptedPath = Path.of("corrupted_document.pdf");
        java.nio.file.Files.write(corruptedPath, "invalid pdf content".getBytes());
        
        pdfService.open(corruptedPath, null);
        
        assertThrows(IllegalStateException.class, () -> {
            signingService.getSignatureCount();
        }, "Getting signature count on corrupted document should throw exception");
        
        // Clean up
        java.nio.file.Files.deleteIfExists(corruptedPath);
    }
}
