package net.suberic.util.gui;

/**
 * This class is a KeyBinding controller for a JComponent.
 */

public class ConfigurableKeyBinding implements ConfigurableUI {
    JComponent currentComponent;

    /**
     * This creates a new ConfigurableKeyBinding which attaches to component
     * newComponent, and defines itself using componentID and vars.
     */
    public ConfigurableKeyBinding(JComponent newComponent, String componentID, VariableBundle vars) {
	currentComponent = newComponent;
	
	configureComponent(componentID, vars);
    }

    public void configureComponent(String componentID, VariableBundle vars) {

    }
}
