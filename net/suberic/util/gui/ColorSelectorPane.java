package net.suberic.util.gui;
import net.suberic.util.gui.*;
import net.suberic.util.VariableBundle;
import java.awt.Color;
import javax.swing.*;
import java.awt.event.*;


/**
 * This displays the currently selected file (if any), along with a 
 * button which will bring up a JColorChooser to choose any other file(s).
 *
 * If property._enabledBox is set to true, then this also adds a 
 * checkbox to show whether or not to use this property, or just to use
 * the defaults.
 * 
 * Note that the value that gets set is actually property.rgb (which is
 * the rgb value of the color selected), and, if the enabled checkbox is
 * there, property.enabled.  the property value itself is not set.
 */

public class ColorSelectorPane extends DefaultPropertyEditor {
  
  String property;
  String propertyTemplate;
  String origValue;
  JLabel label;
  JButton inputButton;
  VariableBundle sourceBundle;

  int originalRgb = -1;
  Color currentColor;

  boolean useEnabledBox = false;
  JCheckBox enabledBox = null;
  boolean origEnabled = false;

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

    label = new JLabel(sourceBundle.getProperty(propertyTemplate + ".label", defaultLabel));

    inputButton = createInputButton();
    
    int defaultValue = inputButton.getBackground().getRGB();

    origValue = sourceBundle.getProperty(property + ".rgb", Integer.toString(defaultValue));
    originalRgb = Integer.parseInt(origValue);

    setCurrentColor(new Color(originalRgb));
    
    inputButton.setPreferredSize(new java.awt.Dimension(150, label.getMinimumSize().height));
    
    this.add(label);
    labelComponent = label;
    
    JPanel tmpPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0,0));
    tmpPanel.add(inputButton);
    
    useEnabledBox = sourceBundle.getProperty(propertyTemplate + "._enabledBox", "false").equalsIgnoreCase("true");
    if (useEnabledBox) {
      enabledBox = new JCheckBox();
      origEnabled = sourceBundle.getProperty(property + "._enabled", "false").equalsIgnoreCase("true");
      enabledBox.setSelected(origEnabled);
      enabledBox.addItemListener(new ItemListener() {
	  public void itemStateChanged(ItemEvent e) {
	    enabledBoxUpdated(enabledBox.isSelected());
	  }
	});
      tmpPanel.add(enabledBox);
    }

    valueComponent = tmpPanel;
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
    if (isEnabled() && isChanged()) {
      sourceBundle.setProperty(property + ".rgb", Integer.toString(currentColor.getRGB()));
      if (useEnabledBox) {
      if (enabledBox.isSelected())
	sourceBundle.setProperty(property + "._enabled", "true");
      else
	sourceBundle.setProperty(property + "._enabled", "false");
      }
    }
  }
  
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property + ".rgb", Integer.toString(currentColor.getRGB()));
    if (useEnabledBox) {
      if (enabledBox.isSelected())
	retProps.setProperty(property + "._enabled", "true");
      else
	retProps.setProperty(property + "._enabled", "false");
    }
    return retProps;
  }

  public void resetDefaultValue() {
    setCurrentColor(new Color(originalRgb));
    
    if (useEnabledBox)
      enabledBox.setSelected(origEnabled);
  }
  
  public boolean isChanged() {
    if (useEnabledBox)
      return (! (enabledBox.isSelected() == origEnabled && origValue.equals(Integer.toString(currentColor.getRGB()))));
    else
      return (!(origValue.equals(Integer.toString(currentColor.getRGB()))));
  }
  
  public void setEnabled(boolean newValue) {
    if (useEnabledBox) {
      enabledBox.setEnabled(newValue);
      inputButton.setEnabled(newValue && enabledBox.isSelected());
    }
    if (inputButton != null) {
      inputButton.setEnabled(newValue);
    }
    enabled=newValue;
  }

  /**
   * Called when the enabledBox's value is updated.
   */
  private void enabledBoxUpdated(boolean newValue) {
    inputButton.setEnabled(newValue);
  }
  
}
