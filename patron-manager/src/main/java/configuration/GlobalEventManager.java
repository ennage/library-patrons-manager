package configuration;

import javafx.beans.property.SimpleBooleanProperty;

public class GlobalEventManager {
    
    // A singleton instance to ensure all parts of the application use the same signal
    private static final GlobalEventManager INSTANCE = new GlobalEventManager();
    
    // The signal property. When its value changes, all listeners are notified.
    private final SimpleBooleanProperty refreshSignal = new SimpleBooleanProperty(false);
    
    private GlobalEventManager() {
        // Private constructor for Singleton pattern
    }

    public static GlobalEventManager getInstance() {
        return INSTANCE;
    }

    /**
     * Public method for controllers to access the read-only signal property.
     * Controllers will bind their refresh logic to this property.
     */
    public SimpleBooleanProperty getRefreshSignal() {
        return refreshSignal;
    }

    /**
     * Public method to trigger a refresh across the entire application.
     * This simply toggles the value of the property, notifying all listeners.
     */
    public void triggerRefresh() {
        // Toggling the value (true -> false or false -> true) forces a change event.
        refreshSignal.set(!refreshSignal.get());
        System.out.println("Global Refresh triggered.");
    }
}