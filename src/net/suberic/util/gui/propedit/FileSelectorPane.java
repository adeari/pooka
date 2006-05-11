package net.suberic.util.gui.propedit;
import java.io.*;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This displays the currently selected file (if any), along with a 
 * button which will bring up a JFileChooser to choose any other file(s).
 */

public class FileSelectorPane extends LabelValuePropertyEditor {
  
  JLabel label;
  JTextField valueDisplay;
  JButton inputButton;

  int fileSelection;
  
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
    originalValue = manager.getProperty(property, "");
    String currentValue = parseValue(manager.getProperty(property, ""));
    
    if (debug) {
      System.out.println("property is " + property + "; editorTemplate is " + editorTemplate);
    }

    label = createLabel();

    valueDisplay = new JTextField(currentValue);
    
    inputButton = createInputButton();
    
    valueDisplay.setPreferredSize(new java.awt.Dimension(150 - inputButton.getPreferredSize().width, valueDisplay.getMinimumSize().height));
    
    String selectionType = manager.getProperty(editorTemplate + ".propertyType", "File");
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

    this.add(tmpPanel);
    
    this.setEnabled(isEnabled);
    
    manager.registerPropertyEditor(property, this);
  }
  
  /**
   * Creates a button that will bring up a way to select a new File.
   */
  public JButton createInputButton() {
    try {
      java.net.URL url = this.getClass().getResource(manager.getProperty("FileSelectorPane.inputButton.image", "/net/suberic/util/gui/images/More.gif"));
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
		     manager.getProperty("FolderEditorPane.Select",
					 "Select"));

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File returnFile = jfc.getSelectedFile();
      String newValue = returnFile.getAbsolutePath();
      
      try {
	firePropertyChangingEvent(newValue);
	firePropertyChangedEvent(newValue);

	valueDisplay.setText(newValue);
	
      } catch (PropertyValueVetoException pvve) {
	manager.getFactory().showError(valueDisplay, "Error changing value " + label.getText() + " to " + newValue + ":  " + pvve.getReason());
      }
    }
    
  }
  
  //  as defined in net.suberic.util.gui.PropertyEditorUI
  
  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source PropertyEditorManager.
   */
  public void setValue() {
    if (isEnabled() && isChanged())
      manager.setProperty(property, (String)valueDisplay.getText());
  }
  
  /**
   * Returns the current values of the edited properties as a 
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property, (String)valueDisplay.getText());
    
    return retProps;
  }
  
  /**
   * This resets the editor to the original (or latest set, if setValue() 
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {
    valueDisplay.setText(originalValue);
  }

  /**
   * Returns whether or not this editor has its original value.
   */
  public boolean isChanged() {
    return (!(originalValue.equals(valueDisplay.getText())));
  }
  
  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled 
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    if (inputButton != null) {
      inputButton.setEnabled(newValue);
      enabled=newValue;
    }
  }

  /**
   * Parses any ${} special values out of the string.
   */
   public String parseValue(String origString) {
     StringBuffer newValue = new StringBuffer(origString);
     int nextVar = origString.indexOf("${");
     int offset = 0;
     while (nextVar >= 0) {
       int end = origString.indexOf("}", nextVar);
       if (end >= nextVar) {
	 String variable = origString.substring(nextVar +2, end);

	 String replaceValue = System.getProperty(variable);
	 if (replaceValue == null)
	   replaceValue = "";
	 newValue.replace(nextVar + offset, end + 1 + offset, replaceValue);

	 offset = offset - end + nextVar + replaceValue.length() - 1;
	 
	 nextVar = origString.indexOf("${", end);
       } else {
	 nextVar = -1;
       }
     }
     return newValue.toString();
   }
}
