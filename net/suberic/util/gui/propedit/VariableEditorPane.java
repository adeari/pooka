package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This will made a panel which can change depending on 
 * exact properties which are then edited will depend on the value of 
 * another propery. 
 */
public class VariableEditorPane extends CompositeSwingPropertyEditor {

  String keyProperty;
  HashMap valueToTemplateMap = new HashMap();
  
  JButton button;
  
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
    
    List allowedValues = manager.getPropertyAsList(editorTemplate + ".allowedValues", "");
    for (int i = 0; i < allowedValues.size(); i++) {
      String value = (String) allowedValues.get(i);
      String editValue = manager.getProperty(editorTemplate + ".allowedValues." + value,  "");
      valueToTemplateMap.put(value, editValue);
    }
    
    valueComponent = new JPanel();
    valueComponent.setLayout(new java.awt.CardLayout());
  }
  
  /**
   * This shows the editor window for the configured value.
   */
  public void showEditorWindow() {
    PropertyEditorUI parent = getParentPropertyEditor();
    //System.out.println("keyProperty = " + keyProperty);
    String currentValue = manager.getProperty(keyProperty, "");
    
    //System.out.println("currentValue initial = " + keyProperty + ":  " + currentValue);
    if (parent != null) {
      String test = parent.getValue().getProperty(keyProperty);
      //System.out.println("parent.getValue() = ");
      //parent.getValue().list(System.out);
      //System.out.println("parent != null; parent.getValue.getProperty(" + keyProperty + ") = " + test);
      if (test != null)
	currentValue = test;
    }
    
    /*
      String editValue = (String) valueToEditorTemplateMap.get(currentValue);
      if (editValue == null)
      editValue = (String) valueToEditorTemplateMap.get("default");
    */
    
    String editValue = currentValue;
    
    if (scoped) {
      editValue = editorTemplate + "." + editValue;
      //System.out.println("scoped; editValue = " + editValue);
    } else {
      //System.out.println("not scoped; editValue = " + editValue);
    }
    
    Vector propertyVector = new Vector();
    propertyVector.add(property);
    Vector editorTemplateVector = new Vector();
    editorTemplateVector.add(editValue);
    
    manager.getFactory().showNewEditorWindow(manager.getProperty(editValue + ".title", editValue), propertyVector, editorTemplateVector, manager);
  }
  
  /**
   * Returns the parent PropertyEditorUI.
   */
  protected PropertyEditorUI getParentPropertyEditor() {
    java.awt.Container parent = valueComponent.getParent();
    PropertyEditorUI returnValue = null;
    while (returnValue == null && parent != null) {
      if (parent instanceof PropertyEditorUI)
	returnValue = (PropertyEditorUI) parent;
      else
	parent = parent.getParent();
    }
    
    return returnValue;
  }
  
  public void setEnabled(boolean newValue) {
    button.setEnabled(newValue);
    enabled=newValue;
  }
}



