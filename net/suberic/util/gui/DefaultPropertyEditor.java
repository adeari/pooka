package net.suberic.util.gui;
import net.suberic.util.*;
import javax.swing.*;
import java.awt.Dimension;

/**
 * A Swing implementation of the PropertyEditorUI.
 */
public abstract class DefaultPropertyEditor extends JPanel implements PropertyEditorUI {
    // shows whether or not this component is enabled.
    protected boolean enabled;

    // the label component.  this is used for a default implementation
    // of the sizing code we have below.
    protected java.awt.Container labelComponent;

    // the value component.  this is used for a default implementation
    // of the sizing code we have below.
    protected java.awt.Container valueComponent;

    /**
     * Creates a new DefaultPropertyEditor, in this case a Box with an
     * X_AXIS layout.
     */
    public DefaultPropertyEditor() {
	super();
	this.setLayout(new java.awt.GridBagLayout());
    }

    /**
     * Creates a new DefaultPropertyEditor, with the axis as given.
     */
    public DefaultPropertyEditor(int axis) {
	super();
	this.setLayout(new java.awt.GridBagLayout());
    }

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
     */
    public void configureEditor(String propertyName, String template, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, propertyName, template, bundle, isEnabled);
    }

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
     */
    public void configureEditor(String propertyName, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, propertyName, propertyName, bundle, isEnabled);
    }

    /**
     * This configures an editor for the given propertyName in the 
     * VariableBundle bundle.
     */
    public void configureEditor(String propertyName, VariableBundle bundle) {
	configureEditor(null, propertyName, propertyName, bundle, true);
    }

    /**
     * A default implementation of setEnabled.  This simply sets the
     * enabled flag to the newValue.  If the labelComponent and 
     * valueComponent attributes are set, it will also call setEnabled
     * on those.
     *
     * Subclasses which do not use the default labelComponent and 
     * valueComponent attributes, or which require additional functionality,
     * should override this method.
     */
    public void setEnabled(boolean newValue) {
	enabled=newValue;
    }

    /**
     * Returns the enabled flag.
     */
    public boolean isEnabled() {
	return enabled;
    }

    /**
     * Gets the minimum size for the labelComponent.
     */
    public Dimension getMinimumLabelSize() {
	if (labelComponent != null) {
	    return labelComponent.getMinimumSize();
	} else {
	    return new Dimension(0,0);
	}
    }

    /**
     * Gets the minimum size for the valueComponent.
     */
    public Dimension getMinimumValueSize() {
	if (valueComponent != null) {
	    return valueComponent.getMinimumSize();
	} else {
	    return new Dimension(0,0);
	}
    }

    /**
     * Returns the calculated minimum size for this component.
     */
    public Dimension getMinimumTotalSize() {
	return this.getMinimumSize();
    }

    /**
     * Sets the size for the label component and the value component.
     */
    public void setSizes(Dimension labelSize, Dimension valueSize) {
	if (labelComponent != null)
	    labelComponent.setSize(labelSize);
	if (valueComponent != null)
	    valueComponent.setSize(valueSize);
    }

    /**
     * Sets the widths for the label component and the value component.
     */
    public void setWidths(int labelWidth, int valueWidth) {
	if (labelComponent != null)
	    labelComponent.setSize(new Dimension(labelWidth, labelComponent.getSize().height));
	if (valueComponent != null)
	    valueComponent.setSize(new Dimension(valueWidth, valueComponent.getSize().height));
    }

    /**
     * Sets the heights for the label component and the value component.
     */
    public void setHeights(int labelHeight, int valueHeight) {
	if (labelComponent != null)
	    labelComponent.setSize(new Dimension(labelComponent.getSize().width, labelHeight));
	if (valueComponent != null)
	    valueComponent.setSize(new Dimension(valueComponent.getSize().width, valueHeight));
    }

    /**
     * Gets the current valueComponent.
     */
    public java.awt.Container getValueComponent() {
	return valueComponent;
    } 
}
