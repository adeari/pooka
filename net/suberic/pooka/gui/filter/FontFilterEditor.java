package net.suberic.pooka.gui.filter;
import javax.swing.*;
import java.awt.*;

/**
 * This class allows you to choose colors for a FontFilter.
 */
public class FontFilterEditor extends FilterEditor {
    JComboBox fontCombo;
    String origFontString;

    public static String FILTER_CLASS = "net.suberic.pooka.gui.filter.FontDisplayFilter";

    /**
     * Configures the given FilterEditor from the given VariableBundle and
     * property.
     */
    public void configureEditor(net.suberic.util.VariableBundle bundle, String propertyName) {
	property = propertyName;
	sourceBundle = bundle;
	
	origFontString = sourceBundle.getProperty(propertyName + ".type");

	fontCombo = createFontCombo();

	fontCombo.setSelectedItem(getFontLabel(origFontString));
	
	this.add(fontCombo);

    }

    /**
     * creates the font combo.
     */
    public JComboBox createFontCombo() {
	java.util.Vector labels = new java.util.Vector();
	labels.add(sourceBundle.getProperty("Font.PLAIN.label", "PLAIN"));
	labels.add(sourceBundle.getProperty("Font.BOLD.label", "BOLD"));
	labels.add(sourceBundle.getProperty("Font.ITALIC.label", "ITALIC"));

	return new JComboBox(labels);
    }

    /**
     * Returns the font label for this font.
     */
    public String getFontLabel(String fontType) {
	return sourceBundle.getProperty("Font." + fontType + ".label", "");
    }

    /**
     * Returns the selected font type.
     */
    public String getSelectedFontType() {
	String selectedString = (String) fontCombo.getSelectedItem();
	if (selectedString.equalsIgnoreCase(sourceBundle.getProperty("Font.PLAIN.label", "PLAIN")))
	    return "PLAIN";
	else if (selectedString.equalsIgnoreCase(sourceBundle.getProperty("Font.BOLD.label", "BOLD")))
	    return "BOLD";
	else if (selectedString.equalsIgnoreCase(sourceBundle.getProperty("Font.ITALIC.label", "ITALIC")))
	    return "ITALIC";
	else
	    return "";
    }
    
    /**
     * Gets the values that would be set by this FilterEditor.
     */
    public java.util.Properties getValue() {
	java.util.Properties props = new java.util.Properties();
	props.setProperty(property + ".type", getSelectedFontType());
	props.setProperty(property + ".class", FILTER_CLASS);
	return props;
    }

    /**
     * Sets the values represented by this FilterEditor in the sourceBundle.
     */
    public void setValue() {
	String newValue = getSelectedFontType();
	if (newValue != origFontString)
	    sourceBundle.setProperty(property + ".type", newValue);

	String oldClassName = sourceBundle.getProperty(property + ".class", "");
	if (!oldClassName.equals(FILTER_CLASS))
	    sourceBundle.setProperty(property + ".class", FILTER_CLASS);
    }
    
}









