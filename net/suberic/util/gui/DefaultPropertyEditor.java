package net.suberic.util.gui;
import net.suberic.util.*;
import javax.swing.*;

public abstract class DefaultPropertyEditor extends Box implements PropertyEditorUI {
    boolean enabled;

    public DefaultPropertyEditor() {
	super(BoxLayout.X_AXIS);
    }

    public void configureEditor(String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, templateType, bundle, isEnabled);
    }

    public void configureEditor(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, newProperty, bundle, isEnabled);
    }

    public void configureEditor(String newProperty, VariableBundle bundle) {
	configureEditor(newProperty, newProperty, bundle, true);
    }

    public void setValue() {
    };

    public java.util.Properties getValue() {
	return null;
    };

    public void resetDefaultValue() {
    };

    public void setEnabled(boolean newValue) {
	enabled=newValue;
    }

    public boolean isEnabled() {
	return enabled;
    }
}
