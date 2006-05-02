package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.CardLayout;
import javax.swing.event.*;
import java.util.*;
import javax.swing.*;

/**
 * This class will make an editor for a list of elements, where each 
 * element will be displayed on the left and, by selecting one of these
 * elements, the editor for that item will appear in the panel to the
 * right.
 *
 * Configuration is as follows:
 *
 * Foo.propertyType=Sectioned  --  shows this is a property editor that
 *                                 uses a SectionedEditorPane
 *
 * Foo.editableFields=Foo.bar:Foo.baz -- shows which subfields are to be edited
 * 
 * Foo._default=Foo.bar -- shows that by default, the editor for Foo.bar
 *                         is shown.  If this is not included or blank,
 *                         then no editor is displayed by default.
 *
 * The value for Foo itself is not used.
 *
 * If your Foo.editableFields=Foo.bar:.baz:Frotz.zork, then the values
 * edited will be defined by Foo.bar, Foo.baz, and Frotz.zork.  
 */

public class SectionedEditorPane extends CompositeSwingPropertyEditor implements ListSelectionListener {

  JList optionList;
  JLabel label;
  JPanel entryPanel;
  boolean changed = false;
  DefaultListModel optionListModel;
  List templates;
  Box optionBox;
  
  Hashtable currentPanels = new Hashtable();
  
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
    
    // create the editors list.
    editors = new Vector();

    // set the default label.
    
    label = createLabel();
    
    // create the list of properties to be edited.

    List propertyList = manager.getPropertyAsList(propertyName + ".editableFields", "");
    
    optionList = createOptionList(propertyList, manager);
    
    optionBox = createOptionBox(label, optionList);
    this.add(optionBox);
    
    // create entryPanels (the panels which show the editors for each
    // property in the optionList) for each option.
    
