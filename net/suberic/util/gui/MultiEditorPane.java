package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.CardLayout;
import javax.swing.event.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.*;

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
public class MultiEditorPane extends DefaultPropertyEditor implements ListSelectionListener {
  /*
 * Foo.editableFields.bar.addSubProperty=false -- shows whether or not to
 *                 add this property's name to the main edited property.
 *
 * If, for instance, Foo.editableFields.baz is a compisite editor (a 
 * CompositeEditorPane or a TabbedEditorPane, for instance), then, by default,
 * all properties under that editor will be scoped by baz.  So if the baz
 * editor has a "zork" property, then the actual edited property will be
 * Foo.fooOne.baz.zork.  If you'd rather have this Composite/Tabbed pane
 * edit top-level properties, then use the ".addSubProperty=false" extension
 * on that property.
 */
  String property;
  VariableBundle sourceBundle;
  PropertyEditorFactory factory;
  JList optionList;
  JPanel entryPanel;
  boolean changed = false;
  Vector removeValues = new Vector();
  DefaultListModel optionListModel;
  Vector templateTypes;
  Box optionBox;
  
  String template;
  
  String originalValue;
  Hashtable originalPanels = new Hashtable();
  Hashtable currentPanels = new Hashtable();
  
  /**
   * Creates a new MultiEditorPane from the given properties.
   */
    public MultiEditorPane(String newProperty, PropertyEditorFactory newFactory, boolean isEnabled) {
      this.configureEditor(newFactory, newProperty, newProperty, newFactory.getBundle(), isEnabled);
    }
  
  /**
   * Creates a new MultiEditorPane from the given properties.
   */
  public MultiEditorPane(String newProperty, String typeTemplate, PropertyEditorFactory newFactory, boolean isEnabled) {
    this.configureEditor(newFactory, newProperty, typeTemplate, newFactory.getBundle(), isEnabled);
  }
  
  /**
   * Creates a new MultiEditorPane from the given properties.
   */
  public MultiEditorPane(String newProperty, String typeTemplate, PropertyEditorFactory newFactory) {
    this.configureEditor(newFactory, newProperty, typeTemplate, newFactory.getBundle(), true);
  }
  
  /**
   * Creates a new MultiEditorPane from the given properties.
   */
  public MultiEditorPane(String newProperty,
			 PropertyEditorFactory newFactory) {
    this(newProperty, newFactory, true);
  }
  
  /**
   * Configures the MultiEditorPane using the given properties.
   */
  public void configureEditor(PropertyEditorFactory newFactory, String propertyName, String newTemplate, VariableBundle bundle, boolean isEnabled) { 
    JLabel label;
    property=propertyName;
    factory = newFactory;
    sourceBundle=bundle;
    originalValue = sourceBundle.getProperty(property, "");
    
    // set the default label.
    
    String defaultLabel;
    int dotIndex = property.lastIndexOf(".");
    if (dotIndex == -1) 
      defaultLabel = new String(property);
    else
      defaultLabel = property.substring(dotIndex+1);
    
    label = new JLabel(sourceBundle.getProperty(newTemplate + ".label", defaultLabel));
    
    // create the current list of edited items.  so if this is a User list,
    // these values might be 'allen', 'deborah', 'marc', 'jessica', etc.
    
    Vector optionVector = createEditedList(originalValue);
    
    optionListModel = new DefaultListModel();
    
    for (int i = 0; i < optionVector.size(); i++) {
      optionListModel.addElement(optionVector.elementAt(i));
    }
    
    optionList = new JList(optionListModel);
    
    optionBox = createOptionBox(label, optionList);
    this.add(optionBox);
    
    // now create the list of subproperties to be edited.
    
    template = newTemplate + ".editableFields";
    
    // create entryPanels (the panels which show the subproperties
    // of each item in the optionList) for each option.
    
    entryPanel = createEntryPanel(optionVector, true);
    this.add(entryPanel);
    
    labelComponent = optionBox;
    valueComponent = entryPanel;
    
    this.setEnabled(isEnabled);
  }
  
