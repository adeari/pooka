package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import net.suberic.util.gui.IconManager;
import java.util.*;
import java.awt.Container;
import java.awt.Component;

/**
 * A factory which can be used to create PropertyEditorUI's.
 */
public class PropertyEditorFactory {
  // the property that defines the different editor classes for the
  // registry.
  public static String SOURCE_PROPERTY = "PropertyEditor";

  // the VariableBundle that holds both the properties and the editor
  // definitions.
  VariableBundle sourceBundle;

  // the IconManager used for PropertyEditors that use icons.
  IconManager iconManager;

  // the propertyType to className mapping
  Map typeToClassMap = new HashMap();

  /**
   * Creates a PropertyEditorFactory using the given VariableBundle as
   * a source.
   */
  public PropertyEditorFactory(VariableBundle bundle, IconManager manager) {
    sourceBundle = bundle;
    iconManager = manager;
    createTypeToClassMap();
  }

  /**
   * Creates the typeToClassMap.
   */
  private void createTypeToClassMap() {

    try {
      Class parentClass = Class.forName("net.suberic.util.gui.propedit.SwingPropertyEditor");

      Vector propertyTypes = sourceBundle.getPropertyAsVector(SOURCE_PROPERTY, "");
      for (int i = 0; i < propertyTypes.size(); i++) {
        String currentType = (String) propertyTypes.get(i);
        String className = sourceBundle.getProperty(SOURCE_PROPERTY + "." + currentType + ".class", "");
        try {
          Class currentClass = Class.forName(className);
          if (parentClass.isAssignableFrom(currentClass)) {
            typeToClassMap.put(currentType, currentClass);
          }
        } catch (Exception e) {
          System.out.println("error registering class for property type " + currentType + ":  " + e);
        }
      }
    } catch (Exception e) {
      System.out.println("caught exception initializing PropertyEditorFactory:  " + e);
      e.printStackTrace();
    }
  }

  /**
   * Shows an error message.
   */
  public void showError(Object component, String errorMessage) {
    JOptionPane.showMessageDialog((Component) component, errorMessage);
  }

  /**
   * Shows an input dialog.
   */
  public String showInputDialog(SwingPropertyEditor dpe, String query) {
    return JOptionPane.showInputDialog(dpe, query);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, List properties) {
    showNewEditorWindow(title, properties, properties);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, List properties, List templates) {
    showNewEditorWindow(title, properties, templates, null);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, List properties, List templates, PropertyEditorManager mgr) {
    JFrame jf = (JFrame) createEditorWindow(title, properties, templates, mgr);
    jf.setVisible(true);
  }


  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, PropertyEditorUI editor) {
    JFrame jf = new JFrame(title);
    jf.getContentPane().add(new PropertyEditorPane(editor.getManager(), (SwingPropertyEditor)editor, jf));
    jf.setSize(200,200);
    jf.pack();
    jf.setVisible(true);
  }

  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties List.  The title string is the title of the
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, List properties) {
    return createEditorWindow(title, properties, properties, new PropertyEditorManager(sourceBundle, this, iconManager));
  }

  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties List.  The title string is the title of the
   * JFrame.
   */
  public Container createEditorWindow(String title, List properties, List templates ) {
    return createEditorWindow(title, properties, templates, new PropertyEditorManager(sourceBundle, this, iconManager));
  }

  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties Vector.  The title string is the title of the
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, List properties, List templates, PropertyEditorManager mgr) {
    JFrame jf = new JFrame(title);
    jf.getContentPane().add(new PropertyEditorPane(mgr, properties, templates, jf));
    jf.setSize(200,200);
    jf.pack();
    return jf;
  }


  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(String property, String editorTemplate, PropertyEditorManager mgr) {

    return createEditor(property, editorTemplate, mgr, true);
  }

  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(String property, String editorTemplate, PropertyEditorManager mgr, boolean enabled) {
    String type = sourceBundle.getProperty(editorTemplate + ".propertyType", "");
    return createEditor(property, editorTemplate, type, mgr, enabled);
  }
  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(String property, String editorTemplate, String baseProperty, String type, PropertyEditorManager mgr, boolean enabled) {

    //System.err.println("creating editor for property " + property + ", template " + editorTemplate + ", type " + type);

    Class editorClass = (Class) typeToClassMap.get(type);
    if (editorClass == null) {
      editorClass = (Class) typeToClassMap.get("String");
    }

    PropertyEditorUI returnValue = null;
    try {
      returnValue = (PropertyEditorUI) editorClass.newInstance();
    } catch (Exception e) {
      System.err.println("error creating editor for property " + property + ":  " + e);
      returnValue = new StringEditorPane();
    }
    returnValue.configureEditor(property, editorTemplate, mgr, enabled);
    return returnValue;
  }

  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(List properties, List editorTemplates, PropertyEditorManager mgr) {
    return new CompositeEditorPane(properties, editorTemplates, mgr);
  }

  /**
   * Gets the source bundle for this factory.
   */
  public VariableBundle getSourceBundle() {
    return sourceBundle;
  }

  /** 
   * Gets the IconManager for this factory.
   */
  public IconManager getIconManager() {
    return iconManager;
  }
}







