package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.*;
import java.util.Vector;

public class DesktopPropertyEditorFactory extends PropertyEditorFactory {

    protected JDesktopPane desktop;

    public DesktopPropertyEditorFactory(VariableBundle bundle, JDesktopPane newDesktop) {
	super(bundle);
	desktop = newDesktop;
    }

    public DesktopPropertyEditorFactory(VariableBundle bundle) {
	super(bundle);
    }

    public JDesktopPane getDesktop() {
	return desktop;
    }

    public void setDesktop(JDesktopPane newDesktop) {
	desktop=newDesktop;
    }
    

    /**
     * This method returns an EditorWindow (a JInternalFrame in this 
     * implementation) which has an editor for each property in the
     * properties Vector.  The title string is the title of the 
     * JInternalFrame.
     */
    public Container createEditorWindow(String title, Vector properties) {
	JInternalFrame jif = new JInternalFrame(title, true, false, false, false);
	jif.getContentPane().add(new PropertyEditorPane(this, properties, jif));
	jif.setSize(100,100);
	jif.pack();
	return jif;
    }
    
    /**
     * Creates and displays an editor window.  
     */
    public void showNewEditorWindow(String title, Vector properties) {
	JInternalFrame jif = (JInternalFrame) createEditorWindow(title, properties);
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
    public void showNewEditorWindow(String title, Vector properties, Vector templates) {
	JInternalFrame jif = (JInternalFrame) createEditorWindow(title, properties, templates);
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
    public void showNewEditorWindow(String title, DefaultPropertyEditor editor) {
      JInternalFrame jif = new JInternalFrame(title, true, false, false, false);
      jif.getContentPane().add(new PropertyEditorPane(this, editor, jif));
      jif.setSize(100,100);
      jif.pack();
      desktop.add(jif);
      jif.setVisible(true);
      try {
	jif.setSelected(true);
      } catch (java.beans.PropertyVetoException pve) {
      }
    }

    /**
     * This method returns an EditorWindow (a JInternalFrame in this 
     * implementation) which has an editor for each property in the
     * properties Vector.  The title string is the title of the 
     * JInternalFrame.
     */
    public Container createEditorWindow(String title, Vector properties, Vector templateTypes) {
	JInternalFrame jif = new JInternalFrame(title, true, false, false, false);
	jif.getContentPane().add(new PropertyEditorPane(this, properties, templateTypes, jif));
	jif.setSize(100,100);
	jif.pack();
	return jif;
    }

    /**
     * This shows an input Dialog.
     */
    public String showInputDialog(DefaultPropertyEditor dpe, String query) {
	if (dpe instanceof java.awt.Component) {
	    if (SwingUtilities.windowForComponent((java.awt.Component)dpe) != null)
		return JOptionPane.showInternalInputDialog(dpe, query);
	    else {
		if (SwingUtilities.windowForComponent(dpe.getValueComponent()) != null)
		    return JOptionPane.showInternalInputDialog(dpe.getValueComponent(), query);
	    }

	}

	return null;
    }
	    
}

     
    



