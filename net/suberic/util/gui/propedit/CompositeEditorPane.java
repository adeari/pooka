package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.Vector;
import java.util.List;
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
public class CompositeEditorPane extends CompositeSwingPropertyEditor {
  boolean scoped;
  
  /**
   * This configures this editor with the following values.
   *
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
    
    this.setBorder(BorderFactory.createEtchedBorder());

    debug = manager.getProperty("editors.debug", "false").equalsIgnoreCase("true");

    if (debug) {
      System.out.println("creating CompositeEditorPane for " + property + " with template " + editorTemplate);
    }
    
    scoped = manager.getProperty(template + ".scoped", "false").equalsIgnoreCase("true");

    if (debug) {
      System.out.println("manager.getProperty (" + template + ".scoped) = " +  manager.getProperty(template + ".scoped", "false") + " = " + scoped);
    }

    List properties = new Vector();
    List templates = new Vector();
    
    if (scoped) {
      if (debug) {
	System.out.println("testing for template " + template);
      }
      String scopeRoot = manager.getProperty(template + ".scopeRoot", template);
      if (debug) {
	System.out.println("scopeRoot is " + scopeRoot);
      }
      List templateNames = manager.getPropertyAsList(template, "");
      if (debug) {
	System.out.println("templateNames = getProp(" + template + ") = " + manager.getProperty(template, ""));
      }
      
      for (int i = 0; i < templateNames.size() ; i++) {
	String propToEdit = null;
	String currentSubProperty =  (String) templateNames.get(i);
	if (manager.getProperty(scopeRoot + "." + currentSubProperty + ".addSubProperty", "true").equalsIgnoreCase("false")) {
	  propToEdit = property;
	} else {
	  propToEdit = property + "." + (String) templateNames.get(i);
	}
	String templateToEdit = scopeRoot + "." + (String) templateNames.get(i);
	properties.add(propToEdit);
	templates.add(templateToEdit);
	if (debug) {
	  System.out.println("adding " + propToEdit + ", template " + templateToEdit);
	}
      }
    } else {
      if (debug) {
	System.out.println("creating prop list for Composite EP using " + property + ", " + template);
      }
      properties = manager.getPropertyAsList(property, "");
      templates = manager.getPropertyAsList(template, "");
    }
    
    SwingPropertyEditor currentEditor;
    
    editors = new Vector();
    
    java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
    constraints.insets = new java.awt.Insets(1,3,0,3);
    
    java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(layout);

    constraints.weightx = 1.0;
    constraints.fill = java.awt.GridBagConstraints.BOTH;
    
    
    if (debug) {
      System.out.println("creating editors for " + properties.size() + " properties.");
    }

    for (int i = 0; i < properties.size(); i++) {
      currentEditor =
	(SwingPropertyEditor) manager.createEditor((String)properties.get(i), (String) templates.get(i));
      currentEditor.setEnabled(enabled);
      editors.add(currentEditor);
      
      if (currentEditor.labelComponent != null) {
	layout.setConstraints(currentEditor.labelComponent, constraints);
	contentPanel.add(currentEditor.labelComponent);
      }
      
      if (currentEditor.valueComponent != null) {
	constraints.gridwidth=java.awt.GridBagConstraints.REMAINDER;
	layout.setConstraints(currentEditor.valueComponent, constraints);
	contentPanel.add(currentEditor.valueComponent);
      }
      
      constraints.weightx = 0.0;
      constraints.gridwidth = 1;
      
    }
    
    this.add(contentPanel);
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
      labelWidth = Math.max(labelWidth, ((SwingPropertyEditor)editors.get(i)).getMinimumLabelSize().width);
      valueWidth = Math.max(valueWidth, ((SwingPropertyEditor)editors.get(i)).getMinimumValueSize().width);
      totalWidth = Math.max(totalWidth, ((SwingPropertyEditor)editors.get(i)).getMinimumTotalSize().width);
    }
    
    if (totalWidth > labelWidth + valueWidth) {
      int difference = totalWidth - labelWidth - valueWidth;
      labelWidth = labelWidth + (difference / 2);
      valueWidth = totalWidth - labelWidth;
    }
    
    for (int i = 0; i < editors.size(); i++) {
      ((SwingPropertyEditor) editors.get(i)).setWidths(labelWidth, valueWidth);
    }
    

    
  }
  
}
    


