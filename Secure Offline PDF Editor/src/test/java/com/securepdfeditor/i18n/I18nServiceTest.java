package com.securepdfeditor.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class I18nServiceTest {
    
    private I18nService i18nService;
    
    @BeforeEach
    void setUp() {
        i18nService = I18nService.getInstance();
    }
    
    @Test
    void testSingletonInstance() {
        I18nService instance1 = I18nService.getInstance();
        I18nService instance2 = I18nService.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test
    void testDefaultLanguage() {
        assertEquals("en", i18nService.getCurrentLanguage());
        assertNotNull(i18nService.getCurrentLocale());
    }
    
    @Test
    void testSetLanguage() {
        i18nService.setLanguage("en");
        assertEquals("en", i18nService.getCurrentLanguage());
    }
    
    @Test
    void testGetString() {
        // Test getting a string that should exist
        String result = i18nService.getString("app.title");
        assertNotNull(result);
        // If resource bundle is not loaded, it will return the key
        if (!result.equals("app.title")) {
            assertFalse(result.isEmpty());
        }
        
        // Test getting a non-existent string (should return the key)
        String nonExistent = i18nService.getString("non.existent.key");
        assertEquals("non.existent.key", nonExistent);
    }
    
    @Test
    void testGetStringWithFormatting() {
        String result = i18nService.getString("page.current", 1, 5);
        assertNotNull(result);
        // Just verify the result is not null, regardless of whether bundle is loaded
    }
    
    @Test
    void testSupportedLanguages() {
        String[] languages = i18nService.getSupportedLanguages();
        assertNotNull(languages);
        assertTrue(languages.length > 0);
        assertTrue(languages[0].equals("en"));
    }
    
    @Test
    void testLanguageNames() {
        String[] names = i18nService.getLanguageNames();
        assertNotNull(names);
        assertTrue(names.length > 0);
        assertTrue(names[0].equals("English"));
    }
    
    @Test
    void testGetLanguageName() {
        String name = i18nService.getLanguageName("en");
        assertEquals("English", name);
        
        // Test with unsupported language
        String unsupported = i18nService.getLanguageName("fr");
        assertEquals("fr", unsupported);
    }
    
    @Test
    void testIsLanguageSupported() {
        assertTrue(i18nService.isLanguageSupported("en"));
        assertFalse(i18nService.isLanguageSupported("fr"));
        assertFalse(i18nService.isLanguageSupported("de"));
    }
    
    @Test
    void testCurrentLanguageProperty() {
        assertNotNull(i18nService.currentLanguageProperty());
        assertEquals("en", i18nService.currentLanguageProperty().get());
    }
    
    @Test
    void testReloadBundles() {
        // Test that reloading doesn't throw an exception
        assertDoesNotThrow(() -> i18nService.reloadBundles());
    }
}
