package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

/**
 * This will make an editor for a list of properties.  Each property, in
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
 * Options:
 * TabbedList.propertyScoped - add the subproperty to the property instead
 *                             of using it as its own property
 * TabbedList.templateScoped - add subproperty to the template
 *
 */
public class TabbedEditorPane extends CompositeSwingPropertyEditor {

  JTabbedPane tabbedPane;
  protected boolean templateScoped = false;
  protected boolean propertyScoped = false;
  
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
    System.err.println("creating tep for " + propertyName + ", template " + template);
    property=propertyName;
    manager=newManager;
    editorTemplate = template;
    originalValue = manager.getProperty(property, "");

    debug = manager.getProperty("editors.debug", "false").equalsIgnoreCase("true");

    if (debug) {
      System.out.println("configuring editor with property " + propertyName + ", editorTemplate " + editorTemplate);
    }

    enabled=isEnabled;

    templateScoped = manager.getProperty(editorTemplate + ".templateScoped", "false").equalsIgnoreCase("true");
    propertyScoped = manager.getProperty(editorTemplate + ".propertyScoped", "false").equalsIgnoreCase("true");

    tabbedPane = new JTabbedPane();
    
    // first, get the strings that we're going to edit.

    if (debug) {
      System.out.println("creating prop from " + template + "=" + manager.getProperty(template, ""));
    }

    List propsToEdit = manager.getPropertyAsList(template, "");
    
    editors = createEditors(property, propsToEdit);
    
    labelComponent=tabbedPane;

    if (debug) {
      System.out.println("minimumSize for tabbedPane = " + tabbedPane.getMinimumSize());
      System.out.println("preferredSize for tabbedPane = " + tabbedPane.getPreferredSize());
      System.out.println("size for tabbedPane = " + tabbedPane.getSize());
    }

    this.add(tabbedPane);
  }

  /**
   * Creates the appropriate editors for the given properties.
   */
  public List createEditors(String property, List propsToEdit) {
    
    List editorList = new ArrayList();
    SwingPropertyEditor currentEditor;
    
    for (int i = 0; i < propsToEdit.size(); i++) {
      String currentProperty = editorTemplate + "." + (String)propsToEdit.get(i);
      
      if (debug) {
	System.err.println("getting editor for " + currentProperty);
	if (propertyScoped) {
	  if (manager.getProperty(currentProperty + ".addSubProperty", "false").equalsIgnoreCase("true")) {
	    if (debug) {
	      System.err.println("TEP:  addSubProperty = true; getting editor for " + property + "." + propsToEdit.get(i) + "," + currentProperty);
	    }
	    currentEditor = createEditorPane(property + "." + (String) propsToEdit.get(i), currentProperty);
	  } else {
	    if (debug) {
	      System.err.println("TWP:  addSubProperty = false.  getting editor for " + property + ", " + currentProperty);
	    }
	    currentEditor = createEditorPane(property, currentProperty);
	  }
	} else {
	  if (debug) {
	    System.err.println("TEP:  notPropScoped; getting editor for " + currentProperty + ", " + currentProperty);
	  }
	  currentEditor = createEditorPane(currentProperty, currentProperty);
	}
	
	if (debug) {
	  System.out.println("adding " + currentEditor);
	  System.out.println("currentEditor.getMinimumSize() = " + currentEditor.getMinimumSize());
	}
	editorList.add(currentEditor);
	tabbedPane.add(manager.getProperty(currentProperty + ".label", currentProperty), currentEditor);
      }
    }

    return editorList;
  }
    
  
  /**
   * Creates an editor pane for a group of values.
   */
  private SwingPropertyEditor createEditorPane(String subProperty, String subTemplate) {
    CompositeEditorPane cep = new CompositeEditorPane();
    cep.configureEditor(subProperty, subTemplate, manager, true);
    return cep;
  }
  
}
    


