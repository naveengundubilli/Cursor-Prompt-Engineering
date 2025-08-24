using System;
using System.Globalization;
using System.Windows.Data;

namespace SecureFamilyPdf.Views;

/// <summary>
/// Converts zoom level (double) to display string (e.g., "100%", "150%")
/// </summary>
public class ZoomLevelConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is double zoomLevel)
        {
            return $"{zoomLevel * 100:F0}%";
        }
        
        return "100%";
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is string zoomString && zoomString.EndsWith('%'))
        {
            if (double.TryParse(zoomString.TrimEnd('%'), out double percentage))
            {
                return percentage / 100.0;
            }
        }
        
        return 1.0;
    }
}
