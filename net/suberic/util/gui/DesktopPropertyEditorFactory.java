package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.*;
import java.util.Vector;

public class DesktopPropertyEditorFactory extends PropertyEditorFactory {


    public DesktopPropertyEditorFactory(VariableBundle bundle) {
	super(bundle);
    }

    /**
     * This method returns an EditorWindow (a JInternalFrame in this 
     * implementation) which has an editor for each property in the
     * properties Vector.  The title string is the title of the 
     * JInternalFrame.
     */
    public Container createEditorWindow(String title, Vector properties) {
	JInternalFrame jif = new JInternalFrame(title, false, false, false, false);
	jif.getContentPane().add(new PropertyEditorPane(this, properties, jif));
	jif.setSize(100,100);
	jif.pack();
	return jif;
    }

    /**
     * This method returns an EditorWindow (a JInternalFrame in this 
     * implementation) which has an editor for each property in the
     * properties Vector.  The title string is the title of the 
     * JInternalFrame.
     */
    public Container createEditorWindow(String title, Vector properties, Vector templateTypes) {
	JInternalFrame jif = new JInternalFrame(title, false, false, false, false);
	jif.getContentPane().add(new PropertyEditorPane(this, properties, templateTypes, jif));
	jif.setSize(100,100);
	jif.pack();
	return jif;
    }

    /**
     * This shows an input Dialog.
     */
    public String showInputDialog(DefaultPropertyEditor dpe, String query) {
	if (dpe instanceof java.awt.Component) 
	    return JOptionPane.showInternalInputDialog(dpe, query);
	else
	    return null;
    }
	    
}

     
    



