package net.suberic.util.gui;
import javax.swing.*;
import java.util.Vector;
import java.awt.*;
import net.suberic.util.VariableBundle;

/**
 * This is a Property Editor which displays a group of properties.
 * These properties should all be definte by a single property.
 *
 * An example:
 *
 * Configuration=foo:bar
 * Configuration.propertyType=Composite
 * Configuration.scoped=false
 * foo=zork
 * bar=frobozz
 *
 * Options:
 *
 * Configuration.scoped - shows that the properties listed are subproperties
 *   of both the property and the template.  So, in this example, if you 
 *   had Configuration.scoped, the properties edited would be 
 *   Configuration.foo and Configuration.bar
 * Configuration.scopeRoot - if the setting is scoped, then this is the
 *   root for the template's scope.  Useful when dealing with properties that
 *   can be reached from multiple points (i.e. if 
 *   Configuration.one.two.three=foo:bar also, then you could set the 
 *   scopeRoot to Configuration and use the already configured foo and bar.
 * Configuration.subProperty.addSubProperty - shows whether or not you
 *   should add the given subproperty to the edited property for this editor.
 *   Useful if you have a CompositeEditorPane that contains other 
 *   Composite or Tabbed EditorPanes.  If Configuration.foo is another
 *   CompositeEditorPane which in turn edits .frotz and .ozmoo, and 
 *   Configuration.foo.addSubProperty=true (the default), then 
 *   Configuration.foo.frotz and Configuration.foo.ozmoo will be edited.
 *   If Configuration.foo.addSubProperty=false, then Configuration.frotz
 *   and Configuration.ozmoo will be edited, using Configuration.foo.frotz
 *   and Configuration.foo.ozmoo as templates.  This is primarily useful
 *   when using MultiEditorPanes.
 *
 */
public class CompositeEditorPane extends DefaultPropertyEditor {
  Vector editors;
  PropertyEditorFactory factory;
  String property;
  String template;
  
  boolean scoped;
  
  /**
   * This contructor creates a PropertyEditor for the list of 
   * properties represented by the given property.
   */     
  public CompositeEditorPane(PropertyEditorFactory newFactory, String 
			     newProperty, String newTemplate) {
    super(BoxLayout.X_AXIS);
    configureEditor(newFactory, newProperty, newTemplate, newFactory.getBundle(), true);
    
  }
  
  /**
   * This contructor creates a PropertyEditor for the list of 
   * properties represented by the given property.
   */     
  public CompositeEditorPane(PropertyEditorFactory newFactory, String 
			     newProperty, String newTemplate, 
			     boolean isEnabled) {
    super(BoxLayout.X_AXIS);
    configureEditor(newFactory, newProperty, newTemplate, newFactory.getBundle(), isEnabled);
    
  }
  
  /**
   * This configures an editor for the given propertyName in the 
   * VariableBundle bundle.
   *
   */
  public void configureEditor(PropertyEditorFactory newFactory, String newProperty, String newTemplate, VariableBundle bundle, boolean isEnabled) {
    
    //System.out.println("creating CompositeEditorPane for " + newProperty + " with template " + newTemplate);
    this.setBorder(BorderFactory.createEtchedBorder());
    factory = newFactory;
    property = newProperty;
    template = newTemplate;
    enabled=isEnabled;
    
    scoped = bundle.getProperty(template + ".scoped", "false").equalsIgnoreCase("true");
    //System.out.println("bundle.getProperty (" + template + ".scoped) = " +  bundle.getProperty(template + ".scoped", "false") + " = " + scoped);
    
    Vector properties = new Vector();
    Vector templates = new Vector();
    
    if (scoped) {
      //System.out.println("testing for template " + template);
      String scopeRoot = bundle.getProperty(template + ".scopeRoot", template);
      //System.out.println("scopeRoot is " + scopeRoot);
      Vector templateNames = bundle.getPropertyAsVector(template, "");
      //System.out.println("templateNames = getProp(" + template + ") = " + bundle.getProperty(template, ""));
      for (int i = 0; i < templateNames.size() ; i++) {
	String currentSubProperty =  (String) templateNames.elementAt(i);
	if (bundle.getProperty(scopeRoot + "." + currentSubProperty + ".addSubProperty", "true").equalsIgnoreCase("false"))
	  properties.add(property);
	else
	  properties.add(property + "." + (String) templateNames.elementAt(i));

	templates.add(scopeRoot + "." + (String) templateNames.elementAt(i));
	//System.out.println("adding " + (String) templateNames.elementAt(i) + ", template " + (String) templateNames.elementAt(i));
      }
    } else {
      //System.out.println("creating prop list for Composite EP using " + property + ", " + template);
      properties = bundle.getPropertyAsVector(property, "");
      templates = bundle.getPropertyAsVector(template, "");
    }
    
    DefaultPropertyEditor currentEditor;
    
    editors = new Vector();
    
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(1,3,0,3);
    GridBagLayout layout = (GridBagLayout) getLayout();
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    
    //System.out.println("creating editors for " + properties.size() + " properties.");
    for (int i = 0; i < properties.size(); i++) {
      currentEditor =
	factory.createEditor((String)properties.elementAt(i), (String) templates.elementAt(i));
      currentEditor.setEnabled(enabled);
      editors.add(currentEditor);
      
      if (currentEditor.labelComponent != null) {
	layout.setConstraints(currentEditor.labelComponent, constraints);
	this.add(currentEditor.labelComponent);
      }
      
      if (currentEditor.valueComponent != null) {
	constraints.gridwidth=GridBagConstraints.REMAINDER;
	layout.setConstraints(currentEditor.valueComponent, constraints);
	this.add(currentEditor.valueComponent);
      }
      
      constraints.weightx = 0.0;
      constraints.gridwidth = 1;
      
    }
    
    alignEditorSizes();
  }
  
