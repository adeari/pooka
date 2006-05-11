package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.awt.Container;
import java.awt.Component;
import java.util.Vector;
import java.util.List;
import net.suberic.util.VariableBundle;

/**
 * This is a Property Editor which displays a group of properties.
 * These properties should all be defined by a single property.
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
   * Creates a CompositeEditorPane.
   */
  public CompositeEditorPane() {

  }

  /**
   * Creates a CompositeEditorPane editing the given list.
   */
  public CompositeEditorPane(List properties, List templates, PropertyEditorManager mgr) {
    configureEditor(properties, templates, mgr);
  }
  
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
    enabled=isEnabled;
    originalValue = manager.getProperty(property, "");
    
    this.setBorder(BorderFactory.createEtchedBorder());

    getLogger().fine("creating CompositeEditorPane for " + property + " with template " + editorTemplate);
    
    scoped = manager.getProperty(template + ".scoped", "false").equalsIgnoreCase("true");

    getLogger().fine("manager.getProperty (" + template + ".scoped) = " +  manager.getProperty(template + ".scoped", "false") + " = " + scoped);
    
    List properties = new Vector();
    List templates = new Vector();
    
    if (scoped) {
      getLogger().fine("testing for template " + template);
      String scopeRoot = manager.getProperty(template + ".scopeRoot", template);
      getLogger().fine("scopeRoot is " + scopeRoot);
      List templateNames = manager.getPropertyAsList(template, "");
      getLogger().fine("templateNames = getProp(" + template + ") = " + manager.getProperty(template, ""));
      
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
        getLogger().fine("adding " + propToEdit + ", template " + templateToEdit);
      }
    } else {
      getLogger().fine("creating prop list for Composite EP using " + property + ", " + template);
      properties = manager.getPropertyAsList(property, "");
      templates = manager.getPropertyAsList(template, "");
    }
    
    addEditors(properties, templates);
  }

  public void addEditors(List properties, List templates) {
    SwingPropertyEditor currentEditor;
    
    editors = new Vector();

    SpringLayout layout = new SpringLayout();

    this.setLayout(new SpringLayout());
    Component[] labelComponents = new Component[properties.size()];
    Component[] valueComponents = new Component[properties.size()];
    for (int i = 0; i < properties.size(); i++) {
      currentEditor = (SwingPropertyEditor) manager.createEditor((String)properties.get(i), (String) templates.get(i));
      currentEditor.setEnabled(enabled);
      editors.add(currentEditor);
      
      if (currentEditor instanceof LabelValuePropertyEditor) {
        LabelValuePropertyEditor lvEditor = (LabelValuePropertyEditor) currentEditor;
        this.add(lvEditor.labelComponent);
        labelComponents[i] = lvEditor.labelComponent;
        this.add(lvEditor.valueComponent);
        valueComponents[i] = lvEditor.valueComponent;
      } else {
        this.add(currentEditor);
        labelComponents[i] = currentEditor;
      }
    }
    makeCompactGrid(this, labelComponents, valueComponents, 5, 5, 5, 5);
    manager.registerPropertyEditor(property, this);
  }
  
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
  public void configureEditor(List properties, List templates, PropertyEditorManager newManager) {
    manager=newManager;
    enabled = true;

    this.setBorder(BorderFactory.createEtchedBorder());

    addEditors(properties, templates);
  }
  
}
