package com.securepdfeditor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SecurityService to verify anti-malware and integrity features.
 */
class SecurityServiceTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create test directories
        Files.createDirectories(tempDir.resolve("logs"));
        Files.createDirectories(tempDir.resolve("quarantine"));
        Files.createDirectories(tempDir.resolve("temp"));
    }

    @Test
    void testCalculateFileHash() throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        String content = "This is a test file for security verification";
        Files.write(testFile, content.getBytes());

        // Calculate hash
        String hash = SecurityService.calculateFileHash(testFile);

        // Verify hash is not null and has correct length (SHA-256 = 64 hex chars)
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[a-f0-9]+"));

        // Verify same content produces same hash
        String hash2 = SecurityService.calculateFileHash(testFile);
        assertEquals(hash, hash2);
    }

    @Test
    void testRegisterFileForMonitoring() throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("monitor.txt");
        String content = "File to be monitored";
        Files.write(testFile, content.getBytes());

        // Register file for monitoring
        SecurityService.registerFileForMonitoring(testFile.toString());

        // Verify file integrity
        assertTrue(SecurityService.verifyFileIntegrity(testFile.toString()));
    }

    @Test
    void testFileIntegrityViolation() throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("integrity.txt");
        String content = "Original content";
        Files.write(testFile, content.getBytes());

        // Register file for monitoring
        SecurityService.registerFileForMonitoring(testFile.toString());

        // Verify initial integrity
        assertTrue(SecurityService.verifyFileIntegrity(testFile.toString()));

        // Modify the file
        String modifiedContent = "Modified content";
        Files.write(testFile, modifiedContent.getBytes());

        // Verify integrity violation is detected
        assertFalse(SecurityService.verifyFileIntegrity(testFile.toString()));
    }

    @Test
    void testMalwareScanning() throws IOException {
        // Create a clean test file
        Path cleanFile = tempDir.resolve("clean.txt");
        String cleanContent = "This is a clean text file";
        Files.write(cleanFile, cleanContent.getBytes());

        // Test clean file
        assertFalse(SecurityService.scanFileForMalware(cleanFile.toString()));

        // Create a file with suspicious content (simulated executable header)
        Path suspiciousFile = tempDir.resolve("suspicious.bin");
        byte[] suspiciousContent = {
            (byte) 0x4D, (byte) 0x5A, (byte) 0x90, (byte) 0x00, // MZ header (executable signature)
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        Files.write(suspiciousFile, suspiciousContent);

        // Test suspicious file
        assertTrue(SecurityService.scanFileForMalware(suspiciousFile.toString()));
    }

    @Test
    void testHighEntropyDetection() throws IOException {
        // Create a file with high entropy (random data)
        Path highEntropyFile = tempDir.resolve("high_entropy.bin");
        byte[] randomData = new byte[1000];
        new java.security.SecureRandom().nextBytes(randomData);
        Files.write(highEntropyFile, randomData);

        // Test high entropy file
        assertTrue(SecurityService.scanFileForMalware(highEntropyFile.toString()));
    }

    @Test
    void testSecurityStatus() {
        // Get security status
        Map<String, Object> status = SecurityService.getSecurityStatus();

        // Verify status contains expected fields
        assertNotNull(status);
        assertTrue(status.containsKey("securityEnabled"));
        assertTrue(status.containsKey("realTimeProtection"));
        assertTrue(status.containsKey("heuristicScanning"));
        assertTrue(status.containsKey("behaviorAnalysis"));
        assertTrue(status.containsKey("monitoredFiles"));
        assertTrue(status.containsKey("lastScan"));

        // Verify default values
        assertTrue((Boolean) status.get("securityEnabled"));
        assertTrue((Boolean) status.get("realTimeProtection"));
        assertTrue((Boolean) status.get("heuristicScanning"));
        assertTrue((Boolean) status.get("behaviorAnalysis"));
    }

    @Test
    void testSecurityStateChanges() {
        // Test enabling/disabling security
        SecurityService.setSecurityEnabled(false);
        Map<String, Object> status = SecurityService.getSecurityStatus();
        assertFalse((Boolean) status.get("securityEnabled"));

        // Re-enable security
        SecurityService.setSecurityEnabled(true);
        status = SecurityService.getSecurityStatus();
        assertTrue((Boolean) status.get("securityEnabled"));

        // Test real-time protection toggle
        SecurityService.setRealTimeProtection(false);
        status = SecurityService.getSecurityStatus();
        assertFalse((Boolean) status.get("realTimeProtection"));

        // Re-enable real-time protection
        SecurityService.setRealTimeProtection(true);
        status = SecurityService.getSecurityStatus();
        assertTrue((Boolean) status.get("realTimeProtection"));
    }

    @Test
    void testLogSecurityEvent() {
        // Test security event logging
        String eventType = "TEST_EVENT";
        String message = "Test security event message";

        // This should not throw an exception
        assertDoesNotThrow(() -> {
            SecurityService.logSecurityEvent(eventType, message);
        });
    }

    @Test
    void testFileQuarantine() throws IOException {
        // Create a test file to quarantine
        Path testFile = tempDir.resolve("quarantine_test.txt");
        String content = "File to be quarantined";
        Files.write(testFile, content.getBytes());

        // Verify file exists
        assertTrue(Files.exists(testFile));

        // Simulate quarantine (this would normally be called by the security service)
        Path quarantineDir = tempDir.resolve("quarantine");
        Path quarantineFile = quarantineDir.resolve(testFile.getFileName().toString() + ".quarantine");

        // Move file to quarantine
        Files.move(testFile, quarantineFile);

        // Verify original file is gone
        assertFalse(Files.exists(testFile));

        // Verify file is in quarantine
        assertTrue(Files.exists(quarantineFile));
    }

    @Test
    void testMalwareSignatureDetection() throws IOException {
        // Test various malware signatures
        String[] signatures = {
            "4D5A", // MZ header
            "50450000", // PE header
            "7F454C46", // ELF header
            "CAFEBABE" // Java class
        };

        for (String signature : signatures) {
            Path testFile = tempDir.resolve("signature_" + signature + ".bin");
            
            // Convert hex string to bytes
            byte[] signatureBytes = hexStringToByteArray(signature);
            Files.write(testFile, signatureBytes);

            // Test malware detection
            assertTrue(SecurityService.scanFileForMalware(testFile.toString()));
        }
    }

    @Test
    void testCleanFileDetection() throws IOException {
        // Create various clean file types
        String[] cleanContents = {
            "This is a plain text file",
            "{\"json\": \"data\"}",
            "<xml>data</xml>",
            "PDF content would go here"
        };

        for (int i = 0; i < cleanContents.length; i++) {
            Path testFile = tempDir.resolve("clean_" + i + ".txt");
            Files.write(testFile, cleanContents[i].getBytes());

            // Test that clean files are not flagged as malware
            assertFalse(SecurityService.scanFileForMalware(testFile.toString()));
        }
    }

    @Test
    void testSecurityServiceShutdown() {
        // Test that shutdown doesn't throw exceptions
        assertDoesNotThrow(() -> {
            SecurityService.shutdown();
        });
    }

    /**
     * Helper method to convert hex string to byte array.
     */
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
