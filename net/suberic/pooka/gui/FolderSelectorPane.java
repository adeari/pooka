package net.suberic.pooka.gui;
import net.suberic.util.gui.*;
import net.suberic.util.VariableBundle;
import net.suberic.pooka.gui.filechooser.*;
import net.suberic.pooka.*;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This displays the currently selected folder (if any), along with a 
 * button which will bring up a 
 */

public class FolderSelectorPane extends DefaultPropertyEditor {

    String property;
    String propertyTemplate;
    String origValue;
    JLabel label;
    JTextField valueDisplay;
    JButton inputButton;
    VariableBundle sourceBundle;
    boolean enabled;

    /**
     * This creates a new FolderSelectorPane.
     */
    public FolderSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
	configureEditor(null, newProperty, typeTemplate, bundle, isEnabled);
    }

    /**
     * This creates a new FolderSelectorPane.
     */
    public FolderSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle) {
	this(newProperty, typeTemplate, bundle, true);
    }

    /**
     * This creates a new FolderSelectorPane.
     */
    public FolderSelectorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	this(newProperty, newProperty, bundle, isEnabled);
    }

    /**
     * This creates a new FolderSelectorPane which is enabled.
     */
    public FolderSelectorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    /**
     * This configures the editor with the appropriate information.
     */
    public void configureEditor(PropertyEditorFactory factory, String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	propertyTemplate = templateType;
	sourceBundle = bundle;

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	origValue = sourceBundle.getProperty(property, "");

	label = new JLabel(sourceBundle.getProperty(propertyTemplate + ".label", defaultLabel));
	valueDisplay = new JTextField(origValue);
	
	inputButton = createInputButton();

	this.add(label);
	this.add(valueDisplay);
	this.add(inputButton);

	this.setEnabled(isEnabled);

    }

    /**
     * Creates a button that will bring up a way to select the
     */
    public JButton createInputButton() {
	try {
	    java.net.URL url = this.getClass().getResource(sourceBundle.getProperty("FolderSelectorPane.inputButton.image", "images/More.gif"));
	    if (url != null) {
		ImageIcon icon = new ImageIcon(url);
	    
		JButton newButton = new JButton(icon);
		newButton.setPreferredSize(new java.awt.Dimension(icon.getIconHeight(), icon.getIconWidth()));
		newButton.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
			    selectNewFolder();
			}
		    });
		
		return newButton;
	    }
	} catch (java.util.MissingResourceException mre) {
	}
	    JButton newButton = new JButton();
	    newButton.addActionListener(new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
			selectNewFolder();
		    }
		});
	    
	    return newButton;
    }
    
    /**
     * This actually brings up a JFileChooser to select a new Folder for 
     * the value of the property.
     */
    public void selectNewFolder() {
	MailFileSystemView mfsv = createFileSystemView();

	String defaultRoot = valueDisplay.getText();
	if (defaultRoot.equals(""))
	    defaultRoot = "/";
	    
	JFileChooser jfc =
	    new JFileChooser(defaultRoot, mfsv);
	    jfc.setMultiSelectionEnabled(false);
	    int returnValue =
		jfc.showDialog(Pooka.getMainPanel(),
			       Pooka.getProperty("FolderEditorPane.Select",
						 "Select"));

	    if (returnValue == JFileChooser.APPROVE_OPTION) {
		net.suberic.pooka.gui.filechooser.FolderFileWrapper wrapper =
		    ((net.suberic.pooka.gui.filechooser.FolderFileWrapper)jfc.getSelectedFile());
		valueDisplay.setText(wrapper.getAbsolutePath());
	    }
	    
    }

    /**
     * Creates the FileSystemView appropriate for this file chooser.  This
     * can either be a view of all the stores and their corresponding 
     * folders, or just the folders of a single store.  This is determined
     * by the 'selectionRoot' subproperty of the edited property's template.
     */
    public MailFileSystemView createFileSystemView() {
	
	MailFileSystemView returnValue = null;
	if (sourceBundle.getProperty(propertyTemplate + ".selectionRoot", "allStores").equals("allStores")) {
	    returnValue = new MailFileSystemView();
	} else {
	    int prefixSize = sourceBundle.getProperty(propertyTemplate + ".namePrefix", "Store.").length();
	    int suffixSize = sourceBundle.getProperty(propertyTemplate + ".nameSuffix", "trashFolder").length();
	    String currentStoreName = property.substring(prefixSize, property.length() - suffixSize);
	    net.suberic.pooka.StoreInfo currentStore = Pooka.getStoreManager().getStoreInfo(currentStoreName);
	    if (currentStore != null) {
		returnValue = new MailFileSystemView(currentStore.getStore());
	    }
	}

	return returnValue;
    }
	
    //  as defined in net.suberic.util.gui.PropertyEditorUI

    public void setValue() {
	if (isEnabled() && isChanged())
	    sourceBundle.setProperty(property, (String)valueDisplay.getText());
    }

    public java.util.Properties getValue() {
	java.util.Properties retProps = new java.util.Properties();

	retProps.setProperty(property, (String)valueDisplay.getText());

	return retProps;
    }

    public void resetDefaultValue() {
	valueDisplay.setText(origValue);
    }

    public boolean isChanged() {
	return (!(origValue.equals(valueDisplay.getText())));
    }

    public void setEnabled(boolean newValue) {
        if (inputButton != null) {
            inputButton.setEnabled(newValue);
            enabled=newValue;
        }
    }

}
