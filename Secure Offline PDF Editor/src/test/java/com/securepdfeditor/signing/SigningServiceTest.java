package com.securepdfeditor.signing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SigningServiceTest {
    @TempDir Path tempDir;
    private SigningService signingService;
    private PDDocument document;
    private Path testPdfPath;

    @BeforeEach
    void setUp() throws IOException {
        signingService = new SigningService();

        // Create a test PDF
        document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        testPdfPath = tempDir.resolve("test.pdf");
        document.save(testPdfPath.toFile());
        document.close();

        // Reopen for signing
        document = Loader.loadPDF(testPdfPath.toFile());
        signingService.setDocument(document);

        // Ensure BC provider is registered
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private Path createPkcs12WithKeypair(String alias, char[] password) throws Exception {
        // Generate RSA key pair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // Build self-signed cert (valid for 1 day)
        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 60_000);
        Date notAfter = new Date(now + 86_400_000);
        X500Name subject = new X500Name("CN=Test");
        BigInteger serial = new BigInteger(64, new SecureRandom());
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, kp.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(kp.getPrivate());
        X509CertificateHolder holder = certBuilder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        cert.checkValidity(new Date());
        cert.verify(kp.getPublic());

        // Store into PKCS12
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, password);
        ks.setKeyEntry(alias, kp.getPrivate(), password, new Certificate[]{cert});
        Path keystorePath = tempDir.resolve("test.p12");
        try (var outputStream = Files.newOutputStream(keystorePath)) {
            ks.store(outputStream, password);
        }
        return keystorePath;
    }

    @Test
    void testAddDrawnSignature() throws IOException {
        BufferedImage signatureImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        signingService.addDrawnSignature(0, signatureImage, 100, 100, 200, 100);
        assertTrue(signingService.isDocumentLoaded());
    }

    @Test
    void testAddImageSignature() throws IOException {
        BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Path imagePath = tempDir.resolve("signature.png");
        javax.imageio.ImageIO.write(testImage, "PNG", imagePath.toFile());
        signingService.addImageSignature(0, imagePath, 150, 150, 100, 50);
        assertTrue(signingService.isDocumentLoaded());
    }

    @Test
    void testLoadKeystore() throws Exception {
        Path keystorePath = createPkcs12WithKeypair("alias1", "password".toCharArray());
        signingService.loadKeystore(keystorePath, "password");
        assertTrue(signingService.isKeystoreLoaded());
        assertNotNull(signingService.getKeyAlias());
    }

    @Test
    void testSignDocumentWithoutKeystore() {
        assertThrows(IllegalStateException.class, () -> {
            signingService.signDocument(0, 100, 100, 200, 100, "Test reason", "Test location");
        });
    }

    @Test
    void testVerifySignatures() {
        assertTrue(signingService.verifySignatures());
        assertEquals(0, signingService.getSignatureCount());
    }

    @Test
    void testInvalidPageIndex() {
        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addDrawnSignature(-1, new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), 100, 100, 100, 100);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            signingService.addDrawnSignature(10, new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), 100, 100, 100, 100);
        });
    }

    @Test
    void testNoDocumentLoaded() {
        signingService.setDocument(null);

        assertThrows(IllegalStateException.class, () -> {
            signingService.addDrawnSignature(0, new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), 100, 100, 100, 100);
        });

        assertThrows(IllegalStateException.class, () -> {
            signingService.addImageSignature(0, tempDir.resolve("test.png"), 100, 100, 100, 100);
        });
    }

    @Test
    void testClearKeystore() throws Exception {
        Path keystorePath = createPkcs12WithKeypair("alias2", "password".toCharArray());
        signingService.loadKeystore(keystorePath, "password");
        assertTrue(signingService.isKeystoreLoaded());
        signingService.clearKeystore();
        assertFalse(signingService.isKeystoreLoaded());
        assertNull(signingService.getKeyAlias());
    }

    @Test
    void testSignatureCount() {
        assertEquals(0, signingService.getSignatureCount());
    }

    @Test
    void testDocumentState() {
        assertTrue(signingService.isDocumentLoaded());
        signingService.setDocument(null);
        assertFalse(signingService.isDocumentLoaded());
        signingService.setDocument(document);
        assertTrue(signingService.isDocumentLoaded());
    }
}
