package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;
import java.util.Date;
import java.util.Calendar;

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

	if (value instanceof BooleanIcon) {
	    BooleanIcon bIcon = (BooleanIcon)value;
	    if (bIcon.bool == true) {
		Component icon = bIcon.getIcon();
		if (icon != null) {
		    icon.setBackground(returnValue.getBackground());
		}
		return icon;
	    }
	} 

	if (value instanceof Date) {
	    Date displayDate = (Date)value; 
	    String dateText = null;
	    Calendar current = Calendar.getInstance();
	    current.set(Calendar.HOUR_OF_DAY, 0);
	    current.set(Calendar.MINUTE, 0);
	    if (current.before(displayDate)) {
		dateText = Pooka.getDateFormatter().todayFormat.format(displayDate);
	    } else {
		current.add(Calendar.DAY_OF_YEAR, (current.getMaximum(Calendar.DAY_OF_WEEK) - 1) * -1);
		if (current.before(displayDate)) {
		    dateText = Pooka.getDateFormatter().thisWeekFormat.format(displayDate);
		} else {
		    dateText = Pooka.getDateFormatter().shortFormat.format(displayDate);
		}
	    }

	    if (returnValue instanceof JLabel)
		((JLabel)returnValue).setText(dateText);
	    else {
		JLabel label = new JLabel(dateText);
		label.setBackground(returnValue.getBackground());
		label.setForeground(returnValue.getForeground());
		label.setFont(returnValue.getFont());
		returnValue = label;
	    }
	}


	FolderTableModel ftm = null;

	if (table.getModel() instanceof FolderTableModel) {
	    ftm = (FolderTableModel) table.getModel();

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
