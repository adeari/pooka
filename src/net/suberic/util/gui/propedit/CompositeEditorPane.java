package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.awt.Container;
import java.awt.Component;
import java.util.Vector;
import java.util.ArrayList;
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

  /**
   * Creates a CompositeEditorPane.
   */
  public CompositeEditorPane() {

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
  public void configureEditor(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {

    configureBasic(propertyName, template, propertyBaseName, newManager, isEnabled);

    getLogger().fine("creating CompositeEditorPane for " + property + " with template " + editorTemplate);

    //this.setBorder(BorderFactory.createEtchedBorder());

    String borderLabel = manager.getProperty(editorTemplate + ".label.border", "");
    if (borderLabel.length() > 0) {
      this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), borderLabel));
    }

    List<String> properties = new ArrayList<String>();
    List<String> templates = new ArrayList<String>();

    getLogger().fine("testing for template " + template);

    List<String> templateNames = manager.getPropertyAsList(template, "");
    getLogger().fine("templateNames = getProp(" + template + ") = " + manager.getProperty(template, ""));

    for (int i = 0; i < templateNames.size() ; i++) {
      String subTemplateString = templateNames.get(i);
      properties.add(createSubProperty(subTemplateString));
      templates.add(createSubTemplate(subTemplateString));
    }

    addEditors(properties, templates);
  }

  public void addEditors(List<String> properties, List<String> templates) {
    SwingPropertyEditor currentEditor;

    editors = new Vector();

    SpringLayout layout = new SpringLayout();

    this.setLayout(new SpringLayout());
    Component[] labelComponents = new Component[properties.size()];
    Component[] valueComponents = new Component[properties.size()];
    for (int i = 0; i < properties.size(); i++) {
      currentEditor = (SwingPropertyEditor) manager.createEditor(properties.get(i), templates.get(i), propertyBase);
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
    //makeCompactGrid(this, labelComponents, valueComponents, 5, 5, 5, 5);
    layoutGrid(this, labelComponents, valueComponents, 5, 5, 5, 5);
    manager.registerPropertyEditor(property, this);
  }


}
