package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.tree.*;
import java.awt.*;
import javax.mail.MessagingException;
import javax.swing.JTree;

/**
 * This class overrides the default TreeCellRenderer in order to 
 * provide notification of some such, like for unread messages.  
 * Subclasses could probably add additional enhancements.
 *
 */

public class DefaultFolderTreeCellRenderer extends DefaultTreeCellRenderer {
    /* grr.  it looks like the DefaultTreeCellRenderer returns the same
       component, which is annoying.  that means that we have to reset the
       font information each time.  or at least, that's what i'm doing. :)
    */

	
    private boolean hasFocus;

    Font specialFont = null;

    Font defaultFont = null;

    public DefaultFolderTreeCellRenderer() {
	super();
    }
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
	    
	    if (isSpecial(node)) {
		setFontToSpecial(node);
	    } else {
		setFontToDefault();
	    }
        } else {
	    setFontToDefault();
	}

        return this;
    }

    public void setFontToDefault() {
	if (getDefaultFont() != null) {
	    setFont(getDefaultFont());
	} else {
	    // create the new font.
	    String fontStyle;
	    fontStyle = Pooka.getProperty("FolderTree.readStyle", "");
	    
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
	    
	    setDefaultFont(f);
	    this.setFont(f);
	}

    }

    public void setFontToSpecial(MailTreeNode mtn) {
	if (getSpecialFont() != null) {
	    setFont(getSpecialFont());
	} else {
	    // create the new font.
	    String fontStyle;
	    fontStyle = Pooka.getProperty("FolderTree.UnreadStyle", "BOLD");

	    Font f = null;
	    
	    if (fontStyle.equalsIgnoreCase("BOLD"))
		f = this.getFont().deriveFont(Font.BOLD);
	    else if (fontStyle.equalsIgnoreCase("ITALIC"))
		f = this.getFont().deriveFont(Font.ITALIC);
	    else if (fontStyle.equalsIgnoreCase("PLAIN"))
		f = this.getFont().deriveFont(Font.PLAIN);
	    
	    if (f == null)
		f = this.getFont();
	    
	    setSpecialFont(f);
	    this.setFont(f);
	}

    }

    /**
     * Returns whether or not we should render the default style or a 
     * special style.
     */
    public boolean isSpecial (MailTreeNode node) {
	if (node instanceof FolderNode) 
	    return (node != null && ((FolderNode)node).getFolderInfo().hasUnread());
	else
	    return false;
    }

    public Font getSpecialFont() {
        return specialFont;
    }
    
    public void setSpecialFont(Font newValue) {
        specialFont = newValue;
    }

    public Font getDefaultFont() {
	return defaultFont;
    }
    
    public void setDefaultFont(Font newValue) {
	defaultFont = newValue;
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
	    retDimension = new Dimension((int)((retDimension.width + 3) * 1.2),
					 retDimension.height);
	return retDimension;
    }

} //end class DefaultFolderTreeCellRenderer

