package com.securepdfeditor.packaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class PackagingServiceTest {
    
    private PackagingService packagingService;
    
    @BeforeEach
    void setUp() {
        packagingService = PackagingService.getInstance();
    }
    
    @Test
    void testSingletonInstance() {
        PackagingService instance1 = PackagingService.getInstance();
        PackagingService instance2 = PackagingService.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Disabled("Invokes Gradle jlink/jpackage; skip in unit tests environment")
    @Test
    void testBuildPackageSuccess(@TempDir Path tempDir) throws ExecutionException, InterruptedException {
        PackagingService.PackagingOptions options = new PackagingService.PackagingOptions();
        options.setOutputDirectory(tempDir.toString());
        options.setAppName("Test App");
        options.setVersion("1.0.0");
        options.setDescription("Test description");
        options.setVendor("Test Vendor");
        options.setCopyright("Test Copyright");
        
        CompletableFuture<PackagingService.PackagingResult> future = packagingService.buildPackage(options);
        PackagingService.PackagingResult result = future.get();
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getPackageFile());
        assertNotNull(result.getPackageInfo());
        assertNull(result.getErrorMessage());
        
        // Verify package file exists
        assertTrue(java.nio.file.Files.exists(result.getPackageFile()));
    }
    
    @Disabled("Invokes Gradle jlink/jpackage; skip in unit tests environment")
    @Test
    void testBuildPackageWithInvalidOptions() throws ExecutionException, InterruptedException {
        PackagingService.PackagingOptions options = new PackagingService.PackagingOptions();
        // Don't set required fields
        
        CompletableFuture<PackagingService.PackagingResult> future = packagingService.buildPackage(options);
        PackagingService.PackagingResult result = future.get();
        
        assertFalse(result.isSuccess());
        assertNull(result.getPackageFile());
        assertNull(result.getPackageInfo());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Output directory"));
    }
    
    @Test
    void testCheckForUpdates() throws ExecutionException, InterruptedException {
        CompletableFuture<PackagingService.UpdateCheckResult> future = packagingService.checkForUpdates();
        PackagingService.UpdateCheckResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getCurrentVersion());
        assertNotNull(result.getLatestVersion());
        assertNotNull(result.getMessage());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testRunQualityChecks() throws ExecutionException, InterruptedException {
        CompletableFuture<PackagingService.QualityCheckResult> future = packagingService.runQualityChecks();
        PackagingService.QualityCheckResult result = future.get();
        
        assertNotNull(result);
        assertTrue(result.isPassed()); // Should pass with no issues in test
        assertNotNull(result.getIssues());
        assertNull(result.getErrorMessage());
    }
    
    @Disabled("Invokes Gradle jlink/jpackage; skip in unit tests environment")
    @Test
    void testBuildAllPackages(@TempDir Path tempDir) throws ExecutionException, InterruptedException {
        PackagingService.PackagingOptions options = new PackagingService.PackagingOptions();
        options.setOutputDirectory(tempDir.toString());
        options.setAppName("Test App");
        options.setVersion("1.0.0");
        
        CompletableFuture<PackagingService.PackagingResult> future = packagingService.buildAllPackages(options);
        PackagingService.PackagingResult result = future.get();
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getPackageFile());
        assertNotNull(result.getPackageInfo());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testPackagingOptions() {
        PackagingService.PackagingOptions options = new PackagingService.PackagingOptions();
        
        options.setOutputDirectory("/test/output");
        options.setAppName("Test App");
        options.setVersion("1.0.0");
        options.setDescription("Test description");
        options.setVendor("Test Vendor");
        options.setCopyright("Test Copyright");
        
        assertEquals("/test/output", options.getOutputDirectory());
        assertEquals("Test App", options.getAppName());
        assertEquals("1.0.0", options.getVersion());
        assertEquals("Test description", options.getDescription());
        assertEquals("Test Vendor", options.getVendor());
        assertEquals("Test Copyright", options.getCopyright());
        assertNotNull(options.getJavaOptions());
    }
    
    @Test
    void testQualityIssue() {
        PackagingService.QualityIssue issue = new PackagingService.QualityIssue(
            "Test Type", "Test Description", "CRITICAL", "Test Location"
        );
        
        assertEquals("Test Type", issue.getType());
        assertEquals("Test Description", issue.getDescription());
        assertEquals("CRITICAL", issue.getSeverity());
        assertEquals("Test Location", issue.getLocation());
        assertTrue(issue.isCritical());
        assertTrue(issue.isHigh());
        
        PackagingService.QualityIssue lowIssue = new PackagingService.QualityIssue(
            "Test Type", "Test Description", "LOW", "Test Location"
        );
        assertFalse(lowIssue.isCritical());
        assertFalse(lowIssue.isHigh());
    }
    
    @Test
    void testPackageInfo() {
        PackagingService.PackageInfo info = new PackagingService.PackageInfo(
            "Test Platform", 1, java.util.List.of(Path.of("test.pkg")), "1.0.0", "Test App", 1024L
        );
        
        assertEquals("Test Platform", info.getPlatform());
        assertEquals(1, info.getPackageCount());
        assertEquals(1, info.getPackages().size());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("Test App", info.getAppName());
        assertEquals(1024L, info.getTotalSize());
    }
}
