using Microsoft.Extensions.DependencyInjection;
using SecureFamilyPdf.ViewModels;
using System.Windows;

namespace SecureFamilyPdf.Views;

/// <summary>
/// Main window for the Secure Family PDF application.
/// </summary>
public partial class MainWindow : Window
{
    public MainWindow(IServiceProvider serviceProvider)
    {
        InitializeComponent();
        
        // Get the main view model from the service provider
        var mainViewModel = serviceProvider.GetRequiredService<MainWindowViewModel>();
        DataContext = mainViewModel;
        
        // Set up window event handlers
        Loaded += MainWindow_Loaded;
        Closing += MainWindow_Closing;
    }

    private void MainWindow_Loaded(object sender, RoutedEventArgs e)
    {
        // Initialize the application when the window is loaded
        if (DataContext is MainWindowViewModel viewModel)
        {
            viewModel.Initialize();
        }
    }

    private void MainWindow_Closing(object sender, System.ComponentModel.CancelEventArgs e)
    {
        // Clean up resources when the window is closing
        if (DataContext is MainWindowViewModel viewModel)
        {
            viewModel.Cleanup();
        }
    }
}
