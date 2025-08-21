using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using SecureFamilyPdf.Core.Security;
using SecureFamilyPdf.Services;
using SecureFamilyPdf.ViewModels;
using System.Windows;

namespace SecureFamilyPdf;

/// <summary>
/// Main application class with dependency injection and offline-first configuration.
/// </summary>
public partial class App : Application
{
    private IHost? _host;

    protected override async void OnStartup(StartupEventArgs e)
    {
        try
        {
            // Build the host with dependency injection
            _host = CreateHostBuilder(e.Args).Build();
            
            // Start the host
            await _host.StartAsync();
            
            // Configure the main window with services
            var mainWindow = _host.Services.GetRequiredService<MainWindow>();
            mainWindow.Show();
            
            base.OnStartup(e);
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Failed to start application: {ex.Message}", 
                          "Startup Error", 
                          MessageBoxButton.OK, 
                          MessageBoxImage.Error);
            Shutdown(1);
        }
    }

    protected override async void OnExit(ExitEventArgs e)
    {
        try
        {
            if (_host != null)
            {
                await _host.StopAsync();
                _host.Dispose();
            }
        }
        catch (Exception ex)
        {
            // Log the error but don't prevent shutdown
            System.Diagnostics.Debug.WriteLine($"Error during shutdown: {ex.Message}");
        }
        
        base.OnExit(e);
    }

    private static IHostBuilder CreateHostBuilder(string[] args) =>
        Host.CreateDefaultBuilder(args)
            .ConfigureServices((context, services) =>
            {
                // Register core services
                RegisterCoreServices(services);
                
                // Register application services
                RegisterApplicationServices(services);
                
                // Register view models
                RegisterViewModels(services);
                
                // Register views
                RegisterViews(services);
            })
            .ConfigureLogging((context, logging) =>
            {
                logging.ClearProviders();
                logging.AddDebug();
                logging.AddConsole();
                logging.SetMinimumLevel(LogLevel.Information);
                
                // Ensure no telemetry or online logging
                logging.AddFilter("Microsoft", LogLevel.Warning);
                logging.AddFilter("System", LogLevel.Warning);
            });

    private static void RegisterCoreServices(IServiceCollection services)
    {
        // Security settings
        services.AddSingleton<SecuritySettings>(provider =>
        {
            return new SecuritySettings
            {
                DisableJavaScript = true,
                DisableExternalLinks = true,
                DisableEmbeddedFiles = true,
                DisableFormSubmission = true,
                MaxFileSizeBytes = 100 * 1024 * 1024, // 100MB
                EnableRedaction = true,
                RequirePasswordForSensitiveOperations = true
            };
        });

        // Security manager
        services.AddSingleton<PdfSecurityManager>();

        // PDF services
        services.AddSingleton<IPdfService, PdfService>();
        services.AddSingleton<IRedactionService, RedactionService>();
        services.AddSingleton<IPasswordProtectionService, PasswordProtectionService>();

        // File services
        services.AddSingleton<IFileService, FileService>();
        services.AddSingleton<ISettingsService, SettingsService>();
    }

    private static void RegisterApplicationServices(IServiceCollection services)
    {
        // Application state management
        services.AddSingleton<IApplicationStateService, ApplicationStateService>();
        
        // UI services
        services.AddSingleton<IDialogService, DialogService>();
        services.AddSingleton<INavigationService, NavigationService>();
        
        // Offline services (no network dependencies)
        services.AddSingleton<IOfflineService, OfflineService>();
    }

    private static void RegisterViewModels(IServiceCollection services)
    {
        // Main view models
        services.AddTransient<MainWindowViewModel>();
        services.AddTransient<PdfViewerViewModel>();
        services.AddTransient<ThumbnailPanelViewModel>();
        services.AddTransient<StatusBarViewModel>();
        
        // Dialog view models
        services.AddTransient<OpenFileDialogViewModel>();
        services.AddTransient<RedactionDialogViewModel>();
        services.AddTransient<PasswordDialogViewModel>();
    }

    private static void RegisterViews(IServiceCollection services)
    {
        // Main window
        services.AddTransient<MainWindow>();
        
        // User controls
        services.AddTransient<PdfViewerControl>();
        services.AddTransient<ThumbnailPanelControl>();
        services.AddTransient<StatusBarControl>();
    }
}
