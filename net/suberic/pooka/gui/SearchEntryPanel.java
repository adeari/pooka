package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.*;

/**
 * This is a full dialog for making search options.
 */
public class SearchEntryPanel extends JPanel {
    // top panel
    JPanel folderChooserPanel;
    JLabel folderChooserLabel;
    JComboBox folderCombo;

    // bottom panel
    JPanel conditionPanel;
    JScrollPane conditionScrollPane;
    SearchTermPane[] searchTerms;


    public class SearchTermPane extends JPanel {
	
    }
    
}
