package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;

/**
 * This class overrides the default TableCellRenderer in order to
 * show things like unread messages, etc.
 */

public class DefaultFolderCellRenderer extends DefaultTableCellRenderer {
    static Font unreadFont = null;
    
    public static Font getUnreadFont() {
	return unreadFont;
    }
    
    public static void setUnreadFont(Font newValue) {
	unreadFont = newValue;
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	Component returnValue = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	if (table.getModel() instanceof FolderTableModel) {
	    FolderTableModel ftm = (FolderTableModel) table.getModel();
	    
	    if (!(ftm.getMessageProxy(row).isSeen())) {
		if (getUnreadFont() != null) {
		    returnValue.setFont(getUnreadFont());
		    return returnValue;
		} else {
		    // create the new font.
		    String fontStyle = Pooka.getProperty("FolderTable.UnreadStyle", "");
		    Font f = null;
		
		    if (fontStyle.equalsIgnoreCase("BOLD"))
			f = returnValue.getFont().deriveFont(Font.BOLD);
		    else if (fontStyle.equalsIgnoreCase("ITALIC"))
			f = returnValue.getFont().deriveFont(Font.ITALIC);
		    
		    if (f == null)
			f = returnValue.getFont();
		    
		    setUnreadFont(f);
		    returnValue.setFont(f);
		    return returnValue;
		}
	    }
	}

	return returnValue;
    }
} //end class DefaultFolderCellRenderer
