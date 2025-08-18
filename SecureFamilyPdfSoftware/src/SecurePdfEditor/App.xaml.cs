using System;
using System.Windows;
using System.Windows.Threading;

namespace SecurePdfEditor;

/// <summary>
/// Main application class with security-first error handling and initialization.
/// </summary>
public partial class App : Application
{
    protected override void OnStartup(StartupEventArgs e)
    {
        // Configure global exception handling for security and stability
        Current.DispatcherUnhandledException += OnDispatcherUnhandledException;
        AppDomain.CurrentDomain.UnhandledException += OnUnhandledException;
        
        // Initialize security settings
        InitializeSecuritySettings();
        
        base.OnStartup(e);
    }

    /// <summary>
    /// Handles unhandled exceptions on the UI thread with secure error reporting.
    /// </summary>
    private void OnDispatcherUnhandledException(object sender, DispatcherUnhandledExceptionEventArgs e)
    {
        // Log the error securely (no sensitive data)
        LogError("UI Thread Exception", e.Exception);
        
        // Show user-friendly error message
        MessageBox.Show(
            "An unexpected error occurred. The application will continue to run, but some features may not work correctly.",
            "Application Error",
            MessageBoxButton.OK,
            MessageBoxImage.Warning);
        
        // Mark as handled to prevent application crash
        e.Handled = true;
    }

    /// <summary>
    /// Handles unhandled exceptions on non-UI threads with secure error reporting.
    /// </summary>
    private void OnUnhandledException(object sender, UnhandledExceptionEventArgs e)
    {
        if (e.ExceptionObject is Exception exception)
        {
            // Log the error securely (no sensitive data)
            LogError("Unhandled Exception", exception);
        }
        
        // For fatal errors, show critical message
        if (e.IsTerminating)
        {
            MessageBox.Show(
                "A critical error occurred and the application must close. Please restart the application.",
                "Critical Error",
                MessageBoxButton.OK,
                MessageBoxImage.Error);
        }
    }

    /// <summary>
    /// Initializes security-related settings for the application.
    /// </summary>
    private static void InitializeSecuritySettings()
    {
        try
        {
            // Disable clipboard history for security (prevents data leakage)
            if (OperatingSystem.IsWindowsVersionAtLeast(10, 0, 17763))
            {
                // Note: This would require additional Windows API calls
                // For now, we'll rely on user education about clipboard security
            }
            
            // Configure TLS settings for any future network calls (if needed)
            System.Net.ServicePointManager.SecurityProtocol = 
                System.Net.SecurityProtocolType.SystemDefault;
        }
        catch (System.Security.SecurityException ex)
        {
            // Log security initialization errors but don't fail startup
            LogError("Security Initialization Error", ex);
        }
        catch (System.Net.WebException ex)
        {
            // Log network-related errors but don't fail startup
            LogError("Network Configuration Error", ex);
        }
    }

    /// <summary>
    /// Logs errors securely without exposing sensitive information.
    /// </summary>
    private static void LogError(string context, Exception exception)
    {
        // In a production app, this would write to a secure log file
        // For now, we'll use debug output for development
        System.Diagnostics.Debug.WriteLine($"[ERROR] {context}: {exception.Message}");
        
        // Don't log stack traces or inner exceptions in production
        // as they might contain sensitive information
    }
}
