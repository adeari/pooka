package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.List;
import java.util.LinkedList;
import java.awt.*;

/**
 * This is a top-level editor for properties.  It includes buttons for
 * activating changes, accepting and closing, and cancelling the action.
 */
public class PropertyEditorPane extends Box {
  SwingPropertyEditor editor;
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

  /**
   * This contructor creates a PropertyEditor for the list of 
   * properties in the properties List, using the template
   * types in the templates List.  Note that there should be
   * one entry in each of the properties List and the templates
   * List for each property to be edited.
   */     
  public PropertyEditorPane(PropertyEditorManager newManager, 
			    List properties, List templates,
			    Container newContainer) {
    super(BoxLayout.Y_AXIS);
    
    manager = newManager;
    container = newContainer;
    
    editor = (SwingPropertyEditor) manager.createEditor(properties, templates);

    this.add(editor);
    
    this.addButtons();
  }
  
  /**
   * This contructor creates a PropertyEditor using the given 
   * SwingPropertyEditor.
   */     
  public PropertyEditorPane(PropertyEditorManager newManager, 
			    SwingPropertyEditor newEditor,
			    Container newContainer) {
    super(BoxLayout.Y_AXIS);
    
    manager = newManager;
    container = newContainer;
    
    editor = newEditor;
    
    this.add(newEditor);
    
    this.addButtons();
  }

  /**
   * Accepts the changes for the edited properties, and writes them to
   * the PropertyEditorManager.
   */
  public void setValue() throws PropertyValueVetoException {
    editor.setValue();
  }
 
  /**
   * Gets the currently selected values for the edited properties.
   */
  public java.util.Properties getValue() {
    return editor.getValue();
  }
  
  /**
   * Resets the original values for the edited properties.
   */
  public void resetDefaultValue() {
    editor.resetDefaultValue();
  }
  
  /**
   * Adds the appropriate buttons (Ok, Accept, Cancel) to this component.
   */
  public void addButtons() {
    Box buttonBox = new Box(BoxLayout.X_AXIS);
    
    buttonBox.add(createButton("Ok", new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  try {
	    setValue();
	    manager.commit();
	    if (container instanceof JInternalFrame) {
	      try {
		((JInternalFrame)container).setClosed(true);
	      } catch (java.beans.PropertyVetoException pve) {
	      }
	    } else if (container instanceof JFrame) {
	      ((JFrame)container).dispose();
	    }
	  } catch (PropertyValueVetoException pvve) {
	    manager.getFactory().showError(PropertyEditorPane.this, "Error changing value " + pvve.getProperty() + " to " + pvve.getRejectedValue() + ":  " + pvve.getReason());
	  }
	}
      }, true));
    
    buttonBox.add(createButton("Apply", new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  try {
	    setValue();
	    manager.commit();
	  } catch (PropertyValueVetoException pvve) {
	    manager.getFactory().showError(PropertyEditorPane.this, "Error changing value " + pvve.getProperty() + " to " + pvve.getRejectedValue() + ":  " + pvve.getReason());
	  }
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
  
  /**
   * Creates the appropriate Button.
   */
  private JButton createButton(String label, Action e, boolean isDefault) {
    JButton thisButton;
    
    thisButton = new JButton(manager.getProperty("label." + label, label));
    try {
      thisButton.setMnemonic(manager.getProperty("label." + label + ".mnemonic", "").charAt(0));
    } catch (java.util.MissingResourceException mre) {
    }
    
    thisButton.setSelected(isDefault);
    
    thisButton.addActionListener(e);
    
    return thisButton;
  }
  
}
