package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.*;
import java.util.Vector;

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
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, Vector properties) {
    JFrame jf = (JFrame) createEditorWindow(title, properties);
    jf.show();
  }

  /**
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, Vector properties, Vector templates) {
    JFrame jf = (JFrame) createEditorWindow(title, properties, templates);
    jf.show();
  }
  
  /**
   * Creates and displays an editor window.  
   */
  public void showNewEditorWindow(String title, SwingEditorPane editor) {
    JFrame jf = new JFrame(title);
    jf.getContentPane().add(new PropertyEditorPane(this, editor, jf));
    jf.setSize(200,200);
    jf.pack();
    jf.show();
  }

  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties Vector.  The title string is the title of the 
   * JInternalFrame.
   */
  public Container createEditorWindow(String title, Vector properties) {
    JFrame jf = new JFrame(title);
    jf.getContentPane().add(createEditor(this, properties, jf));

    jf.setSize(200,200);
    jf.pack();
    return jf;
  }

  /**
   * This method returns an EditorWindow (a JFrame in this
   * implementation) which has an editor for each property in the
   * properties Vector.  The title string is the title of the 
   * JFrame.
   */
  public Container createEditorWindow(String title, Vector properties, Vector templates ) {
    JFrame jf = new JFrame(title);
    jf.getContentPane().add(new CompositeEditorPane(this, properties, templates, jf));
    jf.setSize(200,200);
    jf.pack();
	return jf;
    }
  
  

  
}



    



