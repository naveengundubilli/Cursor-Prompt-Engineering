package com.securepdfeditor.history;

import com.securepdfeditor.editing.EditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

public class HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    
    private final Deque<HistoryCommand> undoStack = new ArrayDeque<>();
    private final Deque<HistoryCommand> redoStack = new ArrayDeque<>();
    private boolean isExecuting = false;
    
    public interface HistoryCommand {
        void execute();
        void undo();
        String getDescription();
    }
    
    // Text editing commands
    public static class TextEditCommand implements HistoryCommand {
        private final EditAction action;
        private final Runnable executeAction;
        private final Runnable undoAction;
        
        public TextEditCommand(EditAction action, Runnable executeAction, Runnable undoAction) {
            this.action = action;
            this.executeAction = executeAction;
            this.undoAction = undoAction;
        }
        
        @Override
        public void execute() {
            executeAction.run();
        }
        
        @Override
        public void undo() {
            undoAction.run();
        }
        
        @Override
        public String getDescription() {
            return "Text " + action.getType().toString().toLowerCase();
        }
    }
    
    // Zoom commands
    public static class ZoomCommand implements HistoryCommand {
        private final double oldZoom;
        private final double newZoom;
        private final Runnable zoomAction;
        
        public ZoomCommand(double oldZoom, double newZoom, Runnable zoomAction) {
            this.oldZoom = oldZoom;
            this.newZoom = newZoom;
            this.zoomAction = zoomAction;
        }
        
        @Override
        public void execute() {
            zoomAction.run();
        }
        
        @Override
        public void undo() {
            // Zoom back to old value
            // This would need to be implemented with a callback
        }
        
        @Override
        public String getDescription() {
            return String.format("Zoom %.0f%%", newZoom * 100);
        }
    }
    
    // Page navigation commands
    public static class PageNavigationCommand implements HistoryCommand {
        private final int oldPage;
        private final int newPage;
        private final Runnable navigationAction;
        
        public PageNavigationCommand(int oldPage, int newPage, Runnable navigationAction) {
            this.oldPage = oldPage;
            this.newPage = newPage;
            this.navigationAction = navigationAction;
        }
        
        @Override
        public void execute() {
            navigationAction.run();
        }
        
        @Override
        public void undo() {
            // Navigate back to old page
            // This would need to be implemented with a callback
        }
        
        @Override
        public String getDescription() {
            return String.format("Page %d → %d", oldPage + 1, newPage + 1);
        }
    }
    
    // Form field commands
    public static class FormFieldCommand implements HistoryCommand {
        private final String fieldName;
        private final String oldValue;
        private final String newValue;
        private final Runnable executeAction;
        private final Runnable undoAction;
        
        public FormFieldCommand(String fieldName, String oldValue, String newValue, 
                              Runnable executeAction, Runnable undoAction) {
            this.fieldName = fieldName;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.executeAction = executeAction;
            this.undoAction = undoAction;
        }
        
        @Override
        public void execute() {
            executeAction.run();
        }
        
        @Override
        public void undo() {
            undoAction.run();
        }
        
        @Override
        public String getDescription() {
            return String.format("Form field '%s': '%s' → '%s'", fieldName, oldValue, newValue);
        }
    }
    
    // Page management commands
    public static class PageManagementCommand implements HistoryCommand {
        private final String operation;
        private final int pageIndex;
        private final Runnable executeAction;
        private final Runnable undoAction;
        
        public PageManagementCommand(String operation, int pageIndex, 
                                   Runnable executeAction, Runnable undoAction) {
            this.operation = operation;
            this.pageIndex = pageIndex;
            this.executeAction = executeAction;
            this.undoAction = undoAction;
        }
        
        @Override
        public void execute() {
            executeAction.run();
        }
        
        @Override
        public void undo() {
            undoAction.run();
        }
        
        @Override
        public String getDescription() {
            return String.format("%s page %d", operation, pageIndex + 1);
        }
    }
    
    public void executeCommand(HistoryCommand command) {
        if (isExecuting) {
            return; // Prevent recursive execution
        }
        
        try {
            isExecuting = true;
            command.execute();
            undoStack.push(command);
            redoStack.clear(); // Clear redo stack when new command is executed
            logger.info("Executed command: {}", command.getDescription());
        } finally {
            isExecuting = false;
        }
    }
    
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    public void undo() {
        if (!canUndo()) {
            logger.warn("Cannot undo: stack is empty");
            return;
        }
        
        try {
            isExecuting = true;
            HistoryCommand command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            logger.info("Undid command: {}", command.getDescription());
        } finally {
            isExecuting = false;
        }
    }
    
    public void redo() {
        if (!canRedo()) {
            logger.warn("Cannot redo: stack is empty");
            return;
        }
        
        try {
            isExecuting = true;
            HistoryCommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            logger.info("Redid command: {}", command.getDescription());
        } finally {
            isExecuting = false;
        }
    }
    
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        logger.info("History cleared");
    }
    
    public int getUndoCount() {
        return undoStack.size();
    }
    
    public int getRedoCount() {
        return redoStack.size();
    }
    
    public List<String> getUndoHistory() {
        List<String> history = new ArrayList<>();
        for (HistoryCommand command : undoStack) {
            history.add(command.getDescription());
        }
        return history;
    }
    
    public List<String> getRedoHistory() {
        List<String> history = new ArrayList<>();
        for (HistoryCommand command : redoStack) {
            history.add(command.getDescription());
        }
        return history;
    }
    
    public String getNextUndoDescription() {
        return undoStack.isEmpty() ? null : undoStack.peek().getDescription();
    }
    
    public String getNextRedoDescription() {
        return redoStack.isEmpty() ? null : redoStack.peek().getDescription();
    }
}
