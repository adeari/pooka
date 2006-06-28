package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import net.suberic.util.gui.IconManager;
import java.util.*;
import java.awt.Container;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Dialog;
import javax.help.HelpBroker;

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

  // the HelpBroker
  HelpBroker helpBroker;

  // the propertyType to className mapping
  Map typeToClassMap = new HashMap();

  /**
   * Creates a PropertyEditorFactory using the given VariableBundle as
   * a source.
   */
  public PropertyEditorFactory(VariableBundle bundle, IconManager manager, HelpBroker broker) {
    sourceBundle = bundle;
    iconManager = manager;
    helpBroker = broker;
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
  public void showNewEditorWindow(String title, String property) {
    showNewEditorWindow(title, property, property);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, String property, String template) {
    showNewEditorWindow(title, property, template, null);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, String property, String template, PropertyEditorManager mgr) {
    showNewEditorWindow(title, property, template, mgr, null);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, String property, String template, PropertyEditorManager mgr, Container window) {
    showNewEditorWindow(title, property, template, property, mgr, window);
  }

  public void showNewEditorWindow(String title, String property, String template, String propertyBase, PropertyEditorManager mgr, Container window) {
    JDialog jd = (JDialog) createEditorWindow(title, property, template, propertyBase, mgr, window);
    jd.setVisible(true);
  }

  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, PropertyEditorUI editor) {
    showNewEditorWindow(title, editor, null);
  }
  /**
   * Creates and displays an editor window.
   */
  public void showNewEditorWindow(String title, PropertyEditorUI editor, Container window) {
    JDialog jd = (JDialog) createEditorWindow(title, editor, window);
    jd.setVisible(true);
  }

  /**
   * This method returns an EditorWindow (a JDialog in this
   * implementation) which has an editor for each property in the
   * property List.  The title string is the title of the
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, String property) {
    return createEditorWindow(title, property, property, new PropertyEditorManager(sourceBundle, this, iconManager));
  }

  /**
   * This method returns an EditorWindow (a JDialog in this
   * implementation) which has an editor for each property in the
   * property List.  The title string is the title of the
   * JDialog.
   */
  public Container createEditorWindow(String title, String property, String template ) {
    return createEditorWindow(title, property, template, new PropertyEditorManager(sourceBundle, this, iconManager));
  }

  /**
   * This method returns an EditorWindow (a JDialog in this
   * implementation) which has an editor for each property in the
   * property Vector.  The title string is the title of the
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, String property, String template, PropertyEditorManager mgr) {
    return createEditorWindow(title, property, template, mgr, null);
  }
  /**
   * This method returns an EditorWindow (a JDialog in this
   * implementation) which has an editor for each property in the
   * property Vector.  The title string is the title of the
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, String property, String template, PropertyEditorManager mgr, Container window) {
    return createEditorWindow(title, property, template, property, mgr, window);
  }

  public Container createEditorWindow(String title, String property, String template, String propertyBase, PropertyEditorManager mgr, Container window) {
    return createEditorWindow(title, createEditor(property, template, propertyBase, mgr, true), window);
  }

  public Container createEditorWindow(String title, PropertyEditorUI editor, Container window) {
    JDialog jd = null;
    if (window instanceof Dialog) {
      jd = new JDialog((Dialog) window, title, true);
    } else if (window instanceof Frame) {
      jd = new JDialog((Frame) window, title, true);
    } else {
      jd = new JDialog();
      jd.setTitle(title);
      jd.setModal(true);
    }
    jd.getContentPane().add(new PropertyEditorPane(editor.getManager(), (SwingPropertyEditor)editor, jd));
    return jd;
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
    return createEditor(property, editorTemplate, property, mgr, enabled);
  }

  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(String property, String editorTemplate, String propertyBase, PropertyEditorManager mgr, boolean enabled) {
    String type = sourceBundle.getProperty(editorTemplate + ".propertyType", "");
    return createEditor(property, editorTemplate, propertyBase, type, mgr, enabled);
  }
  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(String property, String editorTemplate, String propertyBase, String type, PropertyEditorManager mgr, boolean enabled) {

    System.err.println("factory:  creating editor for property " + property + ", template " + editorTemplate + ", propertyBase " + propertyBase);
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
    returnValue.configureEditor(property, editorTemplate, propertyBase, mgr, enabled);
    return returnValue;
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

  /**
   * Returns the HelpBroker for this PropertyEditorManager.
   */
  public HelpBroker getHelpBroker() {
    return helpBroker;
  }
}







