package net.suberic.util.gui;
import net.suberic.util.gui.*;
import net.suberic.util.VariableBundle;
import java.io.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import net.suberic.util.swing.JFontChooser;
import java.awt.Font;

/**
 * This displays the currently selected file (if any), along with a 
 * button which will bring up a FontChooser to choose any other file(s).
 */

public class FontSelectorPane extends DefaultPropertyEditor {
  
  String property;
  String propertyTemplate;
  String origValue;
  JLabel label;
  JTextField valueDisplay;
  JButton inputButton;
  VariableBundle sourceBundle;
  
  /**
   * This creates a new FontSelectorPane.
   */
  public FontSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
    configureEditor(null, newProperty, typeTemplate, bundle, isEnabled);
  }
  
  /**
   * This creates a new FontSelectorPane.
   */
  public FontSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle) {
    this(newProperty, typeTemplate, bundle, true);
  }
  
  /**
   * This creates a new FontSelectorPane.
   */
  public FontSelectorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
    this(newProperty, newProperty, bundle, isEnabled);
  }
  
  /**
   * This creates a new FontSelectorPane which is enabled.
   */
  public FontSelectorPane(String newProperty, VariableBundle bundle) {
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
   * Creates a button that will bring up a way to select a new Font.
   */
  public JButton createInputButton() {
    try {
      java.net.URL url = this.getClass().getResource(sourceBundle.getProperty("FontSelectorPane.inputButton.image", "images/More.gif"));
      if (url != null) {
	ImageIcon icon = new ImageIcon(url);
	
	JButton newButton = new JButton(icon);
	
	newButton.setPreferredSize(new java.awt.Dimension(icon.getIconHeight(), icon.getIconWidth()));
	newButton.addActionListener(new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	      selectNewFont();
	    }
	  });
	
	return newButton;
      }
    } catch (java.util.MissingResourceException mre) {
    }
    
    JButton newButton = new JButton();
    newButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  selectNewFont();
	}
      });
    
    return newButton;
  }
  
  /**
   * This actually brings up a FontChooser to select a new Font for 
   * the value of the property.
   */
  public void selectNewFont() {
    String fontText = valueDisplay.getText();
    Font f = null;
    if (fontText != null && fontText.length() > 0) {
      f = Font.decode(fontText);
    }

    String newFontText = JFontChooser.showStringDialog(this,
			    sourceBundle.getProperty("FontEditorPane.Select",
						     "Select"), f);

    //System.err.println("new font text = " + newFontText);
    if (newFontText != null)
      valueDisplay.setText(newFontText);
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
