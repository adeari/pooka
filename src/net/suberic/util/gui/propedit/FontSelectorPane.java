package net.suberic.util.gui.propedit;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import net.suberic.util.swing.JFontChooser;
import java.awt.Font;

/**
 * This displays the currently selected file (if any), along with a 
 * button which will bring up a FontChooser to choose any other file(s).
 *
 * If property._enabledBox is set to true, then this also adds a 
 * checkbox to show whether or not to use this property, or just to use
 * the defaults.
 * 
 */

public class FontSelectorPane extends SwingPropertyEditor {
  
  JLabel label;
  JTextField valueDisplay;
  JButton inputButton;

  boolean useEnabledBox = false;
  JCheckBox enabledBox = null;
  boolean origEnabled = false;
  
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

    if (debug) {
      System.out.println("property is " + property + "; editorTemplate is " + editorTemplate);
    }

    label = createLabel();

    valueDisplay = new JTextField(originalValue);
    
    inputButton = createInputButton();
    
    valueDisplay.setPreferredSize(new java.awt.Dimension(150 - inputButton.getPreferredSize().width, valueDisplay.getMinimumSize().height));
    
    this.add(label);
    labelComponent = label;
    JPanel tmpPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0,0));
    tmpPanel.add(valueDisplay);
    tmpPanel.add(inputButton);
    tmpPanel.setPreferredSize(new java.awt.Dimension(150, valueDisplay.getMinimumSize().height));

    useEnabledBox = manager.getProperty(editorTemplate + "._enabledBox", "false").equalsIgnoreCase("true");
    if (useEnabledBox) {
      enabledBox = new JCheckBox();
      origEnabled = manager.getProperty(property + "._enabled", "false").equalsIgnoreCase("true");
      enabledBox.setSelected(origEnabled);
      enabledBox.addItemListener(new ItemListener() {
	  public void itemStateChanged(ItemEvent e) {
	    enabledBoxUpdated(enabledBox.isSelected());
	  }
	});
      enabledBoxUpdated(origEnabled);
      tmpPanel.add(enabledBox);
    }

    valueComponent = tmpPanel;
    //this.add(valueDisplay);
    //this.add(inputButton);
    this.add(tmpPanel);
    
    this.setEnabled(isEnabled);
    
    manager.registerPropertyEditor(property, this);
  }
  
  /**
   * Creates a button that will bring up a way to select a new Font.
   */
  public JButton createInputButton() {
    try {
      java.net.URL url = this.getClass().getResource(manager.getProperty("FontSelectorPane.inputButton.image", "/net/suberic/util/gui/images/More.gif"));
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
			    manager.getProperty("FontEditorPane.Select",
						     "Select"), f);

    if (newFontText != null) {
      try {
	firePropertyChangingEvent(newFontText);
	firePropertyChangedEvent(newFontText);
	valueDisplay.setText(newFontText);
      } catch (PropertyValueVetoException pvve) {
	manager.getFactory().showError(this, "Error changing value " + label.getText() + " to " + newFontText + ":  " + pvve.getReason());
      }
    }

  }

  //  as defined in net.suberic.util.gui.PropertyEditorUI
  

  /**
   * This writes the currently configured value in the PropertyEditorUI
   * to the source PropertyEditorManager.
   */
  public void setValue() {
    if (isEnabled() && isChanged()) {
      //System.err.println("setting value for " + property);
      manager.setProperty(property, (String)valueDisplay.getText());
      
      if (useEnabledBox) {
	if (enabledBox.isSelected())
	    manager.setProperty(property + "._enabled", "true");
	else
	  manager.setProperty(property + "._enabled", "false");
      }
    }
  }
  
  /**
   * Returns the current values of the edited properties as a 
   * java.util.Properties object.
   */
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property, (String)valueDisplay.getText());
    if (useEnabledBox) {
      if (enabledBox.isSelected())
	retProps.setProperty(property + "._enabled", "true");
      else
	retProps.setProperty(property + "._enabled", "false");
    }
    return retProps;
  }

  /**
   * This resets the editor to the original (or latest set, if setValue() 
   * has been called) value of the edited property.
   */
  public void resetDefaultValue() {
    valueDisplay.setText(originalValue);
    if (useEnabledBox)
      enabledBox.setSelected(origEnabled);
  } 
  
  /**
   * Returns whether or not this editor still has its originally configured
   * value.
   */
  public boolean isChanged() {
    if (useEnabledBox) {
      return (enabledBox.isSelected() != origEnabled  || !(originalValue.equals(valueDisplay.getText())));
    } else {
      return (!(originalValue.equals(valueDisplay.getText())));
    }
  }
  
  /**
   * Sets the enabled property of the PropertyEditorUI.  Disabled 
   * editors should not be able to do setValue() calls.
   */
  public void setEnabled(boolean newValue) {
    if (useEnabledBox) {
      enabledBox.setEnabled(newValue);
      //inputButton.setEnabled(newValue && enabledBox.isSelected());

      if (inputButton != null) {
	inputButton.setEnabled(newValue && enabledBox.isSelected());
      }
      if (valueDisplay != null) {
	valueDisplay.setEnabled(newValue && enabledBox.isSelected());
      }

    } else {
      if (inputButton != null) {
	inputButton.setEnabled(newValue);
      }
      if (valueDisplay != null) {
	valueDisplay.setEnabled(newValue);
      }
    }
    enabled=newValue;
  }

  /**
   * Called when the enabledBox's value is updated.
   */
  private void enabledBoxUpdated(boolean newValue) {
    if (inputButton != null)
      inputButton.setEnabled(newValue);

    if (valueDisplay != null)
      valueDisplay.setEnabled(newValue);
  }
  
}
