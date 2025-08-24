using System.Windows;
using Application = System.Windows.Application;

namespace SecureFamilyPdf;

/// <summary>
/// Main application class with simplified startup.
/// </summary>
public partial class App : Application
{
    protected override void OnStartup(StartupEventArgs e)
    {
        try
        {
            // Create and show the main window directly
            var mainWindow = new Views.MainWindow();
            mainWindow.Show();
            
            // Set as main window
            MainWindow = mainWindow;
            
            base.OnStartup(e);
        }
        catch (System.Exception ex)
        {
            System.Windows.MessageBox.Show($"Failed to start application: {ex.Message}", 
                          "Startup Error", 
                          System.Windows.MessageBoxButton.OK, 
                          System.Windows.MessageBoxImage.Error);
            Shutdown(1);
        }
    }
}
