package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.tree.*;
import java.awt.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

/**
 * This is basically an extension of DefaultFolderTreeCellRenderer that has 
 * icon support added.
 */
public class EnhancedFolderTreeCellRenderer extends DefaultFolderTreeCellRenderer {

    protected boolean hasFocus;

    Icon connectedIcon;
    Icon disconnectedIcon;
    Icon unavailableIcon;
    Icon connectedStoreIcon;
    Icon disconnectedStoreIcon;
    Icon subfolderIcon;
    Icon subfolderWithNewIcon;
    Icon connectedWithNewIcon;

    public EnhancedFolderTreeCellRenderer() {
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

	if (tp != null) {
	    Object lastPath = tp.getLastPathComponent();
	    if (lastPath instanceof FolderNode) {
		FolderNode node = (FolderNode)lastPath;

		FolderInfo fi = node.getFolderInfo();
		
		if (isSpecial(node)) {
		    setFontToSpecial();
		} else {
		    setFontToDefault();
		}

		FolderInfo folderInfo = ((FolderNode)node).getFolderInfo();
		
		if (folderInfo == null){
		    setIconToDisconnected();
		} else {
		    if (!((FolderNode)node).isLeaf()) {
			//System.out.println("folderInfo is " + folderInfo.getFolderID() + "; hasNewMessages is " + folderInfo.hasNewMessages());
			if (folderInfo.hasNewMessages())
			    setIconToSubfolderWithNew();
			else 
			    setIconToSubfolder();
		    } else if (folderInfo.isOpen()) {
			if ((! folderInfo.isTrashFolder()) && (! folderInfo.isSentFolder()) && folderInfo.hasNewMessages()) {
			    setIconToOpenWithNew();
			} else
			    setIconToOpen();
		    } else if (!folderInfo.isAvailable()) {
			setIconToUnavailable();
		    } else {
			setIconToDisconnected();
		    }
		}
	    } else if (lastPath instanceof StoreNode) {
		StoreInfo storeInfo = ((StoreNode)lastPath).getStoreInfo();
		if (storeInfo.isConnected())
		    setIconToConnectedStore();
		else
		    setIconToDisconnectedStore();
		
		setFontToDefault();
	    }
	}
        return this;
    }

    public void setIconToUnavailable() {
	if (getUnavailableIcon() != null)
	    setIcon(getUnavailableIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.unavailableIcon", "images/Delete.gif"));
	    if (url != null) {
		setUnavailableIcon(new ImageIcon(url));
		setIcon(getUnavailableIcon());
	    }
	}
    }

    public void setIconToOpen() {
	if (getConnectedIcon() != null)
	    setIcon(getConnectedIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.connectedIcon", "images/Plus.gif"));
	    if (url != null) {
		setConnectedIcon(new ImageIcon(url));
		setIcon(getConnectedIcon());
	    }
	}
    }

    public void setIconToOpenWithNew() {
	if (getConnectedWithNewIcon() != null)
	    setIcon(getConnectedWithNewIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.connectedWithNewIcon", "images/PlusNew.gif"));
	    if (url != null) {
		setConnectedWithNewIcon(new ImageIcon(url));
		setIcon(getConnectedWithNewIcon());
	    }
	}
    }

    public void setIconToDisconnected() {
	if (getDisconnectedIcon() != null)
	    setIcon(getDisconnectedIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.disconnectedIcon", "images/Minus.gif"));
	    if (url != null) {
		setDisconnectedIcon(new ImageIcon(url));
		setIcon(getDisconnectedIcon());
	    }
	}
    }

    public void setIconToDisconnectedStore() {
	if (getDisconnectedStoreIcon() != null)
	    setIcon(getDisconnectedStoreIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.disconnectedStoreIcon", "images/ClosedMailbox.gif"));
	    if (url != null) {
		setDisconnectedStoreIcon(new ImageIcon(url));
		setIcon(getDisconnectedStoreIcon());
	    }
	}
    }

    public void setIconToConnectedStore() {
	if (getConnectedStoreIcon() != null)
	    setIcon(getConnectedStoreIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.connectedStoreIcon", "images/OpenMailbox.gif"));
	    if (url != null) {
		setConnectedStoreIcon(new ImageIcon(url));
		setIcon(getConnectedStoreIcon());
	    }
	}
    }

    public void setIconToSubfolder() {
	if (getSubfolderIcon() != null)
	    setIcon(getSubfolderIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.subfolderIcon", "images/Folder.gif"));
	    if (url != null) {
		setSubfolderIcon(new ImageIcon(url));
		setIcon(getSubfolderIcon());
	    }
	}
    }

    public void setIconToSubfolderWithNew() {
	if (getSubfolderWithNewIcon() != null)
	    setIcon(getSubfolderWithNewIcon());
	else {
	    // create the new Icon.
	    java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.subfolderWithNewIcon", "images/FolderNew.gif"));
	    if (url != null) {
		setSubfolderWithNewIcon(new ImageIcon(url));
		setIcon(getSubfolderWithNewIcon());
	    }
	}
    }

    public Icon getConnectedIcon() {
	return connectedIcon;
    }

    public void setConnectedIcon(Icon newIcon) {
	connectedIcon = newIcon;
    }

    public Icon getConnectedWithNewIcon() {
	return connectedWithNewIcon;
    }

    public Icon getSubfolderWithNewIcon() {
	return subfolderWithNewIcon;
    }

    public void setSubfolderWithNewIcon(Icon newIcon) {
	subfolderWithNewIcon = newIcon;
    }

    public void setConnectedWithNewIcon(Icon newIcon) {
	connectedWithNewIcon = newIcon;
    }

    public Icon getDisconnectedIcon() {
	return disconnectedIcon;
    }

    public void setDisconnectedIcon(Icon newIcon) {
	disconnectedIcon = newIcon;
    }

    public Icon getUnavailableIcon() {
	return unavailableIcon;
    }

    public void setUnavailableIcon(Icon newIcon) {
	unavailableIcon = newIcon;
    }

    public Icon getConnectedStoreIcon() {
	return connectedStoreIcon;
    }

    public void setConnectedStoreIcon(Icon newIcon) {
	connectedStoreIcon = newIcon;
    }

    public Icon getDisconnectedStoreIcon() {
	return disconnectedStoreIcon;
    }

    public void setDisconnectedStoreIcon(Icon newIcon) {
	disconnectedStoreIcon = newIcon;
    }

    public Icon getSubfolderIcon() {
	return subfolderIcon;
    }

    public void setSubfolderIcon(Icon newIcon) {
	subfolderIcon = newIcon;
    }
}


