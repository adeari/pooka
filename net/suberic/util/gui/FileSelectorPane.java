package net.suberic.util.gui;
import net.suberic.util.gui.*;
import net.suberic.util.VariableBundle;
import java.io.*;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This displays the currently selected file (if any), along with a 
 * button which will bring up a JFileChooser to choose any other file(s).
 */

public class FileSelectorPane extends DefaultPropertyEditor {
  
  String property;
  String propertyTemplate;
  String origValue;
  JLabel label;
  JTextField valueDisplay;
  JButton inputButton;
  VariableBundle sourceBundle;

  int fileSelection;
  
  /**
   * This creates a new FileSelectorPane.
   */
  public FileSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
    configureEditor(null, newProperty, typeTemplate, bundle, isEnabled);
  }
  
  /**
   * This creates a new FileSelectorPane.
   */
  public FileSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle) {
    this(newProperty, typeTemplate, bundle, true);
  }
  
  /**
   * This creates a new FileSelectorPane.
   */
  public FileSelectorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
    this(newProperty, newProperty, bundle, isEnabled);
  }
  
  /**
   * This creates a new FileSelectorPane which is enabled.
   */
  public FileSelectorPane(String newProperty, VariableBundle bundle) {
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
    
    valueDisplay.setPreferredSize(new java.awt.Dimension(150 - inputButton.getPreferredSize().width, valueDisplay.getMinimumSize().height));
    
    String selectionType = sourceBundle.getProperty(propertyTemplate + ".propertyType", "File");
    if (selectionType.equalsIgnoreCase("Directory")) {
      fileSelection = JFileChooser.DIRECTORIES_ONLY;
    } else {
      fileSelection = JFileChooser.FILES_ONLY;
    }
    
    this.add(label);
    labelComponent = label;
    JPanel tmpPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0,0));
    tmpPanel.add(valueDisplay);
    tmpPanel.add(inputButton);
    tmpPanel.setPreferredSize(new java.awt.Dimension(150, valueDisplay.getMinimumSize().height));
    valueComponent = tmpPanel;
    //this.add(valueDisplay);
    //this.add(inputButton);
    this.add(tmpPanel);
    
    this.setEnabled(isEnabled);
    
  }
  
  /**
   * Creates a button that will bring up a way to select a new File.
   */
  public JButton createInputButton() {
    try {
      java.net.URL url = this.getClass().getResource(sourceBundle.getProperty("FileSelectorPane.inputButton.image", "images/More.gif"));
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
   * This actually brings up a JFileChooser to select a new File for 
   * the value of the property.
   */
  public void selectNewFolder() {
    JFileChooser jfc =
      new JFileChooser((String)valueDisplay.getText());
    jfc.setMultiSelectionEnabled(false);
    jfc.setFileSelectionMode(fileSelection);
    jfc.setFileHidingEnabled(false);

    int returnValue =
      jfc.showDialog(this,
		     sourceBundle.getProperty("FolderEditorPane.Select",
					      "Select"));

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File returnFile = jfc.getSelectedFile();
      valueDisplay.setText(returnFile.getAbsolutePath());
    }
	    
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
