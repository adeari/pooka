package net.suberic.pooka.gui.filter;
import net.suberic.pooka.gui.search.*;
import net.suberic.pooka.*;
import javax.swing.*;

/**
 * This is a class that lets you choose your filter actions.
 */
public class FilterEditor extends Box {
    JLabel actionLabel;
    JComboBox type;
    JPanel typeEditor;

    public FilterEditor() {
	actionLabel = new JLabel(Pooka.getProperty("title.actionLabel", "Action"));
	
    }
}
