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
 */

public class MultiEditorPane extends DefaultPropertyEditor implements ListSelectionListener {
    String property;
    VariableBundle sourceBundle;
    PropertyEditorFactory factory;
    JList optionList;
    JPanel entryPanel;
    boolean changed = false;
    Vector removeValues = new Vector();
    DefaultListModel optionListModel;
    Vector subProperties;
    Vector templateTypes;

    String originalValue;
    Hashtable originalPanels = new Hashtable();
    Hashtable currentPanels = new Hashtable();

    public MultiEditorPane(String newProperty, PropertyEditorFactory newFactory, boolean isEnabled) {
	JLabel label;
	property=newProperty;
	factory = newFactory;
	sourceBundle=factory.getBundle();
	originalValue = sourceBundle.getProperty(property, "");

	// set the default label.

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	label = new JLabel(sourceBundle.getProperty(property + ".label", defaultLabel));

	// create the current list of options

	Vector optionVector = createEditedList(originalValue);
	
	optionListModel = new DefaultListModel();

	for (int i = 0; i < optionVector.size(); i++)
	    optionListModel.addElement(optionVector.elementAt(i));
	
	// and, using this list, create the actual list.

	optionList = new JList(optionListModel);

	this.add(createOptionBox(label, optionList));

	// first create the subproperties and templateTypes lists
	subProperties = new Vector();
	templateTypes = new Vector();
	StringTokenizer subPropertiesTok  = new StringTokenizer(sourceBundle.getProperty(property + ".editableFields", ""), ":");

	while (subPropertiesTok.hasMoreElements()) {
	    String current = subPropertiesTok.nextToken();
	    subProperties.add(current);
	    templateTypes.add(property + "." + current);
	}
	
	// create entryPanels (the panels which show the subproperties
	// of each item in the optionList) for each option.

	entryPanel = createEntryPanel(optionVector, true);
	this.add(entryPanel);
	this.setEnabled(isEnabled);
    }

    public MultiEditorPane(String newProperty,
                           PropertyEditorFactory newFactory) {
	this(newProperty, newFactory, true);
    }
    private Vector createEditedList(String origValue) {
	Vector items = new Vector();
	StringTokenizer tokens;
	
	tokens = new StringTokenizer(origValue, ":");
	
	for (int i=0; tokens.hasMoreTokens(); i++) {
	    items.add(tokens.nextToken());
	}
	return items;
    }	      

    private Box createOptionBox(JLabel label, JList itemList) {
	Box optBox = new Box(BoxLayout.Y_AXIS);
	optBox.add(label);

	optionList.addListSelectionListener(this);
	JScrollPane listScrollPane = new JScrollPane(optionList);
	optBox.add(listScrollPane);
	optBox.add(createButtonBox());
	
	return optBox;
    }

    /**
     * This creates a panel for each option.  It uses a CardLayout.
     */

    private JPanel createEntryPanel (Vector itemList, boolean original) {
	JPanel entryPanel = new JPanel(new CardLayout());
	PropertyEditorPane pep;

	String rootProp;
	Vector propList;
	Vector templateList;
	
	for (int i = 0; i < itemList.size(); i++) {
	    rootProp = new String(property + "." + (String)(itemList.elementAt(i)));
	    propList = createPropertiesList(rootProp, subProperties);
	    
	    pep = new PropertyEditorPane(factory, propList, templateTypes, null);;
	    
	    if (original == true) {
		originalPanels.put(itemList.elementAt(i), pep);
	    }

	    //	    pep.setEnabled(false);

            // save reference to new pane in hash table
	    currentPanels.put(itemList.elementAt(i), pep);

	    entryPanel.add((String)itemList.elementAt(i), pep);

	}
	
	// create the default 

	int i = itemList.size();

	rootProp = new String(property + ".default");
	    
	propList = createPropertiesList(rootProp, subProperties);

	pep = new PropertyEditorPane(factory, propList, templateTypes, null);;
	    
	if (original == true) {
	    originalPanels.put("default", pep);
	}

	currentPanels.put("default", pep);

	entryPanel.add("default", pep);
	    
	return entryPanel;
    }

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

	buttonBox.add(createButton("Rename", new AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent e) {
		editSelectedValue();
	    }
	}, false));

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


    private Vector createPropertiesList(String rootProperty, Vector subProps) {
	Vector editedProperties = new Vector();

	for (int i = 0; i < subProps.size(); i++) 
	    editedProperties.add(rootProperty + "." + (String)subProps.elementAt(i));

	return editedProperties;
    }

    public void valueChanged(ListSelectionEvent e) {
	CardLayout entryLayout = (CardLayout)entryPanel.getLayout();

	String selectedId = (String)((JList)e.getSource()).getSelectedValue();

	if (selectedId != null)
	    entryLayout.show(entryPanel, selectedId);
	  
    }

    public void addNewValue(String newValueName) {
	if (newValueName == null || newValueName.length() == 0)
	    return;
	
	String rootProp =new String(property.concat("." + newValueName));
	Vector propList = createPropertiesList(rootProp, subProperties);
	
	PropertyEditorPane pep = new PropertyEditorPane(factory, propList, null);;
	optionListModel.addElement(newValueName);

	entryPanel.add(newValueName, pep);
	
	getOptionList().setSelectedValue(newValueName, true);
	
	this.setChanged(true);
    }

    public void removeSelectedValue() {
	String selValue = (String)getOptionList().getSelectedValue();
	if (selValue == null)
	    return;

	ListModel lm = getOptionList().getModel();

	if (lm instanceof DefaultListModel)
	    ((DefaultListModel)lm).removeElement(selValue);
	
	String rootProp = new String(property.concat("." + selValue));
	
	removeValues.addAll(createPropertiesList(rootProp, subProperties));
	
	this.setChanged(true);
    }

    public void editSelectedValue() {
    }

    public String getNewValueName() {
	boolean goodValue = false;
	boolean matchFound = false;

	String newName = factory.showInputDialog(this, sourceBundle.getProperty("MultiEditorPane.renameProperty", "Enter new name."));

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

    public void renameProperty(String oldName, String newName) {
	newName = getNewValueName();
	if (newName != null) {
	    PropertyEditorPane oldPane = (PropertyEditorPane)currentPanels.get(oldName);
	    if (oldPane != null) {
		String rootProp =new String(property.concat("." + newName));
		Vector propList = createPropertiesList(rootProp, subProperties);
		
		PropertyEditorPane pep = new PropertyEditorPane(factory, propList, null);;
		java.util.Properties oldProps = oldPane.getValue();
	    }
	}
    }

    public String getStringFromList(JList list) {

	String retVal;
	if (optionListModel.getSize() < 1)
	    return null;
	else 
	    retVal = new String((String)optionListModel.getElementAt(0));
	
	for (int i = 1; i < optionListModel.getSize(); i++) {
	    retVal = retVal.concat(":" + (String)optionListModel.getElementAt(i));
	}

	return retVal;
    }

    public void setValue() {
	if (isEnabled()) {
	    for (int i = 0; i < removeValues.size() ; i++) 
		sourceBundle.removeProperty((String)removeValues.elementAt(i));
	    
	    removeValues = new Vector();
	    
	    java.awt.Component[] components = entryPanel.getComponents();
	    for (int i = 0; i < components.length; i++) {
		((PropertyEditorPane)components[i]).setValue();
	    }
	    
	    if (isChanged())
		sourceBundle.setProperty(property, getStringFromList(getOptionList()));
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
	    ((PropertyEditorPane)components[i]).resetDefaultValue();
	}
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







