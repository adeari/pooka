package net.suberic.pooka.gui.filter;
import javax.swing.*;
import java.util.Properties;

/**
 * This is a class that lets you choose your filter actions.
 */
public class FlagFilterEditor extends FilterEditor {
    String originalFlagName;
    String originalFlagValue;

    JComboBox flagBox;
    JComboBox trueFalseBox;

    public static String FILTER_CLASS = "net.suberic.pooka.filter.FlagFilterAction";

    /**
     * Configures the given FilterEditor from the given VariableBundle and
     * property.
     */
    public void configureEditor(net.suberic.util.VariableBundle bundle, String propertyName) {
	property = propertyName;
	sourceBundle = bundle;

	Vector flagNames = Pooka.getSearchManager().getFlagLabels();
	flagBox = new JComboBox(flagNames);

	this.add(flagBox);

	Vector trueFalse = new Vector();
	trueFalse.add(Pooka.getProperty("label.true", "True"));
	trueFalse.add(Pooka.getProperty("label.false", "False"));
	
	trueFalseBox = new JComboBox(trueFalse);

	this.add(trueFalseBox);
    }
    
    /**
     * Gets the values that would be set by this FilterEditor.
     */
    public java.util.Properties getValue() {
	Properties props = fsp.getValue();

	String oldClassName = sourceBundle.getProperty(property + ".class", "");
	if (!oldClassName.equals(FILTER_CLASS))
	    props.setProperty(property + ".class", FILTER_CLASS);
	
	return props;
    }

    /**
     * Sets the values represented by this FilterEditor in the sourceBundle.
     */
    public void setValue() {

	fsp.setValue();

	String oldClassName = sourceBundle.getProperty(property + ".class", "");
	if (!oldClassName.equals(FILTER_CLASS))
	    sourceBundle.setProperty(property + ".class", FILTER_CLASS);
    }
    
}
