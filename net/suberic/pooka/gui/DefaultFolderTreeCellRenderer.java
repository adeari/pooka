package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.tree.*;
import java.awt.*;
import javax.mail.MessagingException;
import javax.swing.JTree;

/**
 * This class overrides the default TreeCellRenderer in order to 
 * provide notification of unread messages.  Subclasses could probably
 * add additional enhancements.
 */

public class DefaultFolderTreeCellRenderer extends DefaultTreeCellRenderer {
    /* grr.  it looks like the DefaultTreeCellRenderer returns the same
       component, which is annoying.  that means that we have to reset the
       font information each time.  or at least, that's what i'm doing. :)
    */

    private boolean hasFocus;

    static Font unreadFont = null;

    static Font noUnreadFont = null;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	// from super().

	String stringValue = tree.convertValueToText(value, sel,
					  expanded, leaf, row, hasFocus);

	this.hasFocus = hasFocus;
	setText(stringValue);
	if(sel)
	    setForeground(getTextSelectionColor());
	else
	    setForeground(getTextNonSelectionColor());
	// There needs to be a way to specify disabled icons.
	if (!tree.isEnabled()) {
	    setEnabled(false);
	    if (leaf) {
		setDisabledIcon(getLeafIcon());
	    } else if (expanded) {
		setDisabledIcon(getOpenIcon());
	    } else {
		setDisabledIcon(getClosedIcon());
	    }
	}
	else {
	    setEnabled(true);
	    if (leaf) {
		setIcon(getLeafIcon());
	    } else if (expanded) {
		setIcon(getOpenIcon());
	    } else {
		setIcon(getClosedIcon());
	    }
	}
	    
	selected = sel;

	// end part from DefaultTreeCellRenderer

	TreePath tp = tree.getPathForRow(row);

	if (tp != null && tp.getLastPathComponent() instanceof FolderNode) {
	    FolderNode node = (FolderNode)tp.getLastPathComponent();

	    try {

		if (node != null && node.getFolderInfo().getFolder().getUnreadMessageCount() > 0) {
		    setFontToUnread();
		} else {
		    setFontToNoUnread();
		}
	    } catch (MessagingException me) {
		// if we can't connect, do something silly like turn the
		// node red.
		
		this.setFontToNoUnread();
		this.setForeground(Color.getColor(Pooka.getProperty("MailTreeNode.color.error", "red")));
		return this;
	    } catch (NullPointerException npe) {
		// the IMAP implementation seems to have some bugs in it when
		// it comes to closed connections...
		this.setFontToNoUnread();
		this.setForeground(Color.getColor(Pooka.getProperty("MailTreeNode.color.error", "red")));
		return this;
	    }
        } else {
	    setFontToNoUnread();
	}

	this.setForeground(Color.getColor(Pooka.getProperty("MailTreeNode.color", "black")));
	
        return this;
    }

    public void setFontToNoUnread() {
	if (getNoUnreadFont() != null) {
	    setFont(getNoUnreadFont());
	} else {
	    // create the new font.
	    String fontStyle = Pooka.getProperty("FolderTree.readStyle", "");
	    Font f = null;
	    
	    if (this.getFont() == null)
		return;

	    if (fontStyle.equalsIgnoreCase("BOLD"))
		f = this.getFont().deriveFont(Font.BOLD);
	    else if (fontStyle.equalsIgnoreCase("ITALIC"))
		f = this.getFont().deriveFont(Font.ITALIC);
	    else if (fontStyle.equals(""))
		f = this.getFont().deriveFont(Font.PLAIN);
	    
	    if (f == null)
		f = this.getFont();
	    
	    setNoUnreadFont(f);
	    this.setFont(f);
	}
    }

    public void setFontToUnread() {
	if (getUnreadFont() != null) {
	    setFont(getUnreadFont());
	} else {
	    // create the new font.
	    String fontStyle = Pooka.getProperty("FolderTree.UnreadStyle", "BOLD");
	    Font f = null;
	    
	    if (fontStyle.equalsIgnoreCase("BOLD"))
		f = this.getFont().deriveFont(Font.BOLD);
	    else if (fontStyle.equalsIgnoreCase("ITALIC"))
		f = this.getFont().deriveFont(Font.ITALIC);
	    
	    if (f == null)
		f = this.getFont();
	    
	    setUnreadFont(f);
	    this.setFont(f);
	}
    }

    public static Font getUnreadFont() {
        return unreadFont;
    }
    
    public static void setUnreadFont(Font newValue) {
        unreadFont = newValue;
    }

    public static Font getNoUnreadFont() {
	return noUnreadFont;
    }
    
    public static void setNoUnreadFont(Font newValue) {
	noUnreadFont = newValue;
    }


    /**
     * Overrides <code>JComponent.getPreferredSize</code> to
     * return slightly wider preferred size value.
     */
    public Dimension getPreferredSize() {
	/*  this really sucks.  but i can't seem to figure out how to make
	    it change the size on the JTree, so I just have to set it to
	    larger than necessary here....
	*/

	Dimension        retDimension = super.getPreferredSize();

	if(retDimension != null)
	    retDimension = new Dimension((int)((retDimension.width + 3) * 1.1),
					 retDimension.height);
	return retDimension;
    }

} //end class DefaultFolderTreeCellRenderer