  /**
   * Creates the list of edited items.
   */
  private Vector createEditedList(String origValue) {
    Vector items = new Vector();
    StringTokenizer tokens;
    
    tokens = new StringTokenizer(origValue, ":");
    
    for (int i=0; tokens.hasMoreTokens(); i++) {
      items.add(tokens.nextToken());
    }
    return items;
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
    
    if (! sourceBundle.getProperty(property + "._fixed", "false").equalsIgnoreCase("true"))
      optBox.add(createButtonBox());
    
    return optBox;
  }
  
  /**
   * This creates a panel for each option.  It uses a CardLayout.
   *
   * Note that this is also the section of code which determines which 
   * subproperties are to be edited.
   */
  private JPanel createEntryPanel (Vector itemList, boolean original) {
    JPanel entryPanel = new JPanel(new CardLayout());
    
    CompositeEditorPane pep;
    
    String rootProp;
    Vector propList;
    Vector templateList;
    
    /*
    for (int i = 0; i < itemList.size(); i++) {
      rootProp = new String(property + "." + (String)(itemList.elementAt(i)));
      
      pep = new CompositeEditorPane(factory, rootProp, template);;
      
      if (original == true) {
	originalPanels.put(itemList.elementAt(i), pep);
      }
      
      //	    pep.setEnabled(false);
      
      // save reference to new pane in hash table
      currentPanels.put(itemList.elementAt(i), pep);
      
      entryPanel.add((String)itemList.elementAt(i), pep);
      
    }
    */
    
    // create the default 
    
    int i = itemList.size();
    
    rootProp = new String(property + ".default");
    
    pep = new CompositeEditorPane(factory, rootProp, template, false);
    
    if (original == true) {
      originalPanels.put("___default", pep);
    }
    
    currentPanels.put("___default", pep);
    
    entryPanel.add("___default", pep);
    CardLayout entryLayout = (CardLayout)entryPanel.getLayout();
    entryLayout.show(entryPanel, "___default");
    
    return entryPanel;
  }
  
