package net.suberic.util.gui;
import javax.swing.*;
import java.util.Vector;
import java.awt.*;
import net.suberic.util.VariableBundle;

/**
 * This will made an editor for a list of properties.  Each property, in
 * turn, should have a list of properties wihch it itself edits.  Each
 * top-level property will be a tab, with its values shown on the panel.
 *
 * So an example of a property definition for this would be:
 *
 * TabbedList=tabOne:tabTwo:tabThree
 * TabbedList.tabOne=prop1:prop2:prop3:prop4
 * TabbedList.tabTwo=prop5:prop6
 * TabbedList.tabThree=prop7:prop8:prop9
 *
 * Note that you can also set this up to have the 
 *
 */
public class TabbedEditorPane extends DefaultPropertyEditor {
  Vector editors;
  PropertyEditorFactory factory;
  
  String property;
  String template;
  JTabbedPane tabbedPane;
  VariableBundle sourceBundle;
  
  boolean templateScoped = false;
  boolean propertyScoped = false;
  
  public TabbedEditorPane(String newProperty, String newTemplate, PropertyEditorFactory newFactory) {
    configureEditor(newFactory, newProperty, newTemplate, newFactory.getBundle(), true);
  }
  
  /**
   * This configures this editor with the following values.
   */
  public void configureEditor(PropertyEditorFactory newFactory, String propertyName, String templateType, VariableBundle bundle, boolean isEnabled) {
    
    property=propertyName;
    template=templateType;
    factory = newFactory;
    sourceBundle = bundle;
    enabled=isEnabled;
    
    templateScoped = sourceBundle.getProperty(template + ".templateScoped", "false").equalsIgnoreCase("true");
    propertyScoped = sourceBundle.getProperty(template + ".propertyScoped", "false").equalsIgnoreCase("true");
    
    tabbedPane = new JTabbedPane();
    
    // first, get the strings that we're going to edit.
    
    //System.out.println("creating prop from " + template + "=" + bundle.getProperty(template, ""));
    Vector propsToEdit = bundle.getPropertyAsVector(template, "");
    
    DefaultPropertyEditor currentEditor;
    
    editors = new Vector();
    
    //System.out.println("adding editors to tabbed pane.");
    for (int i = 0; i < propsToEdit.size(); i++) {
      String currentProperty = template + "." + (String)propsToEdit.elementAt(i);
      
      //System.out.println("getting editor for " + currentProperty);
      if (propertyScoped) {
	//System.out.println("getting editor for " + property + ", " + currentProperty);
	currentEditor = createEditorPane(property, currentProperty);
      } else {
	//System.out.println("getting editor for " + currentProperty + ", " + currentProperty);
	currentEditor = createEditorPane(currentProperty, currentProperty);
      }
      
      //System.out.println("adding " + currentEditor);
      //System.out.println("currentEditor.getMinimumSize() = " + currentEditor.getMinimumSize());
      editors.add(currentEditor);
      tabbedPane.add(factory.getBundle().getProperty(currentProperty + ".label", currentProperty), currentEditor);
    }
    
    labelComponent=tabbedPane;
    //labelComponent.validate();
    //tabbedPane.layoutComponent
    //tabbedPane.setMinimumSize(new Dimension(200,200));
    //tabbedPane.setPreferredSize(new Dimension(200,200));
    //System.out.println("minimumSize for tabbedPane = " + tabbedPane.getMinimumSize());
    //System.out.println("preferredSize for tabbedPane = " + tabbedPane.getPreferredSize());
    //System.out.println("size for tabbedPane = " + tabbedPane.getSize());
    this.add(tabbedPane);
  }
  
  /**
   * Creates an editor pane for a group of values.
   */
  private DefaultPropertyEditor createEditorPane(String subProperty, String subTemplate) {
    return new CompositeEditorPane(factory, subProperty, subTemplate);
  }
  
  
  public void setValue() {
    if (isEnabled()) {
      for (int i = 0; i < editors.size() ; i++) {
	((PropertyEditorUI) editors.elementAt(i)).setValue();
      }
    }
  }
  
  public void resetDefaultValue() {
    if (isEnabled()) {
      for (int i = 0; i < editors.size() ; i++) {
	((PropertyEditorUI) editors.elementAt(i)).resetDefaultValue();
      }
    }
  }
  
  public java.util.Properties getValue() {
    java.util.Properties currentRetValue = new java.util.Properties();
    java.util.Iterator iter = editors.iterator();
    while (iter.hasNext()) {
      currentRetValue.putAll(((DefaultPropertyEditor)iter.next()).getValue());
    }
    
    return currentRetValue;
  }
  
  public void setEnabled(boolean newValue) {
    for (int i = 0; i < editors.size() ; i++) {
      ((PropertyEditorUI) editors.elementAt(i)).setEnabled(newValue);
      enabled=newValue;
    }
  }
}
    


