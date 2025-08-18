package com.securepdfeditor.packaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackagingService {
    private static final Logger logger = LoggerFactory.getLogger(PackagingService.class);
    private static PackagingService instance;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final String osName = System.getProperty("os.name").toLowerCase();
    private final String osArch = System.getProperty("os.arch");
    
    private PackagingService() {}
    
    public static PackagingService getInstance() {
        if (instance == null) {
            instance = new PackagingService();
        }
        return instance;
    }
    
    public CompletableFuture<PackagingResult> buildPackage(PackagingOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting package build for {}", getPlatformName());

                validateOptions(options);

                Path outputDir = createOutputDirectory(options);

                // Run Gradle tasks to create runtime image and installer
                int exit = runGradleTasks("clean", "jlink", "jpackage");
                if (exit != 0) {
                    throw new IllegalStateException("Gradle packaging failed with exit code " + exit);
                }

                // Find generated installer and copy to requested output dir
                Path generated = findGeneratedPackage();
                if (generated == null) {
                    throw new IllegalStateException("Could not locate generated installer under build/jpackage");
                }

                Path dest = outputDir.resolve(generated.getFileName());
                Files.createDirectories(outputDir);
                Files.copy(generated, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                PackageInfo packageInfo = generatePackageInfo(dest, options);
                logger.info("Package build completed successfully: {}", dest);
                return new PackagingResult(true, dest, packageInfo, null);

            } catch (Exception e) {
                logger.error("Package build failed", e);
                return new PackagingResult(false, null, null, e.getMessage());
            }
        }, executor);
    }
    
    public CompletableFuture<PackagingResult> buildAllPackages(PackagingOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting multi-platform package build");
                
                List<Path> packages = new ArrayList<>();
                List<String> errors = new ArrayList<>();
                
                // Build for current platform
                PackagingResult result = buildPackage(options).get();
                if (result.isSuccess()) {
                    packages.add(result.getPackageFile());
                } else {
                    errors.add("Current platform: " + result.getErrorMessage());
                }
                
                // Note: Cross-platform building would require additional setup
                // For now, we only build for the current platform
                
                if (packages.isEmpty()) {
                    return new PackagingResult(false, null, null, 
                        "All package builds failed: " + String.join("; ", errors));
                }
                
                return new PackagingResult(true, packages.get(0), 
                    new PackageInfo("Multi-platform", packages.size(), packages), null);
                
            } catch (Exception e) {
                logger.error("Multi-platform package build failed", e);
                return new PackagingResult(false, null, null, e.getMessage());
            }
        }, executor);
    }
    
    public CompletableFuture<UpdateCheckResult> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Checking for application updates");
                
                // In a real implementation, this would check against a version server
                // For now, we'll simulate the check
                
                String currentVersion = getCurrentVersion();
                String latestVersion = getLatestVersion();
                
                boolean updateAvailable = isUpdateAvailable(currentVersion, latestVersion);
                
                return new UpdateCheckResult(
                    updateAvailable,
                    currentVersion,
                    latestVersion,
                    updateAvailable ? "New version available" : "Up to date",
                    null
                );
                
            } catch (Exception e) {
                logger.error("Update check failed", e);
                return new UpdateCheckResult(false, null, null, null, e.getMessage());
            }
        }, executor);
    }
    
    public CompletableFuture<QualityCheckResult> runQualityChecks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Running quality checks");
                
                List<QualityIssue> issues = new ArrayList<>();
                
                // Run SpotBugs analysis (simulated)
                issues.addAll(runSpotBugsAnalysis());
                
                // Run OWASP dependency check (simulated)
                issues.addAll(runDependencyCheck());
                
                // Run security scan (simulated)
                issues.addAll(runSecurityScan());
                
                boolean passed = issues.stream().noneMatch(QualityIssue::isCritical);
                
                return new QualityCheckResult(passed, issues, null);
                
            } catch (Exception e) {
                logger.error("Quality checks failed", e);
                return new QualityCheckResult(false, null, e.getMessage());
            }
        }, executor);
    }
    
    private void validateOptions(PackagingOptions options) throws IllegalArgumentException {
        if (options == null) {
            throw new IllegalArgumentException("Packaging options cannot be null");
        }
        
        if (options.getOutputDirectory() == null) {
            throw new IllegalArgumentException("Output directory must be specified");
        }
        
        if (options.getAppName() == null || options.getAppName().trim().isEmpty()) {
            throw new IllegalArgumentException("App name must be specified");
        }
    }
    
    private Path createOutputDirectory(PackagingOptions options) throws IOException {
        Path outputDir = Paths.get(options.getOutputDirectory());
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        return outputDir;
    }
    
    private Path buildPlatformPackage(PackagingOptions options, Path outputDir) throws IOException {
        // Deprecated by buildPackage invoking Gradle directly. Kept for API compatibility if needed.
        throw new UnsupportedOperationException("Use buildPackage() which invokes Gradle jlink/jpackage");
    }
    
    private PackageInfo generatePackageInfo(Path packageFile, PackagingOptions options) {
        long size = 0L;
        try {
            size = Files.size(packageFile);
        } catch (IOException ioException) {
            logger.warn("Unable to determine package size for {}", packageFile);
        }
        return new PackageInfo(
            getPlatformName(),
            1,
            List.of(packageFile),
            options.getVersion(),
            options.getAppName(),
            size
        );
    }

    private int runGradleTasks(String... tasks) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        boolean isWindows = osName.contains("windows");
        if (isWindows) {
            command.add("cmd.exe");
            command.add("/c");
            command.add(".\\gradlew.bat");
        } else {
            command.add("bash");
            command.add("-lc");
            command.add("./gradlew");
        }
        for (String t : tasks) {
            command.add(t);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.directory(new File(System.getProperty("user.dir")));
        Process process = pb.start();
        process.getInputStream().transferTo(System.out);
        return process.waitFor();
    }

    private Path findGeneratedPackage() throws IOException {
        Path jpackageDir = Paths.get("build", "jpackage");
        if (!Files.exists(jpackageDir)) {
            return null;
        }
        String expectedExt = getPackageExtension();
        try (var files = Files.list(jpackageDir)) {
            return files
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith("." + expectedExt))
                .findFirst()
                .orElse(null);
        }
    }
    
    private String getPlatformName() {
        if (osName.contains("windows")) {
            return "Windows";
        } else if (osName.contains("mac")) {
            return "macOS";
        } else if (osName.contains("linux")) {
            return "Linux";
        } else {
            return "Unknown";
        }
    }
    
    private String getPackageExtension() {
        if (osName.contains("windows")) {
            return "msi";
        } else if (osName.contains("mac")) {
            return "dmg";
        } else {
            return "deb";
        }
    }
    
    private String getCurrentVersion() {
        return "1.0.0";
    }
    
    private String getLatestVersion() {
        // Simulate version check
        return "1.0.1";
    }
    
    private boolean isUpdateAvailable(String current, String latest) {
        return !current.equals(latest);
    }
    
    private List<QualityIssue> runSpotBugsAnalysis() {
        List<QualityIssue> issues = new ArrayList<>();
        // Simulate SpotBugs analysis
        // In real implementation, this would run SpotBugs and parse results
        return issues;
    }
    
    private List<QualityIssue> runDependencyCheck() {
        List<QualityIssue> issues = new ArrayList<>();
        // Simulate OWASP dependency check
        // In real implementation, this would run the dependency check
        return issues;
    }
    
    private List<QualityIssue> runSecurityScan() {
        List<QualityIssue> issues = new ArrayList<>();
        // Simulate security scan
        // In real implementation, this would run security analysis tools
        return issues;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    // Data classes
    public static class PackagingOptions {
        private String outputDirectory;
        private String appName;
        private String version;
        private String description;
        private String vendor;
        private String copyright;
        private Path icon;
        private List<String> javaOptions;
        
        // Constructors, getters, and setters
        public PackagingOptions() {
            this.javaOptions = new ArrayList<>();
        }
        
        // Getters and setters
        public String getOutputDirectory() { return outputDirectory; }
        public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
        
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }
        
        public String getCopyright() { return copyright; }
        public void setCopyright(String copyright) { this.copyright = copyright; }
        
        public Path getIcon() { return icon; }
        public void setIcon(Path icon) { this.icon = icon; }
        
        public List<String> getJavaOptions() { return javaOptions; }
        public void setJavaOptions(List<String> javaOptions) { this.javaOptions = javaOptions; }
    }
    
    public static class PackagingResult {
        private final boolean success;
        private final Path packageFile;
        private final PackageInfo packageInfo;
        private final String errorMessage;
        
        public PackagingResult(boolean success, Path packageFile, PackageInfo packageInfo, String errorMessage) {
            this.success = success;
            this.packageFile = packageFile;
            this.packageInfo = packageInfo;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public Path getPackageFile() { return packageFile; }
        public PackageInfo getPackageInfo() { return packageInfo; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class PackageInfo {
        private final String platform;
        private final int packageCount;
        private final List<Path> packages;
        private final String version;
        private final String appName;
        private final long totalSize;
        
        public PackageInfo(String platform, int packageCount, List<Path> packages) {
            this(platform, packageCount, packages, null, null, 0);
        }
        
        public PackageInfo(String platform, int packageCount, List<Path> packages, 
                          String version, String appName, long totalSize) {
            this.platform = platform;
            this.packageCount = packageCount;
            this.packages = packages;
            this.version = version;
            this.appName = appName;
            this.totalSize = totalSize;
        }
        
        public String getPlatform() { return platform; }
        public int getPackageCount() { return packageCount; }
        public List<Path> getPackages() { return packages; }
        public String getVersion() { return version; }
        public String getAppName() { return appName; }
        public long getTotalSize() { return totalSize; }
    }
    
    public static class UpdateCheckResult {
        private final boolean updateAvailable;
        private final String currentVersion;
        private final String latestVersion;
        private final String message;
        private final String errorMessage;
        
        public UpdateCheckResult(boolean updateAvailable, String currentVersion, 
                               String latestVersion, String message, String errorMessage) {
            this.updateAvailable = updateAvailable;
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.message = message;
            this.errorMessage = errorMessage;
        }
        
        public boolean isUpdateAvailable() { return updateAvailable; }
        public String getCurrentVersion() { return currentVersion; }
        public String getLatestVersion() { return latestVersion; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class QualityCheckResult {
        private final boolean passed;
        private final List<QualityIssue> issues;
        private final String errorMessage;
        
        public QualityCheckResult(boolean passed, List<QualityIssue> issues, String errorMessage) {
            this.passed = passed;
            this.issues = issues;
            this.errorMessage = errorMessage;
        }
        
        public boolean isPassed() { return passed; }
        public List<QualityIssue> getIssues() { return issues; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class QualityIssue {
        private final String type;
        private final String description;
        private final String severity;
        private final String location;
        
        public QualityIssue(String type, String description, String severity, String location) {
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.location = location;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public String getLocation() { return location; }
        
        public boolean isCritical() {
            return "CRITICAL".equalsIgnoreCase(severity);
        }
        
        public boolean isHigh() {
            return "HIGH".equalsIgnoreCase(severity);
        }
    }
}