  /**
   * Creates the box which holds the "Add" and "Remove" buttons.
   */
  private Box createButtonBox() {
    Box buttonBox = new Box(BoxLayout.X_AXIS);
    
    buttonBox.add(createButton("Add", new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  addNewValue(getNewValueName());
	}
      }, true));
    
    buttonBox.add(createButton("Remove", new AbstractAction() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  removeSelectedValue();
	}
      }, false));
    
    /*
      buttonBox.add(createButton("Rename", new AbstractAction() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
      editSelectedValue();
      }
      }, false));
    */
    
    return buttonBox;
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
  
  
  /**
   * Called when the selected value changed.  should result in the 
   * entryPane changing.
   */
  public void valueChanged(ListSelectionEvent e) {
    CardLayout entryLayout = (CardLayout)entryPanel.getLayout();
    
    String selectedId = (String)((JList)e.getSource()).getSelectedValue();
    
    if (selectedId != null) {
      Object newSelected = currentPanels.get(selectedId);
      if (newSelected == null) {
	String rootProp = new String(property + "." + selectedId);
	
	CompositeEditorPane pep = new CompositeEditorPane(factory, rootProp, template);;

	/*
	if (original == true) {
	  originalPanels.put(itemList.elementAt(i), pep);
	}
      
	//	    pep.setEnabled(false);
	*/

	// save reference to new pane in hash table
	currentPanels.put(selectedId, pep);
	
	entryPanel.add(selectedId, pep);

      }
      entryLayout.show(entryPanel, selectedId);
    } else
      entryLayout.show(entryPanel, "___default");
    
  }
  
  public void addNewValue(String newValueName) {
    if (newValueName == null || newValueName.length() == 0)
      return;
    
    String rootProp =new String(property.concat("." + newValueName));
    
    CompositeEditorPane pep = new CompositeEditorPane(factory, rootProp, template);;
    optionListModel.addElement(newValueName);
    
    entryPanel.add(newValueName, pep);
    
    getOptionList().setSelectedValue(newValueName, true);
    
    this.setChanged(true);
  }
  
  public void removeSelectedValue() {
    String selValue = (String)getOptionList().getSelectedValue();
    if (selValue == null)
      return;
    
    String rootProp = new String(property.concat("." + selValue));
    PropertyEditorUI removedUI = (PropertyEditorUI) currentPanels.get(selValue);
    if (removedUI != null) {
      java.util.Properties removedProperties = removedUI.getValue();
      java.util.Enumeration keys = removedProperties.keys();
      while (keys.hasMoreElements()) {
	String currentKey = (String) keys.nextElement();
	removeValues.add(currentKey);
      }
      currentPanels.remove(selValue);
    }
    
    ListModel lm = getOptionList().getModel();
    
    if (lm instanceof DefaultListModel)
      ((DefaultListModel)lm).removeElement(selValue);
    
    this.setChanged(true);
  }
  
  public void editSelectedValue() {
  }
  
  public String getNewValueName() {
    boolean goodValue = false;
    boolean matchFound = false;
    
    String newName = null;
    newName = factory.showInputDialog(this, sourceBundle.getProperty("MultiEditorPane.renameProperty", "Enter new name."));
    
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
	  newName = factory.showInputDialog(this, sourceBundle.getProperty("MultiEditorPane.error.duplicateName", "Name already exists:") + "  " + newName + "\n" + sourceBundle.getProperty("MultiEditorPane.renameProperty", "Enter new name."));
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
    newName = getNewValueName();
    if (newName != null) {
      CompositeEditorPane oldPane = (CompositeEditorPane)currentPanels.get(oldName);
      if (oldPane != null) {
	String rootProp =new String(property.concat("." + newName));
	
	CompositeEditorPane pep = new CompositeEditorPane(factory, rootProp, template);;
	java.util.Properties oldProps = oldPane.getValue();
      }
    }
  }
  
  /**
   * This produces a string for the given JList.
   */
  public String getStringFromList(JList list) {
    
    String retVal;
    if (optionListModel.getSize() < 1)
      return "";
    else 
      retVal = new String((String)optionListModel.getElementAt(0));
    
    for (int i = 1; i < optionListModel.getSize(); i++) {
      retVal = retVal.concat(":" + (String)optionListModel.getElementAt(i));
    }
    
    return retVal;
  }
  
  /**
   * Sets the value for this MultiEditorPane.
   */
  public void setValue() {
    if (isEnabled()) {
      for (int i = 0; i < removeValues.size() ; i++) 
	sourceBundle.removeProperty((String)removeValues.elementAt(i));
      
      removeValues = new Vector();
      
      java.awt.Component[] components = entryPanel.getComponents();
      for (int i = 0; i < components.length; i++) {
	((CompositeEditorPane)components[i]).setValue();
      }
      
      if (isChanged()) {
	//System.out.println("setting property.  property is " + property + "; getStringFromList is " + getStringFromList(getOptionList()));
	sourceBundle.setProperty(property, getStringFromList(getOptionList()));
      }
    }
  }
  
  public void resetDefaultValue() {
    
    removeValues = new Vector();
    
    if (isChanged()) {
      optionListModel.removeAllElements();
      entryPanel.removeAll();
      
      java.util.Enumeration en = originalPanels.keys();
      
      while (en.hasMoreElements()) {
	String key = (String)en.nextElement();
	entryPanel.add(key, (JPanel)originalPanels.get(key));
      }
    }
    
    java.awt.Component[] components = entryPanel.getComponents();
    for (int i = 0; i < components.length; i++) {
      ((CompositeEditorPane)components[i]).resetDefaultValue();
    }
  }
  
  public java.util.Properties getValue() {
    java.util.Properties currentRetValue = new java.util.Properties();
    java.util.Collection panels = currentPanels.values();
    java.util.Iterator iter = panels.iterator();
    while (iter.hasNext()) {
      currentRetValue.putAll(((DefaultPropertyEditor)iter.next()).getValue());
    }
    
    return currentRetValue;
  }
  
  public boolean isChanged() {
    return changed;
  }
  
  public void setChanged(boolean newChanged) {
    changed=newChanged;
  }
  
  public JList getOptionList() {
    return optionList;
  }
  
  public JPanel getEntryPanel() {
    return entryPanel;
  }
}







