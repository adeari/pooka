package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.CardLayout;
import javax.swing.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * This class will make an editor for a list of elements, where each of 
 * the elements has a set of subproperties.  
 *
 * Configuration is as follows:
 *
 * Foo.propertyType=Multi  --  shows this is a property editor for an
 *                             attribute with multiple values.
 *
 * Foo.editableFields=bar:baz -- shows which subfields are to be edited
 *
 * 
 * So if your Foo property equals "fooOne:fooTwo", then you'll end up with
 * a MultiPropertyEditor that has an entry for fooOne and fooTwo, along with
 * ways to add and delete these properties.
 *
 * If your Foo.editableFields=bar:baz, then your editor screen for, say,
 * fooOne will have two entries, one for Foo.fooOne.bar, and the other for
 * Foo.fooOne.baz.  These editors will use Foo.editableFields.bar and
 * Foo.editableFields.baz for templates.
 *
 */

public class MultiEditorPane extends CompositeSwingPropertyEditor implements ListSelectionListener {
  JTable optionTable;
  JPanel entryPanel;
  JPanel buttonPanel;
  JLabel label;

  List buttonList;
  boolean changed = false;
  List<String> removeValues = new ArrayList<String>();
  String propertyTemplate;

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
    getLogger().fine("creating MultiEditorPane for property " + propertyName + ", template " + template);
    property=propertyName;
    manager=newManager;
    editorTemplate = template;
    originalValue = manager.getProperty(property, "");

    // set the default label.
    
    // create the current list of edited items.  so if this is a User list,
    // these values might be 'allen', 'deborah', 'marc', 'jessica', etc.
    
    List<String> optionList = manager.getPropertyAsList(property, "");

    List<String> displayProperties = manager.getPropertyAsList(editorTemplate + ".displayProperties", "");

    optionTable = createOptionTable(optionList, displayProperties);
    
    buttonPanel = createButtonPanel();
    
    this.setEnabled(isEnabled);
    
    manager.registerPropertyEditor(property, this);

    Box mainBox = new Box(BoxLayout.X_AXIS);
    mainBox.add(optionTable);
    mainBox.add(buttonPanel);

    this.add(mainBox);

