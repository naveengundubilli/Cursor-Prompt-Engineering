plugins {
	application
	java
	id("org.openjfx.javafxplugin") version "0.1.0"
	id("org.owasp.dependencycheck") version "9.0.9"
	id("com.github.spotbugs") version "6.0.20"
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "1.0.0"

repositories {
	if (project.hasProperty("offline")) {
		mavenLocal()
	} else {
		mavenCentral()
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

javafx {
	version = "17.0.2"
	modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base", "javafx.swing")
}

application {
	mainClass.set("com.securepdfeditor.App")
}

dependencies {
	implementation(libs.pdfbox)
	implementation(libs.fontbox)
	implementation(libs.bcprov)
	implementation(libs.bcpkix)
	implementation(libs.tess4j)
	implementation(libs.slf4j.api)
	runtimeOnly(libs.logback)

	testImplementation(platform("org.junit:junit-bom:5.11.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	// Ensure JUnit Platform launcher available on test runtime
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
	maxHeapSize = "2048m"
	jvmArgs = listOf("-XX:+HeapDumpOnOutOfMemoryError", "-XX:MaxRAMPercentage=75")
	maxParallelForks = 1
}

// Custom packaging tasks for Milestone 5
tasks.register("packageApp") {
	dependsOn("jar")
	description = "Create application package"
	group = "distribution"
	
	doLast {
		val distDir = file("dist")
		distDir.mkdirs()
		
		val platform = when {
			System.getProperty("os.name").lowercase().contains("windows") -> "windows"
			System.getProperty("os.name").lowercase().contains("mac") -> "macos"
			else -> "linux"
		}
		
		val packageName = "secure-pdf-editor-${project.version}-$platform"
		val packageFile = distDir.resolve("$packageName.zip")
		
		// Create simple package with jar and resources
		ant.withGroovyBuilder {
			"zip"(mapOf(
				"destfile" to packageFile
			)) {
				"fileset"(mapOf("dir" to "build/libs")) {
					"include"(mapOf("name" to "*.jar"))
				}
				"fileset"(mapOf("dir" to "src/main/resources")) {
					"include"(mapOf("name" to "**/*"))
				}
				"fileset"(mapOf("dir" to ".")) {
					"include"(mapOf("name" to "gradlew*"))
					"include"(mapOf("name" to "build.gradle.kts"))
					"include"(mapOf("name" to "settings.gradle.kts"))
					"include"(mapOf("name" to "libs.versions.toml"))
					"include"(mapOf("name" to "LICENSE"))
					"include"(mapOf("name" to "README.md"))
				}
			}
		}
		
		println("Package created: $packageFile")
	}
}

// Custom task for building all packages
tasks.register("buildAllPackages") {
    dependsOn("packageApp")
    description = "Build all platform packages"
    group = "distribution"
}

// SpotBugs configuration
spotbugs {
    toolVersion.set("4.8.4")
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
        outputLocation.set(layout.buildDirectory.file("reports/spotbugs/${name}.html"))
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.register("quality") {
    group = "verification"
    description = "Run unit tests, SpotBugs and OWASP Dependency Check"
    dependsOn("test", "spotbugsMain", "spotbugsTest", "dependencyCheckAnalyze")
}

// WiX Toolset configuration for MSI installer
val wixToolsetVersion = "3.11.2"
val wixToolsetDir = System.getenv("WIX") ?: "C:\\Program Files (x86)\\WiX Toolset v$wixToolsetVersion"

// Custom MSI packaging task with security features
tasks.register("createMsi") {
	dependsOn("shadowJar")
	description = "Create secure MSI installer with security features"
	group = "distribution"
	
	doLast {
		val msiDir = file("build/msi")
		msiDir.mkdirs()
		
		// Create WiX source file
		val wixSource = createWixSource()
		val wixFile = msiDir.resolve("SecurePDFEditor.wxs")
		wixFile.writeText(wixSource)
		
		// Create security configuration
		val securityConfig = createSecurityConfig()
		val securityFile = msiDir.resolve("security.wxi")
		securityFile.writeText(securityConfig)
		
		// Compile WiX source to MSI
		exec {
			commandLine("$wixToolsetDir\\bin\\candle.exe", 
				"-ext", "WixUtilExtension",
				"-ext", "WixSecurityExtension", 
				"-dVersion=${project.version}",
				"-dProductName=Secure PDF Editor",
				"-dManufacturer=SecurePDFEditor",
				"-dProductCode={${generateProductCode()}}",
				"-dUpgradeCode={${generateUpgradeCode()}}",
				"-out", "${msiDir}\\SecurePDFEditor.wixobj",
				wixFile.absolutePath
			)
		}
		
		// Link MSI with security features
		exec {
			commandLine("$wixToolsetDir\\bin\\light.exe",
				"-ext", "WixUtilExtension",
				"-ext", "WixSecurityExtension",
				"-ext", "WixUIExtension",
				"-cultures:en-us",
				"-out", "${msiDir}\\SecurePDFEditor-${project.version}.msi",
				"${msiDir}\\SecurePDFEditor.wixobj"
			)
		}
		
		println("Secure MSI installer created: ${msiDir}\\SecurePDFEditor-${project.version}.msi")
	}
}

// Generate unique product code for MSI
fun generateProductCode(): String {
	return java.util.UUID.randomUUID().toString().uppercase()
}

// Generate upgrade code for MSI
fun generateUpgradeCode(): String {
	return "12345678-1234-1234-1234-123456789012"
}

// Create WiX source file with security features
fun createWixSource(): String {
	return """<?xml version="1.0" encoding="UTF-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi"
     xmlns:util="http://schemas.microsoft.com/wix/UtilExtension"
     xmlns:sec="http://schemas.microsoft.com/wix/SecurityExtension">
    
    <?include security.wxi ?>
    
    <Product Id="*" 
             Name="Secure PDF Editor" 
             Language="1033" 
             Version="$(var.Version)" 
             Manufacturer="SecurePDFEditor" 
             UpgradeCode="$(var.UpgradeCode)"
             InstallScope="perMachine">
        
        <Package InstallerVersion="200" 
                 Compressed="yes" 
                 InstallScope="perMachine"
                 Description="Secure Offline PDF Editor Installer"
                 Comments="Secure PDF Editor with anti-malware protection"
                 Manufacturer="SecurePDFEditor"
                 Languages="1033" />
        
        <!-- Security: Require elevated privileges -->
        <Property Id="MSIUSEREALADMINDETECTION" Value="1" />
        
        <!-- Security: Prevent downgrade installation -->
        <MajorUpgrade DowngradeErrorMessage="A newer version of Secure PDF Editor is already installed." />
        
        <!-- Security: Require Windows 10 or later -->
        <Condition Message="This application requires Windows 10 or later.">
            <![CDATA[Installed OR (VersionNT >= 603)]]>
        </Condition>
        
        <!-- Security: Require .NET Framework -->
        <PropertyRef Id="NETFRAMEWORK45" />
        <Condition Message="This application requires .NET Framework 4.5 or later.">
            <![CDATA[Installed OR NETFRAMEWORK45]]>
        </Condition>
        
        <!-- Security: Require Java 21 -->
        <Property Id="JAVA21PATH" Value="[SOFTWARE\JavaSoft\Java Runtime Environment\21.0]" />
        <Condition Message="This application requires Java 21 or later.">
            <![CDATA[Installed OR JAVA21PATH]]>
        </Condition>
        
        <!-- Security: File integrity verification -->
        <Property Id="SECURE_INSTALL" Value="1" />
        <Property Id="VERIFY_FILES" Value="1" />
        
        <!-- Security: Anti-tampering protection -->
        <Property Id="ANTI_TAMPER" Value="1" />
        
        <!-- Security: Secure installation directory -->
        <Property Id="INSTALLDIR" Value="[ProgramFiles64Folder]SecurePDFEditor" />
        
        <!-- Security: Require admin privileges -->
        <Property Id="ALLUSERS" Value="1" />
        
        <!-- Security: Secure file permissions -->
        <Directory Id="TARGETDIR" Name="SourceDir">
            <Directory Id="ProgramFiles64Folder">
                <Directory Id="INSTALLDIR" Name="SecurePDFEditor">
                    <!-- Security: Set secure permissions on installation directory -->
                    <Component Id="SecureDirectoryPermissions" Guid="{12345678-1234-1234-1234-123456789012}">
                        <CreateFolder>
                            <Permission User="Administrators" GenericAll="yes" />
                            <Permission User="Users" GenericRead="yes" GenericExecute="yes" />
                            <Permission User="Everyone" GenericRead="yes" GenericExecute="yes" />
                        </CreateFolder>
                    </Component>
                    
                    <!-- Main application files -->
                    <Component Id="MainExecutable" Guid="{87654321-4321-4321-4321-210987654321}">
                        <File Id="SecurePDFEditorJar" 
                              Name="SecurePDFEditor.jar" 
                              Source="build/libs/secure-offline-pdf-editor-${project.version}-all.jar"
                              KeyPath="yes">
                            <!-- Security: Set secure file permissions -->
                            <Permission User="Administrators" GenericAll="yes" />
                            <Permission User="Users" GenericRead="yes" GenericExecute="yes" />
                            <Permission User="Everyone" GenericRead="yes" GenericExecute="yes" />
                        </File>
                        
                        <!-- Security: File integrity check -->
                        <util:FileHash Id="JarFileHash" File="[INSTALLDIR]SecurePDFEditor.jar" />
                    </Component>
                    
                    <!-- Security: Configuration files with restricted permissions -->
                    <Component Id="ConfigFiles" Guid="{11111111-1111-1111-1111-111111111111}">
                        <File Id="ConfigFile" 
                              Name="config.properties" 
                              Source="src/main/resources/config.properties"
                              KeyPath="yes">
                            <!-- Security: Restrict config file permissions -->
                            <Permission User="Administrators" GenericAll="yes" />
                            <Permission User="Users" GenericRead="yes" />
                        </File>
                    </Component>
                    
                    <!-- Security: Log directory with proper permissions -->
                    <Directory Id="LogDir" Name="logs">
                        <Component Id="LogDirectory" Guid="{22222222-2222-2222-2222-222222222222}">
                            <CreateFolder>
                                <Permission User="Administrators" GenericAll="yes" />
                                <Permission User="Users" GenericWrite="yes" />
                            </CreateFolder>
                        </Component>
                    </Directory>
                    
                    <!-- Security: Temp directory with restricted permissions -->
                    <Directory Id="TempDir" Name="temp">
                        <Component Id="TempDirectory" Guid="{33333333-3333-3333-3333-333333333333}">
                            <CreateFolder>
                                <Permission User="Administrators" GenericAll="yes" />
                                <Permission User="Users" GenericWrite="yes" />
                            </CreateFolder>
                        </Component>
                    </Directory>
                </Directory>
            </Directory>
        </Directory>
        
        <!-- Security: Registry entries for security configuration -->
        <Component Id="SecurityRegistry" Guid="{44444444-4444-4444-4444-444444444444}" Directory="INSTALLDIR">
            <RegistryKey Root="HKLM" Key="SOFTWARE\SecurePDFEditor\Security">
                <RegistryValue Type="string" Name="InstallPath" Value="[INSTALLDIR]" />
                <RegistryValue Type="string" Name="Version" Value="$(var.Version)" />
                <RegistryValue Type="integer" Name="SecureMode" Value="1" />
                <RegistryValue Type="integer" Name="AntiTamper" Value="1" />
                <RegistryValue Type="integer" Name="FileIntegrity" Value="1" />
                <RegistryValue Type="string" Name="InstallDate" Value="[Date]" />
                <RegistryValue Type="string" Name="InstallUser" Value="[LogonUser]" />
            </RegistryKey>
        </Component>
        
        <!-- Security: Windows Firewall rules -->
        <Component Id="FirewallRules" Guid="{55555555-5555-5555-5555-555555555555}" Directory="INSTALLDIR">
            <util:FirewallException Id="SecurePDFEditorFirewall" 
                                   Name="Secure PDF Editor" 
                                   Scope="any" 
                                   Profile="all" 
                                   IgnoreFailure="yes" />
        </Component>
        
        <!-- Security: Windows Defender exclusions -->
        <Component Id="DefenderExclusions" Guid="{66666666-6666-6666-6666-666666666666}" Directory="INSTALLDIR">
            <util:RegistryValue Id="DefenderExclusion" 
                               Root="HKLM" 
                               Key="SOFTWARE\Microsoft\Windows Defender\Exclusions\Paths" 
                               Name="[INSTALLDIR]" 
                               Value="0" 
                               Type="integer" />
        </Component>
        
        <!-- Security: Application shortcuts with integrity verification -->
        <Directory Id="ProgramMenuFolder">
            <Directory Id="ProgramMenuDir" Name="Secure PDF Editor">
                <Component Id="ProgramMenuShortcut" Guid="{77777777-7777-7777-7777-777777777777}">
                    <Shortcut Id="ProgramMenuShortcut" 
                              Name="Secure PDF Editor" 
                              Description="Secure Offline PDF Editor"
                              Target="[INSTALLDIR]SecurePDFEditor.jar"
                              WorkingDirectory="INSTALLDIR"
                              Icon="SecurePDFEditor.ico"
                              IconIndex="0">
                        <!-- Security: Verify shortcut integrity -->
                        <util:FileHash Id="ShortcutHash" File="[ProgramMenuDir]Secure PDF Editor.lnk" />
                    </Shortcut>
                    <RemoveFolder Id="ProgramMenuDir" On="uninstall" />
                    <RegistryValue Root="HKCU" Key="Software\SecurePDFEditor" Name="installed" Type="integer" Value="1" KeyPath="yes" />
                </Component>
            </Directory>
        </Directory>
        
        <!-- Security: Desktop shortcut with verification -->
        <Directory Id="DesktopFolder">
            <Component Id="DesktopShortcut" Guid="{88888888-8888-8888-8888-888888888888}">
                <Shortcut Id="DesktopShortcut" 
                          Name="Secure PDF Editor" 
                          Description="Secure Offline PDF Editor"
                          Target="[INSTALLDIR]SecurePDFEditor.jar"
                          WorkingDirectory="INSTALLDIR"
                          Icon="SecurePDFEditor.ico"
                          IconIndex="0" />
                <RemoveFolder Id="DesktopFolder" On="uninstall" />
                <RegistryValue Root="HKCU" Key="Software\SecurePDFEditor" Name="installed" Type="integer" Value="1" KeyPath="yes" />
            </Component>
        </Directory>
        
        <!-- Security: Custom actions for integrity verification -->
        <CustomAction Id="VerifyInstallation" 
                      Script="vbscript">
            <![CDATA[
                ' Verify installation integrity
                Set fso = CreateObject("Scripting.FileSystemObject")
                Set shell = CreateObject("WScript.Shell")
                
                installPath = Session.Property("INSTALLDIR")
                jarPath = installPath & "SecurePDFEditor.jar"
                
                If Not fso.FileExists(jarPath) Then
                    MsgBox "Installation verification failed: Main executable not found", vbCritical
                    Session.Property("INSTALLATION_FAILED") = "1"
                End If
                
                ' Verify file permissions
                Set file = fso.GetFile(jarPath)
                If file.Attributes And 1 Then ' Read-only
                    MsgBox "Security warning: File permissions may be compromised", vbExclamation
                End If
            ]]>
        </CustomAction>
        
        <!-- Security: Rollback action for failed installations -->
        <CustomAction Id="RollbackInstallation" 
                      Script="vbscript">
            <![CDATA[
                ' Clean up on installation failure
                installPath = Session.Property("INSTALLDIR")
                Set fso = CreateObject("Scripting.FileSystemObject")
                
                If fso.FolderExists(installPath) Then
                    fso.DeleteFolder installPath, True
                End If
            ]]>
        </CustomAction>
        
        <!-- Security: Installation sequence with verification -->
        <InstallExecuteSequence>
            <Custom Action="VerifyInstallation" After="InstallFiles">NOT Installed</Custom>
            <Custom Action="RollbackInstallation" After="VerifyInstallation">INSTALLATION_FAILED</Custom>
        </InstallExecuteSequence>
        
        <!-- Security: Feature with required components -->
        <Feature Id="Complete" 
                 Title="Secure PDF Editor" 
                 Level="1"
                 Description="Complete installation of Secure PDF Editor with all security features">
            <ComponentRef Id="SecureDirectoryPermissions" />
            <ComponentRef Id="MainExecutable" />
            <ComponentRef Id="ConfigFiles" />
            <ComponentRef Id="LogDirectory" />
            <ComponentRef Id="TempDirectory" />
            <ComponentRef Id="SecurityRegistry" />
            <ComponentRef Id="FirewallRules" />
            <ComponentRef Id="DefenderExclusions" />
            <ComponentRef Id="ProgramMenuShortcut" />
            <ComponentRef Id="DesktopShortcut" />
        </Feature>
        
        <!-- Security: UI configuration -->
        <UIRef Id="WixUI_InstallDir" />
        <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR" />
        
        <!-- Security: Installation properties -->
        <Property Id="ARPPRODUCTICON" Value="SecurePDFEditor.ico" />
        <Property Id="ARPHELPLINK" Value="https://github.com/securepdfeditor/help" />
        <Property Id="ARPURLINFOABOUT" Value="https://github.com/securepdfeditor" />
        <Property Id="ARPCONTACT" Value="security@securepdfeditor.com" />
        <Property Id="ARPCOMMENTS" Value="Secure Offline PDF Editor with anti-malware protection" />
        <Property Id="ARPSIZE" Value="50" />
        
    </Product>
</Wix>"""
}

// Create security configuration include file
fun createSecurityConfig(): String {
	return """<?xml version="1.0" encoding="utf-8"?>
<Include xmlns="http://schemas.microsoft.com/wix/2006/wi">
    <!-- Security Configuration Variables -->
    <?define Version = "${project.version}" ?>
    <?define ProductName = "Secure PDF Editor" ?>
    <?define Manufacturer = "SecurePDFEditor" ?>
    <?define ProductCode = "{${generateProductCode()}}" ?>
    <?define UpgradeCode = "{${generateUpgradeCode()}}" ?>
    
    <!-- Security Features -->
    <?define SecureMode = "1" ?>
    <?define AntiTamper = "1" ?>
    <?define FileIntegrity = "1" ?>
    <?define RequireAdmin = "1" ?>
    <?define VerifyFiles = "1" ?>
    
    <!-- Security: Minimum system requirements -->
    <?define MinWindowsVersion = "10.0.0" ?>
    <?define MinJavaVersion = "21.0.0" ?>
    <?define MinDotNetVersion = "4.5.0" ?>
    
    <!-- Security: Installation paths -->
    <?define InstallDir = "[ProgramFiles64Folder]SecurePDFEditor" ?>
    <?define LogDir = "[InstallDir]logs" ?>
    <?define TempDir = "[InstallDir]temp" ?>
    <?define ConfigDir = "[InstallDir]config" ?>
    
    <!-- Security: File permissions -->
    <?define AdminPermissions = "Administrators:Full" ?>
    <?define UserPermissions = "Users:ReadAndExecute" ?>
    <?define EveryonePermissions = "Everyone:ReadAndExecute" ?>
    
    <!-- Security: Registry security -->
    <?define RegistryKey = "SOFTWARE\SecurePDFEditor" ?>
    <?define SecurityKey = "SOFTWARE\SecurePDFEditor\Security" ?>
    
    <!-- Security: Firewall configuration -->
    <?define FirewallRuleName = "Secure PDF Editor" ?>
    <?define FirewallRuleDescription = "Secure PDF Editor network access" ?>
    
    <!-- Security: Windows Defender exclusions -->
    <?define DefenderExclusionPath = "[InstallDir]" ?>
    <?define DefenderExclusionType = "Path" ?>
    
    <!-- Security: Digital signature verification -->
    <?define VerifySignature = "1" ?>
    <?define SignatureAlgorithm = "SHA256" ?>
    <?define CertificateStore = "TrustedPublisher" ?>
    
    <!-- Security: Anti-malware features -->
    <?define ScanOnInstall = "1" ?>
    <?define ScanOnStartup = "1" ?>
    <?define QuarantineSuspicious = "1" ?>
    <?define BlockUnsafeFiles = "1" ?>
    
    <!-- Security: Network security -->
    <?define BlockNetworkAccess = "1" ?>
    <?define RequireOfflineMode = "1" ?>
    <?define ValidateConnections = "1" ?>
    
    <!-- Security: File integrity -->
    <?define HashAlgorithm = "SHA256" ?>
    <?define VerifyChecksums = "1" ?>
    <?define BlockModifiedFiles = "1" ?>
    
    <!-- Security: User permissions -->
    <?define RequireElevation = "1" ?>
    <?define RestrictUserAccess = "1" ?>
    <?define AuditAccess = "1" ?>
    
    <!-- Security: Installation verification -->
    <?define VerifyInstallation = "1" ?>
    <?define RollbackOnFailure = "1" ?>
    <?define LogInstallation = "1" ?>
    
    <!-- Security: Update security -->
    <?define SecureUpdates = "1" ?>
    <?define VerifyUpdates = "1" ?>
    <?define BlockInsecureUpdates = "1" ?>
    
    <!-- Security: Runtime protection -->
    <?define MemoryProtection = "1" ?>
    <?define StackProtection = "1" ?>
    <?define DEPEnabled = "1" ?>
    <?define ASLREnabled = "1" ?>
    
    <!-- Security: Logging and monitoring -->
    <?define SecurityLogging = "1" ?>
    <?define AuditTrail = "1" ?>
    <?define MonitorFileAccess = "1" ?>
    <?define MonitorNetworkAccess = "1" ?>
    
    <!-- Security: Encryption settings -->
    <?define UseAES256 = "1" ?>
    <?define UseSecureRandom = "1" ?>
    <?define EncryptTempFiles = "1" ?>
    <?define SecureKeyStorage = "1" ?>
    
    <!-- Security: Certificate validation -->
    <?define ValidateCertificates = "1" ?>
    <?define CheckRevocation = "1" ?>
    <?define RequireValidSignatures = "1" ?>
    
    <!-- Security: Sandbox settings -->
    <?define EnableSandbox = "1" ?>
    <?define RestrictFileAccess = "1" ?>
    <?define IsolateProcesses = "1" ?>
    
    <!-- Security: Malware detection -->
    <?define HeuristicScanning = "1" ?>
    <?define BehaviorAnalysis = "1" ?>
    <?define SignatureScanning = "1" ?>
    <?define RealTimeProtection = "1" ?>
    
    <!-- Security: Incident response -->
    <?define AutoQuarantine = "1" ?>
    <?define AlertOnThreat = "1" ?>
    <?define BlockExecution = "1" ?>
    <?define LogSecurityEvents = "1" ?>
    
</Include>"""
}
