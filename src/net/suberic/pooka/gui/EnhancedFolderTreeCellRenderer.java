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

  Icon rootIcon;
  Icon connectedIcon;
  Icon disconnectedIcon;
  Icon closedFolderIcon;
  Icon unavailableIcon;
  Icon connectedStoreIcon;
  Icon disconnectedStoreIcon;
  Icon closedStoreIcon;
  Icon subfolderIcon;
  Icon subfolderWithNewIcon;
  Icon connectedWithNewIcon;
  Icon disconnectedWithNewIcon;
  
  /**
   * Creates the EnhancedFolderTreeCellRenderer.
   */
  public EnhancedFolderTreeCellRenderer() {
    super();
  }

  /**
   * gets the renderer component.
   */
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
	setDisabledIcon(getClosedFolderIcon());
      }
    }
    else {
      setEnabled(true);
      if (leaf) {
	setIcon(getLeafIcon());
      } else if (expanded) {
	setIcon(getOpenIcon());
      } else {
	setIcon(getClosedFolderIcon());
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
	  setIconToClosedFolder();
	} else {
	  //System.out.println("folderInfo is " + folderInfo.getFolderID() + "; hasNewMessages is " + folderInfo.hasNewMessages() + "; notifyNewMessagesNode is "+ folderInfo.notifyNewMessagesNode());
	  if (!((FolderNode)node).isLeaf()) {
	    //System.out.println("folderInfo is " + folderInfo.getFolderID() + "; hasNewMessages is " + folderInfo.hasNewMessages());
	    if (folderInfo.hasNewMessages() && folderInfo.notifyNewMessagesNode())
	      setIconToSubfolderWithNew();
	    else 
	      setIconToSubfolder();
	  } else if (folderInfo.isConnected()) {
	    if (folderInfo.notifyNewMessagesNode() && folderInfo.hasNewMessages()) {
	      setIconToOpenWithNew();
	    } else
	      setIconToOpen();
	  } else if (folderInfo.isSortaOpen()) {
	    if (folderInfo.notifyNewMessagesNode() && folderInfo.hasNewMessages()) {
	      setIconToDisconnectedWithNew();
	    } else
	      setIconToDisconnected();
	  } else if (!folderInfo.isValid()) {
	    setIconToUnavailable();
	  } else {
	    setIconToClosedFolder();
	  }
	}
      } else if (lastPath instanceof StoreNode) {
	StoreInfo storeInfo = ((StoreNode)lastPath).getStoreInfo();
	if (storeInfo.isConnected())
	  setIconToConnectedStore();
	else
	  setIconToDisconnectedStore();
	
	setFontToDefault();
      } else {
	setIconToRoot();
      }
    } else {
      setIconToDisconnected();
    }
    return this;
  }
  
  /**
   * Sets the icon to the unavailable icon.
   */
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
  
  /**
   * Sets the icon to the open icon.
   */
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
  
  /**
   * Sets the icon to open with new.
   */
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
  
  /**
   * Sets the icon to the disconnected icon.
   */
  public void setIconToDisconnected() {
    if (getDisconnectedIcon() != null)
      setIcon(getDisconnectedIcon());
    else {
      // create the new Icon.
      java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.disconnectedIcon", "images/PlusMinus.gif"));
      if (url != null) {
	setDisconnectedIcon(new ImageIcon(url));
	setIcon(getDisconnectedIcon());
      }
    }
  }
  
  /**
   * Sets the icon to disconnected with new.
   */
  public void setIconToDisconnectedWithNew() {
    if (getDisconnectedWithNewIcon() != null)
      setIcon(getDisconnectedWithNewIcon());
    else {
      // create the new Icon.
      java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.disconnectedWithNewIcon", "images/PlusNew.gif"));
      if (url != null) {
	setDisconnectedWithNewIcon(new ImageIcon(url));
	setIcon(getDisconnectedWithNewIcon());
      }
    }
  }
  
  public void setIconToClosedFolder() {
    if (getClosedFolderIcon() != null)
      setIcon(getClosedFolderIcon());
    else {
      // create the new Icon.
      java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.closedFolderIcon", "images/Minus.gif"));
      if (url != null) {
	setClosedFolderIcon(new ImageIcon(url));
	setIcon(getClosedFolderIcon());
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
      java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.subfolderIcon", "/org/javalobby/icons/20x20png/Folder.png"));
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
      java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.subfolderWithNewIcon", "images/FolderNew.png"));
      if (url != null) {
	setSubfolderWithNewIcon(new ImageIcon(url));
	setIcon(getSubfolderWithNewIcon());
      }
    }
  }
  
  public void setIconToRoot() {
    if (getRootIcon() != null)
      setIcon(getRootIcon());
    else {
      // create the new Icon.
      java.net.URL url = this.getClass().getResource(Pooka.getProperty("FolderTree.rootIcon", "images/PookaFolderIcon.gif"));
      if (url != null) {
	setRootIcon(new ImageIcon(url));
	setIcon(getRootIcon());
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
  
  public void setConnectedWithNewIcon(Icon newIcon) {
    connectedWithNewIcon = newIcon;
  }

  public void setDisconnectedIcon(Icon newIcon) {
    disconnectedIcon = newIcon;
  }
  
  public Icon getDisconnectedIcon() {
    return disconnectedIcon;
  }
  public Icon getDisconnectedWithNewIcon() {
    return disconnectedWithNewIcon;
  }
  
  public Icon getSubfolderWithNewIcon() {
    return subfolderWithNewIcon;
  }
  
  public void setSubfolderWithNewIcon(Icon newIcon) {
    subfolderWithNewIcon = newIcon;
  }
  
  public void setDisconnectedWithNewIcon(Icon newIcon) {
    disconnectedWithNewIcon = newIcon;
  }
  
  public Icon getClosedFolderIcon() {
    return closedFolderIcon;
  }
  
  public void setClosedFolderIcon(Icon newIcon) {
    closedFolderIcon = newIcon;
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

  public Icon getRootIcon() {
    return rootIcon;
  }
  
  public void setRootIcon(Icon newIcon) {
    rootIcon = newIcon;
  }
}


