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
    manager.getFactory().showNewEditorWindow("Add property", v);
  }

  //  as defined in net.suberic.util.gui.PropertyEditorUI
  
  public void setValue() {
    if (isEnabled() && isChanged())
      manager.setProperty(property, (String)labelToValueMap.get(inputField.getSelectedItem()));
  }
  
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property, (String)labelToValueMap.get(inputField.getSelectedItem()));
    
    return retProps;
  }
  
  public void resetDefaultValue() {
    inputField.setSelectedIndex(originalIndex);
  }
  
  public boolean isChanged() {
    return (!(originalIndex == inputField.getSelectedIndex()));
  }
  
  public void setEnabled(boolean newValue) {
    if (inputField != null) {
      inputField.setEnabled(newValue);
      enabled=newValue;
    }
  }

}
