package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.List;
import java.awt.*;

/**
 * This is a top-level editor for properties.  It includes buttons for
 * accepting changes, accepting and closing, and cancelling the action.
 */
public class PropertyEditorPane extends Box implements PropertyEditorUI {
  List editors;
  PropertyEditorManager manager;
  Container container;
  
  /**
   * This contructor creates a PropertyEditor for the list of 
   * properties in the properties List.
   */     
  public PropertyEditorPane(PropertyEditorManager newManager, 
			    List properties, 
			    Container newContainer) {
    this(newManager, properties, properties, newContainer);
  }
  /*
    super(BoxLayout.Y_AXIS);
    
    manager = newManager;
    container = newContainer;
    
    DefaultPropertyEditor currentEditor;
    
    editors = new LinkedList();
    
    for (int i = 0; i < properties.size(); i++) {
      currentEditor =
	manager.createEditor((String)properties.get(i));
      editors.add(currentEditor);
      this.add(currentEditor);
    }
    if (container != null)
      this.addButtons();
  }
  */
  /**
   * This contructor creates a PropertyEditor for the list of 
   * properties in the properties List, using the template
   * types in the templateTypes List.  Note that there should be
   * one entry in each of the properties List and the templateTypes
   * List for each property to be edited.
   */     
  public PropertyEditorPane(PropertyEditorManager newManager, 
			    List properties, List templateTypes,
			    Container newContainer) {
    super(BoxLayout.Y_AXIS);
    
    manager = newManager;
    container = newContainer;
    
    DefaultPropertyEditor currentEditor;
    
    editors = new LinkedList();
    
    for (int i = 0; i < properties.size(); i++) {
      currentEditor =
	manager.createEditor((String)properties.get(i), (String)templateTypes.get(i));
	    editors.add(currentEditor);
	    this.add(currentEditor);
	}
	if (container != null)
	    this.addButtons();
    }

    /**
     * This contructor creates a PropertyEditor using the given 
     * DefaultPropertyEditor.
     */     
    public PropertyEditorPane(PropertyEditorManager newManager, 
			      DefaultPropertyEditor newEditor,
                              Container newContainer) {
      super(BoxLayout.Y_AXIS);
      
      manager = newManager;
      container = newContainer;
      
      editors = new LinkedList();
      
      editors.add(newEditor);
      this.add(newEditor);
      
      if (container != null)
	this.addButtons();
    }

    public void configureEditor(PropertyEditorManager newManager, String newProperty, String templateType, PropertyEditorManager manager, boolean isEnabled) {
	
    }

    public void configureEditor(String newProperty, String templateType, PropertyEditorManager manager, boolean isEnabled) {
	configureEditor(newProperty, templateType, manager, isEnabled);
    }

    public void configureEditor(String newProperty, PropertyEditorManager manager, boolean isEnabled) {
	configureEditor(newProperty, newProperty, manager, isEnabled);
    }

    public void configureEditor(String newProperty, PropertyEditorManager manager) {
	configureEditor(newProperty, newProperty, manager, true);
    }

    public void setValue() {
	if (isEnabled()) {
	    for (int i = 0; i < editors.size(); i++) {
		((DefaultPropertyEditor)(editors.get(i))).setValue();
	    }
	}
    }

    public java.util.Properties getValue() {
	java.util.Properties currentRetValue = new java.util.Properties();
	for (int i = 0; i < editors.size(); i++) {
	    currentRetValue.putAll(((DefaultPropertyEditor)(editors.get(i))).getValue());
	}

	return currentRetValue;
    }

    public void resetDefaultValue() {
	for (int i = 0; i < editors.size(); i++) {
	    ((DefaultPropertyEditor)(editors.get(i))).resetDefaultValue();
	}
    }

    public void addButtons() {
	Box buttonBox = new Box(BoxLayout.X_AXIS);
	
	buttonBox.add(createButton("Ok", new AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
		setValue();
		manager.saveProperties();
		if (container instanceof JInternalFrame) {
		    try {
			((JInternalFrame)container).setClosed(true);
		    } catch (java.beans.PropertyVetoException pve) {
		    }
		} else if (container instanceof JFrame) {
		    ((JFrame)container).dispose();
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
		if (container instanceof JInternalFrame) {
		    try {
			((JInternalFrame)container).setClosed(true);
		    } catch (java.beans.PropertyVetoException pve) {
		    }
		} else if (container instanceof JFrame) {
		    ((JFrame)container).dispose();
		}
	    }
	}, false));

	this.add(buttonBox);
    }

    private JButton createButton(String label, Action e, boolean isDefault) {
	JButton thisButton;
	
        thisButton = new JButton(manager.getProperty("label." + label, label));
	try {
	    thisButton.setMnemonic(manager.getProperty("label." + label + ".mnemonic").charAt(0));
	} catch (java.util.MissingResourceException mre) {
	}

	thisButton.setSelected(isDefault);

	thisButton.addActionListener(e);

	return thisButton;
    }

    public void setEnabled(boolean newValue) {
	if (editors != null && editors.size() > 0) {
	    for (int i = 0; i < editors.size(); i++) {
		PropertyEditorUI currentEditor = (PropertyEditorUI) editors.get(i);
		currentEditor.setEnabled(newValue);
	    }
	}
    }
}
    


