package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;

/**
 * An EditorPane which allows a user to select from a list of choices.
 */
public class ListEditorPane extends SwingPropertyEditor {
  int originalIndex;
  JLabel label;
  JComboBox inputField;
  HashMap labelToValueMap = new HashMap();
  int currentIndex;

  /**
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
    
    label = createLabel();

    inputField = createComboBox();
    
    Box inputBox = new Box(BoxLayout.X_AXIS);
    inputField.setPreferredSize(inputField.getMinimumSize());
    inputField.setMaximumSize(inputField.getMinimumSize());
    inputBox.add(inputField);
    inputBox.add(Box.createGlue());
    if (manager.getProperty(editorTemplate + "._includeAddButton", "false").equalsIgnoreCase("true")) {
      JButton addButton = createAddButton();
      inputBox.add(addButton);
    }

    this.add(label);
    this.add(inputBox);
    this.setEnabled(isEnabled);
    
    labelComponent = label;
    valueComponent = inputBox;
    //valueComponent.setLayout(new FlowLayout(FlowLayout.LEFT));
    //valueComponent.add(inputField);
    manager.registerPropertyEditor(property, this);
  }
  

  /**
   * Creates the JComboBox with the appropriate options.
   */
  private JComboBox createComboBox() {
    String originalValue = manager.getProperty(property, "");
    String currentItem;
    originalIndex=-1;
    Vector items = new Vector();
    StringTokenizer tokens;
    
    if (manager.getProperty(manager.getProperty(editorTemplate + ".allowedValues", ""), "") != "")
      tokens = new StringTokenizer(manager.getProperty(manager.getProperty(editorTemplate + ".allowedValues", ""), ""), ":");
    else
      tokens = new StringTokenizer(manager.getProperty(editorTemplate + ".allowedValues", ""), ":");
    
    for (int i=0; tokens.hasMoreTokens(); i++) {
      currentItem = tokens.nextToken();
      
      String itemLabel = manager.getProperty(editorTemplate + ".listMapping." + currentItem.toString() + ".label", "");
      if (itemLabel.equals(""))
	itemLabel = currentItem.toString();
      
      String itemValue = manager.getProperty(editorTemplate + ".listMapping." + currentItem.toString() + ".value", "");
      if (itemValue.equals(""))
	itemValue = currentItem.toString();
      
      if (itemValue.equals(originalValue)) {
	originalIndex=i;
	currentIndex=i;
      }
      items.add(itemLabel);
      labelToValueMap.put(itemLabel, itemValue);
    }
    
    if (originalIndex == -1) {
      items.add(originalValue);
      labelToValueMap.put(originalValue, originalValue);
      originalIndex = items.size() - 1;
    }
      
    JComboBox jcb = new JComboBox(items);
    jcb.setSelectedIndex(originalIndex);

    jcb.addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  int newIndex = inputField.getSelectedIndex();
	  if (newIndex != currentIndex) {
	    String newValue = (String)labelToValueMap.get(inputField.getSelectedItem());
	    try {
	      firePropertyChangingEvent(newValue);
	      firePropertyChangedEvent(newValue);
	      currentIndex = newIndex;
	    } catch (PropertyValueVetoException pvve) {
	      manager.getFactory().showError(inputField, "Error changing value " + label.getText() + " to " + newValue + ":  " + pvve.getReason());
	      inputField.setSelectedIndex(currentIndex);
	    } 
	  }
	}
      });

    return jcb;
  }
  
  /**
   * Updates the combo box with the new value(s).
   */
  private void updateComboBox(String newValue) {
    Vector items = new Vector();
    StringTokenizer tokens = new StringTokenizer(newValue, ":");
    String currentValue = (String) inputField.getSelectedItem();

    String currentItem;
    for (int i=0; tokens.hasMoreTokens(); i++) {
      currentItem = tokens.nextToken();
      
      String itemLabel = manager.getProperty(editorTemplate + ".listMapping." + currentItem.toString() + ".label", "");
      if (itemLabel.equals(""))
	itemLabel = currentItem.toString();
      
      String itemValue = manager.getProperty(editorTemplate + ".listMapping." + currentItem.toString() + ".value", "");
      if (itemValue.equals(""))
	itemValue = currentItem.toString();
      
      if (itemValue.equals(originalValue)) {
	originalIndex=i;
      }
      if (itemValue.equals(currentValue)) {
	currentIndex=i;
      }
      items.add(itemLabel);
      labelToValueMap.put(itemLabel, itemValue);
    }

    ComboBoxModel newModel = new DefaultComboBoxModel(items);
    newModel.setSelectedItem(currentValue);
    inputField.setModel(newModel);

  }

  /**
   * Creates a button to add a new value to the List.
   */
  public JButton createAddButton() {
    JButton returnValue = new JButton("Add");
    returnValue.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  addNewEntry();
	}
      });
    
    return returnValue;
  }

  /**
   * Opens up an editor to add a new Item to the List.
   */
  public void addNewEntry() {
    String editedProperty = manager.getProperty(editorTemplate + ".allowedValues", "");
    Vector v = new Vector();
    v.add(editedProperty);
    manager.getFactory().showNewEditorWindow("Add property", v, v, manager);
  }

  //  as defined in net.suberic.util.gui.PropertyEditorUI
  
  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source VariableBundle.
   */
  public void setValue() {
    int newIndex = inputField.getSelectedIndex();
    String currentValue = (String)labelToValueMap.get(inputField.getSelectedItem());
    try {
      if (newIndex != currentIndex) {
	firePropertyChangingEvent(currentValue);
	firePropertyChangedEvent(currentValue);
	currentIndex = newIndex;
      }
      if (isEnabled() && isChanged()) { 
	manager.setProperty(property, currentValue);
      }
    } catch (PropertyValueVetoException pvve) {
      manager.getFactory().showError(inputField, "Error changing value " + label.getText() + " to " + currentValue + ":  " + pvve.getReason());
      inputField.setSelectedIndex(currentIndex);
    } 
  }
    
  /**
   * Returns the current values of the edited properties as a 
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property, (String)labelToValueMap.get(inputField.getSelectedItem()));
    
    return retProps;
  }
  
  /**
   * This resets the editor to the original (or latest set, if setValue() 
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {
    // this will be handled by the ItemListener we have on the inputField,
    // so we don't have to notify listeners here.
    inputField.setSelectedIndex(originalIndex);
  }
  
  /**
   * Returns whether or not the current list selection has changed from
   * the last save.
   */
  public boolean isChanged() {
    return (!(originalIndex == inputField.getSelectedIndex()));
  }
  
  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled 
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    if (inputField != null) {
      inputField.setEnabled(newValue);
      enabled=newValue;
    }
  }

  /**
   * This listens to the property that it currently providing the list
   * of allowed values for this List.  If it changes, then the allowed
   * values list also is updated.
   */
  public class ListEditorListener extends PropertyEditorAdapter {
    
    /**
     * Called after a property changes.
     */
    public void propertyChanged(String newValue) {
      updateComboBox(newValue);
    }
    
  }
}
