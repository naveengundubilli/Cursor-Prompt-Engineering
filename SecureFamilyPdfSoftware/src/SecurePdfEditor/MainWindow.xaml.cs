using System.Windows;
using SecurePdfEditor.ViewModels;

namespace SecurePdfEditor;

/// <summary>
/// Main window of the Secure PDF Editor application.
/// Implements MVVM pattern with proper separation of concerns.
/// </summary>
public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
        
        // Initialize the ViewModel and set it as the DataContext
        // This follows MVVM pattern for clean separation of UI and business logic
        DataContext = new MainWindowViewModel();
    }
}

