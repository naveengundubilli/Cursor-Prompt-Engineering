using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Collections.Generic;
using System.Windows.Input;

namespace SecureFamilyPdf.ViewModels;

/// <summary>
/// View model for the main window.
/// </summary>
public class MainWindowViewModel : INotifyPropertyChanged
{
    private string _documentName = "No document loaded";
    private int _currentPage;
    private int _totalPages;
    private double _zoomLevel = 1.0;
    private string _statusMessage = "Ready";
    private bool _showThumbnails = true;
    private bool _showStatusBar = true;

    public string DocumentName
    {
        get => _documentName;
        set => SetProperty(ref _documentName, value);
    }

    public int CurrentPage
    {
        get => _currentPage;
        set => SetProperty(ref _currentPage, value);
    }

    public int TotalPages
    {
        get => _totalPages;
        set => SetProperty(ref _totalPages, value);
    }

    public double ZoomLevel
    {
        get => _zoomLevel;
        set => SetProperty(ref _zoomLevel, value);
    }

    public string StatusMessage
    {
        get => _statusMessage;
        set => SetProperty(ref _statusMessage, value);
    }

    public bool ShowThumbnails
    {
        get => _showThumbnails;
        set => SetProperty(ref _showThumbnails, value);
    }

    public bool ShowStatusBar
    {
        get => _showStatusBar;
        set => SetProperty(ref _showStatusBar, value);
    }

    // Commands
    public ICommand OpenFileCommand { get; } = new RelayCommand(() => { });
    public ICommand SaveAsCommand { get; } = new RelayCommand(() => { });
    public ICommand SaveCopyCommand { get; } = new RelayCommand(() => { });
    public ICommand PrintCommand { get; } = new RelayCommand(() => { });
    public ICommand ExitCommand { get; } = new RelayCommand(() => { });
    public ICommand ClearRecentFilesCommand { get; } = new RelayCommand(() => { });
    public ICommand UndoCommand { get; } = new RelayCommand(() => { });
    public ICommand RedoCommand { get; } = new RelayCommand(() => { });
    public ICommand CopyCommand { get; } = new RelayCommand(() => { });
    public ICommand SelectAllCommand { get; } = new RelayCommand(() => { });
    public ICommand ZoomInCommand { get; } = new RelayCommand(() => { });
    public ICommand ZoomOutCommand { get; } = new RelayCommand(() => { });
    public ICommand ActualSizeCommand { get; } = new RelayCommand(() => { });
    public ICommand FitPageCommand { get; } = new RelayCommand(() => { });
    public ICommand FitWidthCommand { get; } = new RelayCommand(() => { });
    public ICommand HighlightTextCommand { get; } = new RelayCommand(() => { });
    public ICommand AddStickyNoteCommand { get; } = new RelayCommand(() => { });
    public ICommand SearchCommand { get; } = new RelayCommand(() => { });
    public ICommand RedactTextCommand { get; } = new RelayCommand(() => { });
    public ICommand PasswordProtectCommand { get; } = new RelayCommand(() => { });
    public ICommand RemovePasswordCommand { get; } = new RelayCommand(() => { });
    public ICommand AboutCommand { get; } = new RelayCommand(() => { });
    public ICommand UserGuideCommand { get; } = new RelayCommand(() => { });

    public void Initialize()
    {
        // Initialize the view model
        StatusMessage = "Application initialized";
    }

    public void Cleanup()
    {
        // Clean up resources
        StatusMessage = "Cleaning up...";
    }

    public event PropertyChangedEventHandler? PropertyChanged;

    protected virtual void OnPropertyChanged([CallerMemberName] string? propertyName = null)
    {
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }

    protected bool SetProperty<T>(ref T field, T value, [CallerMemberName] string? propertyName = null)
    {
        if (EqualityComparer<T>.Default.Equals(field, value)) return false;
        field = value;
        OnPropertyChanged(propertyName);
        return true;
    }
}