    entryPanel = createEntryPanel(propertyList);
    
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
  }
  
  /**
   * Creates the list of edited items.
   */
  private JList createOptionList(List editedProperties, PropertyEditorManager manager) {

    optionListModel = new DefaultListModel();
  
    Iterator iter = editedProperties.iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      SEPListEntry listEntry = new SEPListEntry(manager.getProperty(key + ".Label", key), null, key);
      optionListModel.addElement(listEntry);
    }
    
    JList returnValue =  new JList(optionListModel);
    returnValue.setCellRenderer(new SEPCellRenderer());
    return returnValue;
  }	      
  
  /**
   * Creates the option box.
   */
  private Box createOptionBox(JLabel label, JList itemList) {
    Box optBox = new Box(BoxLayout.Y_AXIS);
    optBox.add(label);
    
    optionList.addListSelectionListener(this);
    JScrollPane listScrollPane = new JScrollPane(optionList);
    optBox.add(listScrollPane);
    
    return optBox;
  }
  
  /**
   * This creates a panel for each option.  It uses a CardLayout.
   *
   * Note that this is also the section of code which determines which 
   * subproperties are to be edited.
   */
  private JPanel createEntryPanel (List itemList) {
    JPanel entryPanel = new JPanel(new CardLayout());
    
    String rootProp;
    List propList;
    List templateList;
    
    // create the default 
    
    int i = itemList.size();
    
    rootProp = new String(property + "._default");
    
    SwingPropertyEditor pep =  createEditorPane(rootProp, editorTemplate + ".editableFields");
    pep.setEnabled(false);
    
    currentPanels.put("___default", pep);
    editors.add(pep);

    entryPanel.add("___default", pep);
    CardLayout entryLayout = (CardLayout)entryPanel.getLayout();
    entryLayout.show(entryPanel, "___default");
    
    return entryPanel;
  }
  
  /**
   * Called when the selected value changed.  Should result in the 
   * entryPane changing.
   */
  public void valueChanged(ListSelectionEvent e) {
    
    boolean resize = false;
    CardLayout entryLayout = (CardLayout)entryPanel.getLayout();
    
    String selectedId = ((SEPListEntry)((JList)e.getSource()).getSelectedValue()).getKey();
    
    if (selectedId != null) {
      Object newSelected = currentPanels.get(selectedId);
      if (newSelected == null) {
        String rootProp = new String(property + "." + selectedId);
        SwingPropertyEditor sep = createEditorPane(rootProp, editorTemplate + ".editableFields");

        // save reference to new pane in hash table
        currentPanels.put(selectedId, sep);
        editors.add(sep);
	
        entryPanel.add(selectedId, sep);
        resize = true;
      }
      entryLayout.show(entryPanel, selectedId);
    } else
      entryLayout.show(entryPanel, "___default");
    
    if (resize)
      doResize();
  }
  
  /**
   * Edits the currently selected value.
   */
  public void editSelectedValue() {
  }
  
  /**
   * Puts up a dialog to get a name for the new value.
   */
  public String getNewValueName() {
    boolean goodValue = false;
    boolean matchFound = false;
    
    String newName = null;
    newName = manager.getFactory().showInputDialog(this, manager.getProperty("SectionedEditorPane.renameProperty", "Enter new name."));
    
    while (goodValue == false) {
      matchFound = false;
      if (newName != null) {
        for (int i = 0; i < optionListModel.getSize() && matchFound == false; i++) {
          if (((String)optionListModel.getElementAt(i)).equals(newName)) 
            matchFound = true;
	  
        }
	
        if (matchFound == false)
          goodValue = true;
        else
          newName = manager.getFactory().showInputDialog(this, manager.getProperty("SectionedEditorPane.error.duplicateName", "Name already exists:") + "  " + newName + "\n" + manager.getProperty("SectionedEditorPane.renameProperty", "Enter new name."));
      } else {
        goodValue = true;
      }
    }
    
    return newName;
  }
  
  /**
   * This renames the selected property.
   */
  public void renameProperty(String oldName, String newName) {
    /*
      newName = getNewValueName();
      if (newName != null) {
      CompositeEditorPane oldPane = (CompositeEditorPane)currentPanels.get(oldName);
      if (oldPane != null) {
      
      String rootProp =new String(property.concat("." + newName));
	
      CompositeEditorPane pep = new CompositeEditorPane(manager, rootProp, editorTemplate);;
      java.util.Properties oldProps = oldPane.getValue();
      }
      }
    */
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
   * Sets the value for this SectionedEditorPane.
   */
  public void setValue() throws PropertyValueVetoException {
    if (isEnabled()) {
      
      super.setValue();
      
      if (isChanged()) {
        if (debug) {
          System.out.println("setting property.  property is " + property + "; getStringFromList is " + getStringFromList(optionListModel));
        }
        manager.setProperty(property, getStringFromList(optionListModel));
      }
    }
  }
  
  /**
   * Resets the default values.
   */
  public void resetDefaultValue() throws PropertyValueVetoException {
    
    if (isChanged()) {
      firePropertyChangingEvent(originalValue);
      optionListModel.removeAllElements();
      entryPanel.removeAll();
      
      firePropertyChangedEvent(originalValue);
    }
    
    java.awt.Component[] components = entryPanel.getComponents();
    for (int i = 0; i < components.length; i++) {
      ((CompositeEditorPane)components[i]).resetDefaultValue();
    }
  }
  
  /**
   * Returns the currently edited values as a Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties currentRetValue = super.getValue();
    currentRetValue.setProperty(property, getStringFromList(optionListModel));
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
   * Returns the optionList.
   */
  public JList getOptionList() {
    return optionList;
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

    optionList.setEnabled(newValue);

    Object defaultEditor = currentPanels.get("___default");
    for (int i = 0; i < editors.size() ; i++) {
      PropertyEditorUI current = (PropertyEditorUI) editors.get(i);
      if (current != defaultEditor) {
        current.setEnabled(newValue);
      }
    }

    enabled = newValue;
  }
  
  class SEPCellRenderer extends JLabel implements ListCellRenderer {
    
    public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      SEPListEntry sepValue = (SEPListEntry) value;
      String label = sepValue.getLabel();
      this.setText(label);
      this.setIcon(sepValue.getIcon());
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      setOpaque(true);
      return this;
    }
  }

  class SEPListEntry {
    String label;
    Icon icon;
    String key;
    
    public SEPListEntry(String pLabel, Icon pIcon, String pKey) {
      label = pLabel;
      icon = pIcon;
      key = pKey;
    }

    public String getLabel() {
      return label;
    }
    
    public Icon getIcon() {
      return icon;
    }

    public String getKey() {
      return key;
    }
  }

}
