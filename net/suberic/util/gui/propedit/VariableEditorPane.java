package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;

/**
 * This will made a panel which can change depending on 
 * exact properties which are then edited will depend on the value of 
 * another propery. 
 */
public class VariableEditorPane extends CompositeSwingPropertyEditor {

  String keyProperty;
  HashMap idToEditorMap = new HashMap();
  
  boolean scoped;
  
  /**
   * This configures this editor with the following values.
   *
   * @param propertyName The property to be edited.  
   * @param template The property that will define the layout of the 
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default. 
   */
  public void configureEditor(String propertyName, String template, PropertyEditorManager newManager, boolean isEnabled) {
    property=propertyName;
    manager=newManager;
    editorTemplate = template;

    debug = manager.getProperty("editors.debug", "false").equalsIgnoreCase("true");
    
    enabled=isEnabled;
    
    editors = new Vector();

    String remove = manager.getProperty(editorTemplate + ".removeString", "");
    if (! remove.equals(""))
      property = property.substring(0, property.lastIndexOf(remove));
    
    scoped = manager.getProperty(editorTemplate + ".scoped", "false").equalsIgnoreCase("true");
    if (scoped) {
      keyProperty = property + "." + manager.getProperty(editorTemplate + ".keyProperty", "");
    } else {
      keyProperty =  manager.getProperty(editorTemplate + ".keyProperty", "");
    }

    if (debug) {
      System.out.println("Variable:  property = " + property + "; keyProperty = " + keyProperty);
    }

    manager.addPropertyEditorListener(keyProperty, new PropertyEditorAdapter() {
	public void propertyChanged(PropertyEditorUI ui, String prop, String newValue) {
	  showPanel(newValue);
	}
      });

    valueComponent = new JPanel();
    valueComponent.setLayout(new java.awt.CardLayout());

    String currentValue = manager.getProperty(keyProperty, "");
    showPanel(currentValue);

    manager.registerPropertyEditor(property, this);
  }
  
  /**
   * This shows the editor window for the configured value.
   */
  public void showPanel(String selectedId) {
    boolean enableMe = true;
    boolean resize = false;
    if (selectedId == null || selectedId.equals("")) {
      enableMe = false;
      // check to see the default.
      //String possibleDefault = manager.getProperty(editorTemplate, "");
      //if (idToEditorMap.get(possibleDefault) != null) {
      //selectedId = possibleDefault;
      //}
    }

    CardLayout layout = (CardLayout)valueComponent.getLayout();

    Object newSelected = idToEditorMap.get(selectedId);
    if (newSelected == null) {
      // we'll have to make a new window.
      if (selectedId == null || selectedId.equals("")) {
	JPanel jp = new JPanel();
	valueComponent.add(selectedId, jp);
      } else {

	SwingPropertyEditor spe = createEditorPane(selectedId);
	
	// save reference to new pane in hash table
	idToEditorMap.put(selectedId, spe);
	editors.add(spe);
	
	spe.setEnabled(enableMe && enabled);
	valueComponent.add(selectedId, spe);
	resize = true;
      }
    }
    layout.show(valueComponent, selectedId);
    if (resize) {
      doResize();
    }
  }    
 
  /**
   * Creates a SwingPropertyEditor for the given subproperty.
   */
  public SwingPropertyEditor createEditorPane(String selectedId) {
    
    String editValue = selectedId;

    if (scoped) {
      editValue = editorTemplate + "." + selectedId;
      if (debug) {
	System.out.println("scoped; editValue = " + editValue);
      }
    } else {
      if (debug) 
	System.out.println("not scoped; editValue = " + editValue);
    }
    
    SwingPropertyEditor returnValue = (SwingPropertyEditor)manager.createEditor(property, editValue);
    return returnValue;
  }
}



