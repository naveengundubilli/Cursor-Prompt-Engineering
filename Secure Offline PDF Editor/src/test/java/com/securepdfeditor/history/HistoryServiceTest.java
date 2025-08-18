package com.securepdfeditor.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryServiceTest {
    
    private HistoryService historyService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
    }
    
    @Test
    void testExecuteCommand() {
        assertFalse(historyService.canUndo());
        assertFalse(historyService.canRedo());
        
        // Execute a command
        historyService.executeCommand(new TestCommand("Test Command"));
        
        assertTrue(historyService.canUndo());
        assertFalse(historyService.canRedo());
        assertEquals(1, historyService.getUndoCount());
        assertEquals(0, historyService.getRedoCount());
    }
    
    @Test
    void testUndoRedo() {
        // Execute multiple commands
        historyService.executeCommand(new TestCommand("Command 1"));
        historyService.executeCommand(new TestCommand("Command 2"));
        
        assertEquals(2, historyService.getUndoCount());
        
        // Undo
        historyService.undo();
        assertEquals(1, historyService.getUndoCount());
        assertEquals(1, historyService.getRedoCount());
        
        // Redo
        historyService.redo();
        assertEquals(2, historyService.getUndoCount());
        assertEquals(0, historyService.getRedoCount());
    }
    
    @Test
    void testUndoWhenEmpty() {
        // Undo should not throw exception, just log warning
        assertDoesNotThrow(() -> historyService.undo());
        assertFalse(historyService.canUndo());
    }
    
    @Test
    void testRedoWhenEmpty() {
        // Redo should not throw exception, just log warning
        assertDoesNotThrow(() -> historyService.redo());
        assertFalse(historyService.canRedo());
    }
    
    @Test
    void testClear() {
        historyService.executeCommand(new TestCommand("Command 1"));
        historyService.executeCommand(new TestCommand("Command 2"));
        
        assertEquals(2, historyService.getUndoCount());
        
        historyService.clear();
        
        assertEquals(0, historyService.getUndoCount());
        assertEquals(0, historyService.getRedoCount());
        assertFalse(historyService.canUndo());
        assertFalse(historyService.canRedo());
    }
    
    @Test
    void testGetHistory() {
        historyService.executeCommand(new TestCommand("Command 1"));
        historyService.executeCommand(new TestCommand("Command 2"));
        
        var undoHistory = historyService.getUndoHistory();
        assertEquals(2, undoHistory.size());
        assertEquals("Command 2", undoHistory.get(0));
        assertEquals("Command 1", undoHistory.get(1));
    }
    
    @Test
    void testRedoClearedOnNewCommand() {
        historyService.executeCommand(new TestCommand("Command 1"));
        historyService.undo();
        assertEquals(1, historyService.getRedoCount());
        
        historyService.executeCommand(new TestCommand("Command 2"));
        assertEquals(0, historyService.getRedoCount());
    }
    
    private static class TestCommand implements HistoryService.HistoryCommand {
        private final String description;
        private boolean executed = false;
        
        public TestCommand(String description) {
            this.description = description;
        }
        
        @Override
        public void execute() {
            executed = true;
        }
        
        @Override
        public void undo() {
            executed = false;
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        public boolean isExecuted() {
            return executed;
        }
    }
}
