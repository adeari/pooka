package net.suberic.util.gui;
import net.suberic.util.*;
import javax.swing.*;

public abstract class DefaultPropertyEditor extends Box implements AkpPropertyEditor {
    boolean enabled;

    public DefaultPropertyEditor() {
	super(BoxLayout.X_AXIS);
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
