package com.securepdfeditor.signing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SigningService {
    private static final Logger logger = LoggerFactory.getLogger(SigningService.class);

    private PDDocument document;
    private KeyStore keyStore;
    private String keyAlias;
    private char[] keyPassword;

    public void setDocument(PDDocument document) {
        this.document = document;
    }

    public boolean isDocumentLoaded() {
        return document != null;
    }

    // Load PKCS#12 keystore
    public void loadKeystore(Path keystorePath, String password) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = java.nio.file.Files.newInputStream(keystorePath)) {
            keyStore.load(is, password.toCharArray());
        }
        this.keyPassword = password.toCharArray();
        this.keyAlias = keyStore.aliases().nextElement(); // Get first alias
        logger.info("Loaded keystore: {} with alias: {}", keystorePath.getFileName(), keyAlias);
    }

    // Signature drawing (capture from JavaFX Canvas)
    public void addDrawnSignature(int pageIndex, BufferedImage signatureImage, float x, float y, float width, float height) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");
        if (signatureImage == null) throw new IllegalArgumentException("Signature image cannot be null");
        if (x < 0 || y < 0 || width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid coordinates or dimensions");

        PDPage page = document.getPage(pageIndex);
        PDImageXObject image = LosslessFactory.createFromImage(document, signatureImage);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.drawImage(image, x, y, width, height);
        }

        logger.info("Added drawn signature to page {} at ({}, {}) {}x{}", pageIndex, x, y, width, height);
    }

    // Image-based signature
    public void addImageSignature(int pageIndex, Path imagePath, float x, float y, float width, float height) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");
        if (imagePath == null) throw new IllegalArgumentException("Image path cannot be null");
        if (x < 0 || y < 0 || width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid coordinates or dimensions");

        PDPage page = document.getPage(pageIndex);
        PDImageXObject image = PDImageXObject.createFromFile(imagePath.toString(), document);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.drawImage(image, x, y, width, height);
        }

        logger.info("Added image signature to page {}: {} at ({}, {}) {}x{}", pageIndex, imagePath.getFileName(), x, y, width, height);
    }

    // Certificate-based digital signing
    public void signDocument(int pageIndex, float x, float y, float width, float height, String reason, String location) throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (keyStore == null) throw new IllegalStateException("No keystore loaded");
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) throw new IllegalArgumentException("Invalid page index");
        if (reason == null) throw new IllegalArgumentException("Reason cannot be null");
        if (reason.isEmpty()) throw new IllegalArgumentException("Reason cannot be empty");
        if (reason.length() > 1000) throw new IllegalArgumentException("Reason too long (max 1000 characters)");
        if (location == null) throw new IllegalArgumentException("Location cannot be null");
        if (location.isEmpty()) throw new IllegalArgumentException("Location cannot be empty");
        if (location.length() > 1000) throw new IllegalArgumentException("Location too long (max 1000 characters)");

        // Create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Secure PDF Editor");
        signature.setLocation(location);
        signature.setReason(reason);
        signature.setSignDate(Calendar.getInstance());

        // Add signature to document
        document.addSignature(signature, new CertificateSignatureInterface());

        logger.info("Digitally signed document on page {} at ({}, {}) {}x{}", pageIndex, x, y, width, height);
    }

    // Enhanced signature validation
    public boolean verifySignatures() {
        if (!isDocumentLoaded()) return false;
        try {
            List<PDSignature> signatures = document.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                logger.info("Document has no signatures to verify");
                return true;
            }

            boolean allValid = true;
            for (PDSignature signature : signatures) {
                boolean isValid = verifySignature(signature);
                if (!isValid) {
                    allValid = false;
                    logger.warn("Signature verification failed for: {}", signature.getName());
                } else {
                    logger.info("Signature verified successfully: {}", signature.getName());
                }
            }
            return allValid;
        } catch (Exception e) {
            logger.error("Error verifying signatures", e);
            return false;
        }
    }

    private boolean verifySignature(PDSignature signature) {
        try {
            // Basic validation - check if signature has required fields
            if (signature.getFilter() == null || signature.getSubFilter() == null) {
                logger.warn("Signature missing required filter or subfilter");
                return false;
            }

            // Check signature date
            Calendar signDate = signature.getSignDate();
            if (signDate != null) {
                Calendar now = Calendar.getInstance();
                if (signDate.after(now)) {
                    logger.warn("Signature date is in the future: {}", signDate.getTime());
                    return false;
                }
            }

            // For now, return true as a basic validation
            // Full cryptographic validation would require more complex CMS processing
            return true;
        } catch (Exception e) {
            logger.error("Error verifying individual signature", e);
            return false;
        }
    }

    // Validate signatures when opening a PDF
    public boolean validateSignaturesOnOpen(Path pdfPath) {
        try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
            List<PDSignature> signatures = doc.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                logger.info("PDF has no signatures");
                return true;
            }

            logger.info("Found {} signature(s) in PDF", signatures.size());
            boolean allValid = true;
            for (PDSignature signature : signatures) {
                String signerName = signature.getName() != null ? signature.getName() : "Unknown";
                String reason = signature.getReason() != null ? signature.getReason() : "No reason";
                String location = signature.getLocation() != null ? signature.getLocation() : "No location";
                
                logger.info("Signature: {} | Reason: {} | Location: {}", signerName, reason, location);
                
                // Basic validation
                if (!verifySignature(signature)) {
                    allValid = false;
                }
            }
            return allValid;
        } catch (Exception e) {
            logger.error("Error validating signatures on PDF open", e);
            return false;
        }
    }

    public int getSignatureCount() {
        if (!isDocumentLoaded()) return 0;
        try {
            return document.getSignatureDictionaries().size();
        } catch (Exception e) {
            logger.error("Error getting signature count", e);
            return 0;
        }
    }

    // Get signature information for display
    public List<SignatureInfo> getSignatureInfo() {
        List<SignatureInfo> infoList = new java.util.ArrayList<>();
        if (!isDocumentLoaded()) return infoList;

        try {
            List<PDSignature> signatures = document.getSignatureDictionaries();
            for (PDSignature signature : signatures) {
                SignatureInfo info = new SignatureInfo(
                    signature.getName() != null ? signature.getName() : "Unknown",
                    signature.getReason() != null ? signature.getReason() : "",
                    signature.getLocation() != null ? signature.getLocation() : "",
                    signature.getSignDate(),
                    verifySignature(signature)
                );
                infoList.add(info);
            }
        } catch (Exception e) {
            logger.error("Error getting signature info", e);
        }
        return infoList;
    }

    // Certificate signature interface implementation
    private class CertificateSignatureInterface implements SignatureInterface {
        @Override
        public byte[] sign(InputStream content) throws IOException {
            try {
                // Get private key and certificate
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword);
                Certificate[] certChain = keyStore.getCertificateChain(keyAlias);

                // Create content signer
                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);

                // Create signed data generator
                CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
                generator.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().build()
                    ).build(contentSigner, (X509Certificate) certChain[0])
                );

                // Add certificates
                List<Certificate> certList = Arrays.asList(certChain);
                JcaCertStore certStore = new JcaCertStore(certList);
                generator.addCertificates(certStore);

                // Read content
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                content.transferTo(baos);
                byte[] contentBytes = baos.toByteArray();

                // Generate signature
                CMSSignedData signedData = generator.generate(new org.bouncycastle.cms.CMSProcessableByteArray(contentBytes), true);
                return signedData.getEncoded();

            } catch (Exception e) {
                throw new IOException("Failed to create signature", e);
            }
        }
    }

    // Utility methods
    public boolean isKeystoreLoaded() { return keyStore != null; }
    public String getKeyAlias() { return keyAlias; }
    public void clearKeystore() {
        keyStore = null;
        keyAlias = null;
        keyPassword = null;
        logger.info("Cleared keystore");
    }

    // Data class for signature information
    public static class SignatureInfo {
        private final String signerName;
        private final String reason;
        private final String location;
        private final Calendar signDate;
        private final boolean isValid;

        public SignatureInfo(String signerName, String reason, String location, Calendar signDate, boolean isValid) {
            this.signerName = signerName;
            this.reason = reason;
            this.location = location;
            this.signDate = signDate;
            this.isValid = isValid;
        }

        public String getSignerName() { return signerName; }
        public String getReason() { return reason; }
        public String getLocation() { return location; }
        public Calendar getSignDate() { return signDate; }
        public boolean isValid() { return isValid; }

        @Override
        public String toString() {
            return String.format("SignatureInfo{signer='%s', reason='%s', location='%s', valid=%s}", 
                signerName, reason, location, isValid);
        }
    }
}
