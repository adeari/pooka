package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.*;
import java.util.Vector;

public class PropertyEditorFactory {

    VariableBundle sourceBundle;

    /**
     * Here are the ones that I can think of:

     basic property:  text field
     property with choices:  drop down list
     boolean:  check box
     entire field:  jtabbedpane
     ... one property with subproperties:  that's tough.  left window with
        entries, right with values?

     (add password -jphekman)
    */

    public PropertyEditorFactory(VariableBundle bundle) {
	sourceBundle = bundle;
    }

    public Container createEditorWindow(String title, Vector properties) {
	JInternalFrame jif = new JInternalFrame(title, false, false, false, false);
	jif.getContentPane().add(new PropertyEditorPane(this, properties, jif));
	jif.setSize(100,100);
	jif.pack();
	return jif;
    }

    public DefaultPropertyEditor createEditor(String property) {
	String test = sourceBundle.getProperty(property + ".propertyType", "");
	if (test.equals("String"))
	    return createStringEditor(property);
	if (test.equals("Password"))
	    return createPasswordEditor(property);
	else if (test.equals("List"))
	    return createListEditor(property);
	else if (test.equals("Boolean"))
	    return createBooleanEditor(property);
	else if (test.equals("Multi"))
	    return createMultiEditor(property);
	else
	    return createBasicEditor(property);
    }

    private DefaultPropertyEditor createBasicEditor(String property) {
	return createStringEditor(property);
    }

    private DefaultPropertyEditor createStringEditor(String property) {
	return new StringEditorPane(property, sourceBundle);
    }

    private DefaultPropertyEditor createPasswordEditor(String property) {
	return new PasswordEditorPane(property, sourceBundle);
    }

    private DefaultPropertyEditor createListEditor(String property) {
	return new ListEditorPane(property, sourceBundle);
    }

    private DefaultPropertyEditor createBooleanEditor(String property) {
	return new BooleanEditorPane(property, sourceBundle);
    }

    private DefaultPropertyEditor createMultiEditor(String property) {
	return new MultiEditorPane(property,this);
    }

    public VariableBundle getBundle() {
	return sourceBundle;
    }

    public String showInputDialog(DefaultPropertyEditor dpe, String query) {
	if (dpe instanceof java.awt.Component) 
	    return JOptionPane.showInternalInputDialog(dpe, query);
	else
	    return null;
    }
	    
}

     
    