  /**
   * This should even out the various editors on the panel.
   */
  public void alignEditorSizes() {
    int labelWidth = 0;
    int valueWidth = 0;
    int totalWidth = 0;
    for (int i = 0; i <  editors.size(); i++) {
      labelWidth = Math.max(labelWidth, ((DefaultPropertyEditor)editors.elementAt(i)).getMinimumLabelSize().width);
      valueWidth = Math.max(valueWidth, ((DefaultPropertyEditor)editors.elementAt(i)).getMinimumValueSize().width);
      totalWidth = Math.max(totalWidth, ((DefaultPropertyEditor)editors.elementAt(i)).getMinimumTotalSize().width);
    }
    
    if (totalWidth > labelWidth + valueWidth) {
      int difference = totalWidth - labelWidth - valueWidth;
      labelWidth = labelWidth + (difference / 2);
      valueWidth = totalWidth - labelWidth;
    }
    
    for (int i = 0; i < editors.size(); i++) {
      ((DefaultPropertyEditor) editors.elementAt(i)).setWidths(labelWidth, valueWidth);
    }
    
  }
  
  /**
   * just for fun.
   */
  /*
    public void addNotify() {
    super.addNotify();
    alignEditorSizes();
    }
  */
  
  public void setValue() {
    
    if (isEnabled()) {
      for (int i = 0; i < editors.size(); i++) {
	((DefaultPropertyEditor)(editors.elementAt(i))).setValue();
      }
    }
  }
  
  public java.util.Properties getValue() {
    java.util.Properties currentRetValue = new java.util.Properties();
    for (int i = 0; i < editors.size(); i++) {
      
      java.util.Properties newValue = ((DefaultPropertyEditor)(editors.elementAt(i))).getValue();
      java.util.Enumeration keys = newValue.propertyNames();
      while (keys.hasMoreElements()) {
	String currentKey = (String) keys.nextElement();
	currentRetValue.setProperty(currentKey, newValue.getProperty(currentKey));
      }
    }
    
    return currentRetValue;
  }
  
  /**
   * Returns all of the values of this editorPane except for the ones
   * that belong to the given propertyEditor.
   */
  public java.util.Properties getOtherValues(PropertyEditorUI current) {
    java.util.Properties currentRetValue = new java.util.Properties();
    for (int i = 0; i < editors.size(); i++) {
      DefaultPropertyEditor currentEditor = (DefaultPropertyEditor)(editors.elementAt(i));
      if (currentEditor != current) {
	java.util.Properties newValue = ((DefaultPropertyEditor)(editors.elementAt(i))).getValue();
	java.util.Enumeration keys = newValue.propertyNames();
	while (keys.hasMoreElements()) {
	  String currentKey = (String) keys.nextElement();
	  currentRetValue.setProperty(currentKey, newValue.getProperty(currentKey));
	}
      }
    }
    
    return currentRetValue;
  }
  
  public void resetDefaultValue() {
    for (int i = 0; i < editors.size(); i++) {
      ((DefaultPropertyEditor)(editors.elementAt(i))).resetDefaultValue();
    }
  }
  
  public void setEnabled(boolean newValue) {
    if (editors != null && editors.size() > 0) {
      for (int i = 0; i < editors.size(); i++) {
	PropertyEditorUI currentEditor = (PropertyEditorUI) editors.elementAt(i);
	currentEditor.setEnabled(newValue);
      }
    }
  }
}
    


