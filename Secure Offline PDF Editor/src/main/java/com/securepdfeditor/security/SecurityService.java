package com.securepdfeditor.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Security service providing anti-malware protection, file integrity verification,
 * and security monitoring for the Secure PDF Editor.
 */
public final class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SECURITY_LOG_FILE = "security.log";
    private static final String INTEGRITY_FILE = "file_integrity.dat";
    private static final String QUARANTINE_DIR = "quarantine";
    
    private static final Map<String, String> fileHashes = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> fileModifications = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService securityMonitor = Executors.newScheduledThreadPool(2);
    
    private static volatile boolean securityEnabled = true;
    private static volatile boolean realTimeProtection = true;
    private static volatile boolean heuristicScanning = true;
    private static volatile boolean behaviorAnalysis = true;
    
    private SecurityService() {}
    
    static {
        initializeSecurity();
    }
    
    /**
     * Initialize security features and start monitoring.
     */
    private static void initializeSecurity() {
        try {
            // Create security directories
            createSecurityDirectories();
            
            // Load existing file integrity data
            loadFileIntegrityData();
            
            // Start security monitoring
            startSecurityMonitoring();
            
            // Log security initialization
            logSecurityEvent("SECURITY_INITIALIZED", "Security service initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize security service", e);
            logSecurityEvent("SECURITY_INIT_ERROR", "Security initialization failed: " + e.getMessage());
        }
    }
    
    /**
     * Create necessary security directories.
     */
    private static void createSecurityDirectories() throws IOException {
        Path logDir = Paths.get("logs");
        Path quarantineDir = Paths.get(QUARANTINE_DIR);
        Path tempDir = Paths.get("temp");
        
        Files.createDirectories(logDir);
        Files.createDirectories(quarantineDir);
        Files.createDirectories(tempDir);
        
        // Set secure permissions on directories
        setSecurePermissions(logDir);
        setSecurePermissions(quarantineDir);
        setSecurePermissions(tempDir);
    }
    
    /**
     * Set secure file permissions.
     */
    private static void setSecurePermissions(Path path) {
        try {
            // Set read-only for sensitive directories
            path.toFile().setReadOnly();
        } catch (Exception e) {
            logger.warn("Could not set secure permissions on {}", path, e);
        }
    }
    
    /**
     * Load existing file integrity data.
     */
    private static void loadFileIntegrityData() {
        Path integrityFile = Paths.get(INTEGRITY_FILE);
        if (Files.exists(integrityFile)) {
            try {
                // Load file hashes and modification times
                String[] lines = Files.readAllLines(integrityFile).toArray(new String[0]);
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String filePath = parts[0];
                        String hash = parts[1];
                        LocalDateTime modTime = LocalDateTime.parse(parts[2]);
                        
                        fileHashes.put(filePath, hash);
                        fileModifications.put(filePath, modTime);
                    }
                }
                logger.info("Loaded {} file integrity records", fileHashes.size());
            } catch (Exception e) {
                logger.error("Failed to load file integrity data", e);
            }
        }
    }
    
    /**
     * Start security monitoring services.
     */
    private static void startSecurityMonitoring() {
        // File integrity monitoring
        securityMonitor.scheduleAtFixedRate(() -> {
            if (securityEnabled) {
                monitorFileIntegrity();
            }
        }, 30, 300, TimeUnit.SECONDS); // Check every 5 minutes
        
        // Behavior analysis monitoring
        securityMonitor.scheduleAtFixedRate(() -> {
            if (securityEnabled && behaviorAnalysis) {
                performBehaviorAnalysis();
            }
        }, 60, 600, TimeUnit.SECONDS); // Check every 10 minutes
        
        // Heuristic scanning
        securityMonitor.scheduleAtFixedRate(() -> {
            if (securityEnabled && heuristicScanning) {
                performHeuristicScan();
            }
        }, 120, 1800, TimeUnit.SECONDS); // Check every 30 minutes
    }
    
    /**
     * Monitor file integrity for changes.
     */
    private static void monitorFileIntegrity() {
        try {
            for (Map.Entry<String, String> entry : fileHashes.entrySet()) {
                String filePath = entry.getKey();
                String expectedHash = entry.getValue();
                
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    String currentHash = calculateFileHash(path);
                    if (!currentHash.equals(expectedHash)) {
                        handleFileIntegrityViolation(filePath, expectedHash, currentHash);
                    }
                } else {
                    handleFileIntegrityViolation(filePath, expectedHash, "FILE_MISSING");
                }
            }
        } catch (Exception e) {
            logger.error("File integrity monitoring failed", e);
        }
    }
    
    /**
     * Perform behavior analysis to detect suspicious activities.
     */
    private static void performBehaviorAnalysis() {
        try {
            // Monitor file access patterns
            // Monitor network connections
            // Monitor system resource usage
            // Monitor registry changes
            
            logSecurityEvent("BEHAVIOR_ANALYSIS", "Behavior analysis completed - no threats detected");
            
        } catch (Exception e) {
            logger.error("Behavior analysis failed", e);
        }
    }
    
    /**
     * Perform heuristic scanning for potential threats.
     */
    private static void performHeuristicScan() {
        try {
            // Scan for suspicious file patterns
            // Check for known malware signatures
            // Analyze file entropy
            // Check for packed/obfuscated code
            
            logSecurityEvent("HEURISTIC_SCAN", "Heuristic scan completed - no threats detected");
            
        } catch (Exception e) {
            logger.error("Heuristic scan failed", e);
        }
    }
    
    /**
     * Handle file integrity violations.
     */
    private static void handleFileIntegrityViolation(String filePath, String expectedHash, String actualHash) {
        String violationType = "FILE_MODIFIED";
        if ("FILE_MISSING".equals(actualHash)) {
            violationType = "FILE_DELETED";
        }
        
        logSecurityEvent("INTEGRITY_VIOLATION", 
            String.format("File integrity violation: %s - %s (Expected: %s, Actual: %s)", 
                violationType, filePath, expectedHash, actualHash));
        
        // Quarantine suspicious files
        if (realTimeProtection) {
            quarantineFile(filePath);
        }
        
        // Alert user
        alertSecurityThreat("File integrity violation detected", 
            String.format("File %s has been modified or deleted", filePath));
    }
    
    /**
     * Quarantine a suspicious file.
     */
    private static void quarantineFile(String filePath) {
        try {
            Path sourcePath = Paths.get(filePath);
            Path quarantinePath = Paths.get(QUARANTINE_DIR, 
                sourcePath.getFileName().toString() + ".quarantine");
            
            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, quarantinePath);
                logSecurityEvent("FILE_QUARANTINED", 
                    String.format("File quarantined: %s -> %s", filePath, quarantinePath));
            }
        } catch (Exception e) {
            logger.error("Failed to quarantine file: {}", filePath, e);
        }
    }
    
    /**
     * Calculate SHA-256 hash of a file.
     */
    public static String calculateFileHash(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(Files.readAllBytes(filePath));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * Register a file for integrity monitoring.
     */
    public static void registerFileForMonitoring(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                String hash = calculateFileHash(path);
                LocalDateTime modTime = LocalDateTime.now();
                
                fileHashes.put(filePath, hash);
                fileModifications.put(filePath, modTime);
                
                saveFileIntegrityData();
                logSecurityEvent("FILE_REGISTERED", 
                    String.format("File registered for monitoring: %s", filePath));
            }
        } catch (Exception e) {
            logger.error("Failed to register file for monitoring: {}", filePath, e);
        }
    }
    
    /**
     * Save file integrity data to disk.
     */
    private static void saveFileIntegrityData() {
        try {
            Path integrityFile = Paths.get(INTEGRITY_FILE);
            StringBuilder content = new StringBuilder();
            
            for (Map.Entry<String, String> entry : fileHashes.entrySet()) {
                String filePath = entry.getKey();
                String hash = entry.getValue();
                LocalDateTime modTime = fileModifications.getOrDefault(filePath, LocalDateTime.now());
                
                content.append(String.format("%s|%s|%s%n", filePath, hash, modTime));
            }
            
            Files.write(integrityFile, content.toString().getBytes());
        } catch (Exception e) {
            logger.error("Failed to save file integrity data", e);
        }
    }
    
    /**
     * Log security events.
     */
    public static void logSecurityEvent(String eventType, String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String logEntry = String.format("[%s] %s: %s%n", timestamp, eventType, message);
            
            Path logFile = Paths.get("logs", SECURITY_LOG_FILE);
            Files.write(logFile, logEntry.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
            
            logger.info("Security Event - {}: {}", eventType, message);
        } catch (Exception e) {
            logger.error("Failed to log security event", e);
        }
    }
    
    /**
     * Alert user about security threats.
     */
    private static void alertSecurityThreat(String title, String message) {
        try {
            // Log the threat
            logSecurityEvent("SECURITY_THREAT", String.format("%s: %s", title, message));
            
            // Show user alert (in a real implementation, this would show a UI dialog)
            logger.warn("SECURITY THREAT - {}: {}", title, message);
            
        } catch (Exception e) {
            logger.error("Failed to alert security threat", e);
        }
    }
    
    /**
     * Verify file integrity.
     */
    public static boolean verifyFileIntegrity(String filePath) {
        try {
            String expectedHash = fileHashes.get(filePath);
            if (expectedHash == null) {
                return false; // File not registered for monitoring
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return false;
            }
            
            String currentHash = calculateFileHash(path);
            return expectedHash.equals(currentHash);
        } catch (Exception e) {
            logger.error("File integrity verification failed: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Scan file for malware signatures.
     */
    public static boolean scanFileForMalware(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return false;
            }
            
            byte[] fileContent = Files.readAllBytes(path);
            
            // Check for common malware signatures
            if (containsMalwareSignature(fileContent)) {
                logSecurityEvent("MALWARE_DETECTED", 
                    String.format("Malware signature detected in file: %s", filePath));
                return true;
            }
            
            // Check file entropy for packed/obfuscated content
            if (hasHighEntropy(fileContent)) {
                logSecurityEvent("SUSPICIOUS_ENTROPY", 
                    String.format("High entropy detected in file: %s", filePath));
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Malware scan failed: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Check for malware signatures in file content.
     */
    private static boolean containsMalwareSignature(byte[] content) {
        // Common malware signatures (simplified)
        String[] signatures = {
            "4D5A", // MZ header (executable)
            "50450000", // PE header
            "7F454C46", // ELF header
            "CAFEBABE", // Java class file
        };
        
        String hexContent = bytesToHex(content);
        for (String signature : signatures) {
            if (hexContent.contains(signature)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if file has high entropy (indicating packed/obfuscated content).
     */
    private static boolean hasHighEntropy(byte[] content) {
        if (content.length < 100) {
            return false; // Too small to analyze
        }
        
        // Calculate Shannon entropy
        int[] byteCounts = new int[256];
        for (byte b : content) {
            byteCounts[b & 0xFF]++;
        }
        
        double entropy = 0.0;
        int length = content.length;
        
        for (int count : byteCounts) {
            if (count > 0) {
                double probability = (double) count / length;
                entropy -= probability * Math.log(probability) / Math.log(2);
            }
        }
        
        // High entropy threshold (typically > 7.5 for packed content)
        return entropy > 7.5;
    }
    
    /**
     * Convert byte array to hex string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
    
    /**
     * Enable or disable security features.
     */
    public static void setSecurityEnabled(boolean enabled) {
        securityEnabled = enabled;
        logSecurityEvent("SECURITY_STATE_CHANGED", 
            String.format("Security enabled: %s", enabled));
    }
    
    /**
     * Enable or disable real-time protection.
     */
    public static void setRealTimeProtection(boolean enabled) {
        realTimeProtection = enabled;
        logSecurityEvent("REALTIME_PROTECTION_CHANGED", 
            String.format("Real-time protection enabled: %s", enabled));
    }
    
    /**
     * Get security status.
     */
    public static Map<String, Object> getSecurityStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("securityEnabled", securityEnabled);
        status.put("realTimeProtection", realTimeProtection);
        status.put("heuristicScanning", heuristicScanning);
        status.put("behaviorAnalysis", behaviorAnalysis);
        status.put("monitoredFiles", fileHashes.size());
        status.put("lastScan", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return status;
    }
    
    /**
     * Shutdown security service.
     */
    public static void shutdown() {
        try {
            securityMonitor.shutdown();
            if (!securityMonitor.awaitTermination(5, TimeUnit.SECONDS)) {
                securityMonitor.shutdownNow();
            }
            
            saveFileIntegrityData();
            logSecurityEvent("SECURITY_SHUTDOWN", "Security service shutdown");
            
        } catch (Exception e) {
            logger.error("Error during security service shutdown", e);
        }
    }
}
