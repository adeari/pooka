package net.suberic.util.gui;
import net.suberic.util.gui.*;
import net.suberic.util.VariableBundle;
import java.awt.Color;
import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * This displays the currently selected file (if any), along with a 
 * button which will bring up a JColorChooser to choose any other file(s).
 */

public class ColorSelectorPane extends DefaultPropertyEditor {
  
  public static int NO_VALUE = -1;

  String property;
  String propertyTemplate;
  String origValue;
  JLabel label;
  JButton inputButton;
  VariableBundle sourceBundle;

  int originalRgb = NO_VALUE;
  Color currentColor;

  /**
   * This creates a new ColorSelectorPane.
   */
  public ColorSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle, boolean isEnabled) {
    configureEditor(null, newProperty, typeTemplate, bundle, isEnabled);
  }
  
  /**
   * This creates a new ColorSelectorPane.
   */
  public ColorSelectorPane(String newProperty, String typeTemplate, VariableBundle bundle) {
    this(newProperty, typeTemplate, bundle, true);
  }
  
  /**
   * This creates a new ColorSelectorPane.
   */
  public ColorSelectorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
    this(newProperty, newProperty, bundle, isEnabled);
  }
  
  /**
   * This creates a new ColorSelectorPane which is enabled.
   */
  public ColorSelectorPane(String newProperty, VariableBundle bundle) {
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
    
    origValue = sourceBundle.getProperty(property, Integer.toString(NO_VALUE));
    originalRgb = Integer.parseInt(origValue);

    label = new JLabel(sourceBundle.getProperty(propertyTemplate + ".label", defaultLabel));

    inputButton = createInputButton();

    if (originalRgb != NO_VALUE) {
      setCurrentColor(new Color(originalRgb));
    } else {
      setCurrentColor(Color.blue);
    }
    
    inputButton.setPreferredSize(new java.awt.Dimension(150, label.getMinimumSize().height));
    
    this.add(label);
    labelComponent = label;
    JPanel tmpPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0,0));
    tmpPanel.add(inputButton);
    valueComponent = tmpPanel;
    //this.add(valueDisplay);
    //this.add(inputButton);
    this.add(tmpPanel);
    
    this.setEnabled(isEnabled);
    
  }
  
  /**
   * Creates a button that will bring up a way to select a new Color.
   */
  public JButton createInputButton() {
    JButton newButton = new JButton();
    
    newButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  selectNewColor();
	}
      });
    
    return newButton;
  }
  
  /**
   * This actually brings up a JColorChooser to select a new Color for 
   * the value of the property.
   */
  public void selectNewColor() {
    Color newColor = JColorChooser.showDialog(this, "title", currentColor);
    if (newColor != null) {
      setCurrentColor(newColor);
    }
  }
  
  public void setCurrentColor(Color newColor) {
    currentColor = newColor;
    inputButton.setBackground(currentColor);
  }
  
  public Color getCurrentColor() {
    return currentColor;
  }
  
    //  as defined in net.suberic.util.gui.PropertyEditorUI
  
  public void setValue() {
    if (isEnabled() && isChanged())
      sourceBundle.setProperty(property, Integer.toString(currentColor.getRGB()));
  }
  
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property, Integer.toString(currentColor.getRGB()));
    
    return retProps;
  }

  public void resetDefaultValue() {
    if (originalRgb != NO_VALUE) {
      setCurrentColor(new Color(originalRgb));
    } else {
      setCurrentColor(Color.blue);
    }
  }
  
  public boolean isChanged() {
    return (!(origValue.equals(Integer.toString(currentColor.getRGB()))));
  }
  
  public void setEnabled(boolean newValue) {
    if (inputButton != null) {
      inputButton.setEnabled(newValue);
      enabled=newValue;
    }
  }
  
}
