package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.util.*;
import java.awt.Container;
import java.awt.Component;

/**
 * A factory which can be used to create PropertyEditorUI's.
 */
public class DesktopPropertyEditorFactory extends PropertyEditorFactory {
  JDesktopPane desktop;

  /**
   * Creates a PropertyEditorFactory using the given VariableBundle as
   * a source.
   */
  public DesktopPropertyEditorFactory(VariableBundle bundle, JDesktopPane newDesktop) {
    super(bundle);
    desktop = newDesktop;
  }

  /**
   * Creates a PropertyEditorFactory using the given VariableBundle as
   * a source.
   */
  public DesktopPropertyEditorFactory(VariableBundle bundle) {
    super(bundle);
  }

  /**
   * Returns the desktop.
   */
  public JDesktopPane getDesktop() {
    return desktop;
  }
  
  /**
   * Sets the desktop.
   */
  public void setDesktop(JDesktopPane newDesktop) {
    desktop = newDesktop;
  }

  /**
   * Shows an error message.
   */
  public void showError(Object component, String errorMessage) {
    JOptionPane.showInternalMessageDialog(desktop, errorMessage);
  }

  /**
   * Shows an input dialog.
   */
  public String showInputDialog(SwingPropertyEditor dpe, String query) {
    return JOptionPane.showInternalInputDialog(desktop, query);
  }

  /**
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, List properties, List templates, PropertyEditorManager mgr) {
    JInternalFrame jif = (JInternalFrame) createEditorWindow(title, properties, templates, mgr);
    desktop.add(jif);
    jif.setVisible(true);
    try {
      jif.setSelected(true);
    } catch (java.beans.PropertyVetoException pve) {
    }
  }

  /**
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, PropertyEditorUI editor) {
    JInternalFrame jif = new JInternalFrame(title, true, false, false, false);
    jif.getContentPane().add(new PropertyEditorPane(editor.getManager(), (SwingPropertyEditor)editor, jif));
    
    desktop.add(jif);
    jif.setVisible(true);
    try {
      jif.setSelected(true);
    } catch (java.beans.PropertyVetoException pve) {
    }
  }
  
  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties Vector.  The title string is the title of the 
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, List properties, List templates, PropertyEditorManager mgr) {
    JInternalFrame jif = new JInternalFrame(title, true, false, false, false);
    PropertyEditorPane pep = new PropertyEditorPane(mgr, properties, templates, jif);
    jif.getContentPane().add(pep);
    jif.setSize(100,100);
    jif.pack();
    return jif; 
  }
  

}



    



