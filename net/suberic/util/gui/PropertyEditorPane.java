package net.suberic.util.gui;
import javax.swing.*;
import java.util.Vector;
import java.awt.*;
import net.suberic.util.VariableBundle;

public class PropertyEditorPane extends Box implements PropertyEditorUI {
    Vector editors;
    PropertyEditorFactory factory;
    JInternalFrame container;

    /**
     * This contructor creates a PropertyEditor for the list of 
     * properties in the properties Vector.
     */     
    public PropertyEditorPane(PropertyEditorFactory newFactory, 
                              Vector properties, 
                              JInternalFrame newContainer) {
	super(BoxLayout.Y_AXIS);
        
	factory = newFactory;
	container = newContainer;

	DefaultPropertyEditor currentEditor;

	editors = new Vector();

	for (int i = 0; i < properties.size(); i++) {
	    currentEditor =
              factory.createEditor((String)properties.elementAt(i));
	    editors.add(currentEditor);
	    this.add(currentEditor);
	}
	if (container != null)
	    this.addButtons();
    }

    /**
     * This contructor creates a PropertyEditor for the list of 
     * properties in the properties Vector, using the template
     * types in the templateTypes Vector.  Note that there should be
     * one entry in each of the properties Vector and the templateTypes
     * Vector for each property to be edited.
     */     
    public PropertyEditorPane(PropertyEditorFactory newFactory, 
                              Vector properties, Vector templateTypes,
                              JInternalFrame newContainer) {
	super(BoxLayout.Y_AXIS);
        
	factory = newFactory;
	container = newContainer;

	DefaultPropertyEditor currentEditor;

	editors = new Vector();

	for (int i = 0; i < properties.size(); i++) {
	    currentEditor =
              factory.createEditor((String)properties.elementAt(i), (String)templateTypes.elementAt(i));
	    editors.add(currentEditor);
	    this.add(currentEditor);
	}
	if (container != null)
	    this.addButtons();
    }

    public void configureEditor(PropertyEditorFactory factory, String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
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
	if (isEnabled()) {
	    for (int i = 0; i < editors.size(); i++) {
		((DefaultPropertyEditor)(editors.elementAt(i))).setValue();
	    }
	}
    }

    public java.util.Properties getValue() {
	java.util.Properties currentRetValue = new java.util.Properties();
	for (int i = 0; i < editors.size(); i++) {
	    currentRetValue = new java.util.Properties(((DefaultPropertyEditor)(editors.elementAt(i))).getValue());
	}

	return currentRetValue;
    }

    public void resetDefaultValue() {
	for (int i = 0; i < editors.size(); i++) {
	    ((DefaultPropertyEditor)(editors.elementAt(i))).resetDefaultValue();
	}
    }

    public void addButtons() {
	Box buttonBox = new Box(BoxLayout.X_AXIS);
	
	buttonBox.add(createButton("Ok", new AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
		setValue();
		try {
		    container.setClosed(true);
		} catch (java.beans.PropertyVetoException pve) {
		}
	    }
	}, true));

	buttonBox.add(createButton("Apply", new AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
		setValue();
	    }
	}, false));

	buttonBox.add(createButton("Cancel", new AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
		try {
		    container.setClosed(true);
		} catch (java.beans.PropertyVetoException pve) {
		}
	    }
	}, false));

	this.add(buttonBox);
    }

    private JButton createButton(String label, Action e, boolean isDefault) {
	JButton thisButton;
	
        thisButton = new JButton(factory.getBundle().getProperty("label." + label, label));
	try {
	    thisButton.setMnemonic(factory.getBundle().getProperty("label." + label + ".mnemonic").charAt(0));
	} catch (java.util.MissingResourceException mre) {
	}

	thisButton.setSelected(isDefault);

	thisButton.addActionListener(e);

	return thisButton;
    }

    public void setEnabled(boolean newValue) {
	if (editors != null && editors.size() > 0) {
	    for (int i = 0; i < editors.size(); i++) {
		PropertyEditorUI currentEditor = (PropertyEditorUI) editors.elementAt(i);
		currentEditor.setEnabled(newValue);
	    }
	}
    }
}
    