    getLogger().fine("MultiEditorPane for property " + propertyName + ", template " + template);
    /*
    // create entryPanels (the panels which show the subproperties
    // of each item in the optionList) for each option.
    entryPanel = createEntryPanel(optionVector, true);
    
    if (manager.getProperty(template + "._useScrollPane", "false").equalsIgnoreCase("true")) {
    JScrollPane jsp = new JScrollPane(entryPanel);
    java.awt.Dimension size = jsp.getPreferredSize();
    size.height = Math.min(size.height, 300);
    size.width = Math.min(size.width, 475);
    jsp.setPreferredSize(size);
    this.add(jsp);
    valueComponent = jsp;
    } else {
    this.add(entryPanel);
    valueComponent = entryPanel;
    }
    
    labelComponent = optionBox;
    
    this.setEnabled(isEnabled);

    manager.registerPropertyEditor(property, this);
    */
  }
  
  /**
   * Creates the Option Table.  This is a JTable that lists the various
   * items that have been created.
   */
  private JTable createOptionTable(List<String> optionList, List<String> displayProperties) {
    // first get the display properties and their labels.
    Vector columnLabels = new Vector();
    // first one is always the id.
    columnLabels.add(manager.getProperty(editorTemplate + ".Label", editorTemplate));
    for (String subProperty: displayProperties) {
      getLogger().fine("adding label for " + subProperty);

      String label = manager.getProperty(editorTemplate + "." + subProperty + ".Label", subProperty);
      columnLabels.add(label);
    }
    
    DefaultTableModel dtm = new DefaultTableModel(columnLabels, 0);
    
    // now add the properties.
    
    for (String option: optionList) {
      Vector optionValues = new Vector();
      // first one is always the id, at least for now.
      optionValues.add(option);
      for (String subProperty: displayProperties) {
        getLogger().fine("adding display property for " + option + "." + subProperty);
        optionValues.add(manager.getProperty(property + "." + option + "." + subProperty, subProperty));
      }
      dtm.addRow(optionValues);
    }
    
    JTable returnValue = new JTable(dtm);
    returnValue.setCellSelectionEnabled(false);
    returnValue.setColumnSelectionAllowed(false);
    returnValue.setRowSelectionAllowed(true);
    
    returnValue.getSelectionModel().addListSelectionListener(this);
    return returnValue;
  }
  
  /**
   * Creates the box which holds the "Add", "Edit", and "Remove" buttons.
   */
  private JPanel createButtonPanel() {
    getLogger().fine("creating buttons.");
    buttonList = new ArrayList();
    JPanel returnValue = new JPanel();
    Box buttonBox = new Box(BoxLayout.Y_AXIS);
    
    buttonBox.add(createButton("Add", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          addNewValue(getNewValueName());
        }
      }, true));
    
    buttonBox.add(createButton("Edit", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          editSelectedValue();
        }
      }, true));
    
    buttonBox.add(createButton("Remove", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          removeSelectedValue();
        }
      }, false));
    
    returnValue.add(buttonBox);
    return returnValue;
  }
  
  /**
   * Creates a Button for the ButtonBox with the appropriate label and
   * Action.
   */
  private JButton createButton(String label, Action e, boolean isDefault) {
    JButton thisButton;
    
    thisButton = new JButton(manager.getProperty("label." + label, label));
    String mnemonic = manager.getProperty("label." + label + ".mnemonic", "");
    if (!mnemonic.equals(""))
      thisButton.setMnemonic(mnemonic.charAt(0));
    
    thisButton.setSelected(isDefault);
    
    thisButton.addActionListener(e);
    
    buttonList.add(thisButton);
    return thisButton;
  }
  
  
  /**
   * Called when the selected value changed.  Should result in the 
   * entryPane changing.
   */
  public void valueChanged(ListSelectionEvent e) {

  }
  
  /**
   * Adds a new value to the edited List.
   */
  public void addNewValue(String newValueName) {
    if (newValueName == null || newValueName.length() == 0)
      return;
    
    Vector newValueVector = new Vector();
    newValueVector.add(newValueName);
    ((DefaultTableModel)optionTable.getModel()).addRow(newValueVector);
    optionTable.getSelectionModel().setSelectionInterval(optionTable.getModel().getRowCount(), optionTable.getModel().getRowCount());
    editSelectedValue();
  }
  
  /**
   * Removes the currently selected value from the edited List.
   */
  public void removeSelectedValue() {
    int selectedRow = optionTable.getSelectedRow();
    String selValue = (String) optionTable.getValueAt(selectedRow, 0);
    if (selValue == null)
      return;
    
    try {
      List<String> newValueList = new ArrayList<String>();
      for (int i = 0; i < optionTable.getRowCount(); i++) {
        if (i != selectedRow) {
          newValueList.add((String) optionTable.getValueAt(i, 0));
        }
      }
      String newValue = VariableBundle.convertToString(newValueList);
      firePropertyChangingEvent(newValue) ;
      ((DefaultTableModel)optionTable.getModel()).removeRow(selectedRow);
      firePropertyChangedEvent(newValue);
      
      this.setChanged(true);
    } catch (PropertyValueVetoException pvve) {
      manager.getFactory().showError(this, "Error removing value " + selValue + " from " + label.getText() + ":  " + pvve.getReason());
    }
    
  }
  
  /**
   * Edits the currently selected value.
   */
  public void editSelectedValue() {
    getLogger().fine("calling editSelectedValue().");
    int selectedRow = optionTable.getSelectedRow();
    if (selectedRow != -1) {
      String valueToEdit = (String) optionTable.getValueAt(selectedRow, 0);
      String editProperty = property + "." + valueToEdit;
      getLogger().fine("editing " + editProperty);
      ArrayList<String> propList = new ArrayList<String>();
      ArrayList<String> templateList = new ArrayList<String>();
      List<String> subEditors = manager.getPropertyAsList(editorTemplate + ".editableFields", "");
      for(String subEditor: subEditors) {
        propList.add(editProperty);
        templateList.add(editorTemplate + ".editableFields." + subEditor);
        getLogger().fine("adding " + editorTemplate + ".editableFields." + subEditor + " to the editor list.");
      }
      manager.getFactory().showNewEditorWindow("testing", propList, templateList, manager);
    } else {
      getLogger().fine("editSelectedValue():  no selected value.");
    }

  }
  
  /**
   * Puts up a dialog to get a name for the new value.
   */
  public String getNewValueName() {
    boolean goodValue = false;
    boolean matchFound = false;
    
    String newName = null;
    newName = manager.getFactory().showInputDialog(this, manager.getProperty("MultiEditorPane.renameProperty", "Enter new name."));
    
    while (goodValue == false) {
      matchFound = false;
      if (newName != null) {
	
        for (int i = 0; i < optionTable.getRowCount() && matchFound == false; i++) {
          if (((String)optionTable.getValueAt(i, 0)).equals(newName)) 
            matchFound = true;
	  
        }
	
        if (matchFound == false)
          goodValue = true;
        else
          newName = manager.getFactory().showInputDialog(this, manager.getProperty("MultiEditorPane.error.duplicateName", "Name already exists:") + "  " + newName + "\n" + manager.getProperty("MultiEditorPane.renameProperty", "Enter new name."));
      } else {
        goodValue = true;
      }
    }
    
    return newName;
  }
  
  /**
   * This produces a string for the given JList.
   */
  public String getStringFromList(DefaultListModel dlm) {
    
    String retVal;
    if (dlm.getSize() < 1)
      return "";
    else 
      retVal = new String((String)dlm.getElementAt(0));
    
    for (int i = 1; i < dlm.getSize(); i++) {
      retVal = retVal.concat(":" + (String)dlm.getElementAt(i));
    }
    
    return retVal;
  }
  
  /**
   * Sets the value for this MultiEditorPane.
   */
  public void setValue() throws PropertyValueVetoException {
    if (isEnabled()) {
      
      for (int i = 0; i < removeValues.size() ; i++) 
        manager.removeProperty(removeValues.get(i));
      
      removeValues = new Vector();
      
      super.setValue();
      
      if (isChanged()) {
        getLogger().fine("setting property.  property is " + property + "; value is " + getCurrentValue());
        manager.setProperty(property, getCurrentValue());
      }
    }
  }

  /**
   * Returns the current value from the table.
   */
  public String getCurrentValue() {
    List<String> values = new ArrayList<String>();
    for (int i = 0; i < optionTable.getRowCount(); i++) {
      values.add((String) optionTable.getValueAt(i, 0));
    }
    return VariableBundle.convertToString(values);
  }
  
  /**
   * Resets the default values.
   */
  public void resetDefaultValue() throws PropertyValueVetoException {
    
    //FIXME
    removeValues = new Vector();
    
    if (isChanged()) {
      firePropertyChangedEvent(originalValue);
    }
    
  }
  
  /**
   * Returns the currently edited values as a Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties currentRetValue = super.getValue();
    currentRetValue.setProperty(property, getCurrentValue());
    return currentRetValue;
  }
  
  /**
   * Returns whether or not the top-level edited values of this EditorPane
   * have changed.
   */
  public boolean isChanged() {
    return changed;
  }
  
  /**
   * Sets whether or not the top-level edited values of this EditorPane
   * have changed.
   */
  public void setChanged(boolean newChanged) {
    changed=newChanged;
  }
  
  /**
   * Returns the entryPanel.
   */
  public JPanel getEntryPanel() {
    return entryPanel;
  }

  /**
   * Creates an editor.
   */
  public SwingPropertyEditor createEditorPane(String subProperty, String subTemplate) {
    return (SwingPropertyEditor) manager.getFactory().createEditor(subProperty, subTemplate, "Composite", manager, true);
  }
  
  /**
   * Sets this enabled or disabled.
   */
  public void setEnabled(boolean newValue) {
    for (int i = 0; i < buttonList.size(); i++) {
      ((JButton) buttonList.get(i)).setEnabled(newValue);
    }
    enabled = newValue;
  }

}
