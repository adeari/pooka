package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.util.List;
import java.awt.Container;
import java.awt.Component;

/**
 * A factory which can be used to create PropertyEditorUI's.
 */
public class PropertyEditorFactory {
  
  // the VariableBundle that holds both the properties and the editor
  // definitions.

  VariableBundle sourceBundle;

  /**
   * Creates a PropertyEditorFactory using the given VariableBundle as
   * a source.
   */
  public PropertyEditorFactory(VariableBundle bundle) {
    sourceBundle = bundle;
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
    JFrame jf = (JFrame) createEditorWindow(title, properties, properties);
    jf.show();
  }

  /**
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, List properties, List templates) {
    JFrame jf = (JFrame) createEditorWindow(title, properties, templates);
    jf.show();
  }
  
  /**
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, List properties, List templates, PropertyEditorManager mgr) {
    JFrame jf = (JFrame) createEditorWindow(title, properties, templates, mgr);
    jf.show();
  }
  
  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties List.  The title string is the title of the 
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, List properties) {
    return createEditorWindow(title, properties, properties, new PropertyEditorManager(sourceBundle, this));
  }

  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties List.  The title string is the title of the 
   * JFrame.
   */
  public Container createEditorWindow(String title, List properties, List templates ) {
    return createEditorWindow(title, properties, templates, new PropertyEditorManager(sourceBundle, this));
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
    return null;
  }

  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using the given PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(List properties, List editorTemplates, PropertyEditorManager mgr) {
    return null;
  }
  
}



    



