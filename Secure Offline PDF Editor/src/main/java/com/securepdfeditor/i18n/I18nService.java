package com.securepdfeditor.i18n;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class I18nService {
    private static final Logger logger = LoggerFactory.getLogger(I18nService.class);
    
    private static I18nService instance;
    private ResourceBundle currentBundle;
    private Locale currentLocale;
    private final StringProperty currentLanguageProperty = new SimpleStringProperty("en");
    
    // Supported languages
    public static final String[] SUPPORTED_LANGUAGES = {"en"};
    public static final String[] LANGUAGE_NAMES = {"English"};
    
    private I18nService() {
        setLanguage("en"); // Default to English
    }
    
    public static I18nService getInstance() {
        if (instance == null) {
            instance = new I18nService();
        }
        return instance;
    }
    
    public void setLanguage(String languageCode) {
        try {
            Locale newLocale = new Locale(languageCode);
            currentBundle = ResourceBundle.getBundle("i18n.messages", newLocale);
            currentLocale = newLocale;
            currentLanguageProperty.set(languageCode);
            logger.info("Language set to: {}", languageCode);
        } catch (Exception e) {
            logger.error("Failed to set language to {}: {}", languageCode, e.getMessage());
            // Fallback to English
            setLanguage("en");
        }
    }
    
    public String getString(String key) {
        try {
            if (currentBundle != null && currentBundle.containsKey(key)) {
                return currentBundle.getString(key);
            }
        } catch (Exception e) {
            logger.warn("Failed to get string for key '{}': {}", key, e.getMessage());
        }
        
        // Fallback to key itself if translation not found
        return key;
    }
    
    public String getString(String key, Object... args) {
        String template = getString(key);
        try {
            return String.format(template, args);
        } catch (Exception e) {
            logger.warn("Failed to format string for key '{}': {}", key, e.getMessage());
            return template;
        }
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public String getCurrentLanguage() {
        return currentLanguageProperty.get();
    }
    
    public StringProperty currentLanguageProperty() {
        return currentLanguageProperty;
    }
    
    public String[] getSupportedLanguages() {
        return SUPPORTED_LANGUAGES.clone();
    }
    
    public String[] getLanguageNames() {
        return LANGUAGE_NAMES.clone();
    }
    
    public String getLanguageName(String languageCode) {
        for (int i = 0; i < SUPPORTED_LANGUAGES.length; i++) {
            if (SUPPORTED_LANGUAGES[i].equals(languageCode)) {
                return LANGUAGE_NAMES[i];
            }
        }
        return languageCode;
    }
    
    public boolean isLanguageSupported(String languageCode) {
        for (String supported : SUPPORTED_LANGUAGES) {
            if (supported.equals(languageCode)) {
                return true;
            }
        }
        return false;
    }
    
    public void reloadBundles() {
        if (currentLocale != null) {
            setLanguage(currentLocale.getLanguage());
        }
    }
}
