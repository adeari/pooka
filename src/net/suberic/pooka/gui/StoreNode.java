package net.suberic.pooka.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JComponent;
import javax.mail.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.StoreInfo;
import net.suberic.util.thread.ActionWrapper;
import net.suberic.pooka.gui.search.*;
import net.suberic.pooka.gui.filechooser.*;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.*;
import javax.mail.event.*;


public class StoreNode extends MailTreeNode {
  
  protected StoreInfo store = null;
  protected String displayName = null;
  protected boolean hasLoaded = false;
  
  public StoreNode(StoreInfo newStore, JComponent parent) {
    super(newStore, parent);
    store = newStore;
    newStore.setStoreNode(this);
    displayName=Pooka.getProperty("Store." + store.getStoreID() + ".displayName", store.getStoreID());
    setCommands();
    loadChildren();
    defaultActions = new Action[] {
      new ActionWrapper(new OpenAction(), getStoreInfo().getStoreThread()),
      new SubscribeAction(),
      new TestAction(),
      new NewFolderAction(),
      new ActionWrapper(new DisconnectAction(), getStoreInfo().getStoreThread()),
      new EditAction(),
      new StatusAction()
    };
    
  }
  
  /**
   * this method returns false--a store is never a leaf.
   */
  public boolean isLeaf() {
    return false;
  }
  
  
  /**
   * This loads or updates the top-level children of the Store.
   */
  public void loadChildren() {
    Runnable runMe = new Runnable() {
	public void run() {
	  doLoadChildren();
	}
      };
    
    if (SwingUtilities.isEventDispatchThread())
      doLoadChildren();
    else {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception ie) {
      }
    }
  }
  
  /**
   * Does the actual work for loading the children.  performed on the swing
   * gui thread.
   */
  private void doLoadChildren() {
    if (Pooka.isDebug())
      System.out.println("calling loadChildren() for " + getStoreInfo().getStoreID());
    
    Enumeration origChildren = super.children();
      Vector origChildrenVector = new Vector();
      while (origChildren.hasMoreElements())
	origChildrenVector.add(origChildren.nextElement());
      
      if (Pooka.isDebug())
	System.out.println(getStoreInfo().getStoreID() + ":  origChildrenVector.size() = " + origChildrenVector.size());
      
      Vector storeChildren = getStoreInfo().getChildren();
      
      if (Pooka.isDebug())
	System.out.println(getStoreInfo().getStoreID() + ":  storeChildren.size() = " + storeChildren.size());
      
      if (storeChildren != null) {
	for (int i = 0; i < storeChildren.size(); i++) {
	  FolderNode node = popChild(((FolderInfo)storeChildren.elementAt(i)).getFolderName(), origChildrenVector);
	  if (node == null) {
	    node = new FolderNode((FolderInfo)storeChildren.elementAt(i), getParentContainer());
	    // we used insert here, since add() would mak
	    // another recursive call to getChildCount();
	    insert(node, 0);
	  }
	}
	
      }
      
      removeChildren(origChildrenVector);
      
      hasLoaded=true;
      
      
      javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
      if (folderTree != null && folderTree.getModel() instanceof javax.swing.tree.DefaultTreeModel) {
	((javax.swing.tree.DefaultTreeModel)folderTree.getModel()).nodeStructureChanged(this);
      }
      
	/*
	java.util.Vector storeChildren = getStoreInfo().getChildren();
    
	if (storeChildren != null)
	    for (int i = 0 ; i < storeChildren.size() ; i++) {
		FolderNode node = new FolderNode((FolderInfo)storeChildren.elementAt(i), getParentContainer());
		// we used insert here, since add() would make
		// another recursive call to getChildCount();
		insert(node, i);
	    } 
	
	hasLoaded=true;

	javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
	if (folderTree != null && folderTree.getModel() instanceof javax.swing.tree.DefaultTreeModel) {
	    ((javax.swing.tree.DefaultTreeModel)folderTree.getModel()).nodeStructureChanged(this);
	}
	*/
    }

  /**
   * This goes through the Vector of FolderNodes provided and 
   * returns the FolderNode for the given childName, if one exists.
   * It will also remove the Found FolderNode from the childrenList
   * Vector.
   *
   * If a FolderNode that corresponds with the given childName does
   * not exist, this returns null.
   *
   */
  public FolderNode popChild(String childName, Vector childrenList) {
    if (children != null) {
      for (int i = 0; i < childrenList.size(); i++)
	if (((FolderNode)childrenList.elementAt(i)).getFolderInfo().getFolderName().equals(childName)) {
	  FolderNode fn = (FolderNode)childrenList.elementAt(i);
	  childrenList.remove(fn);
	  return fn;
	}
    }
    
    // no match.
    return null;
  }
  
  /**
   * This removes all the items in removeList from the list of this 
   * node's children.
   */
  public void removeChildren(Vector removeList) {
    for (int i = 0; i < removeList.size(); i++) {
      if (removeList.elementAt(i) instanceof javax.swing.tree.MutableTreeNode)
	this.remove((javax.swing.tree.MutableTreeNode)removeList.elementAt(i));
    }
  }
  
  /**
   * This  creates the current PopupMenu if there is not one.  It then
   * will configure the PopupMenu with the current actions.
   *
   * Overrides MailTreeNode.configurePopupMenu();
   */
  
  public void configurePopupMenu() {
    if (popupMenu == null) {
      popupMenu = new net.suberic.util.gui.ConfigurablePopupMenu();
      popupMenu.configureComponent("StoreNode.popupMenu", Pooka.getResources());
      updatePopupTheme();
    }
    
    popupMenu.setActive(getActions());
    
  }
  
  /**
   * This opens up a dialog asking if the user wants to subscribe to a 
   * subfolder.
   */
  public void newFolder() {
    String message = Pooka.getProperty("Folder.newFolder", "Subscribe/create new subfolder of") + " " + getStoreInfo().getStoreID();
    
    JLabel messageLabel = new JLabel(message);
    
    JPanel typePanel = new JPanel();
    typePanel.setBorder(BorderFactory.createEtchedBorder());
    
    JRadioButton messagesButton = new JRadioButton(Pooka.getProperty("Folder.new.messages.label", "Contains Messages"), true);
    JRadioButton foldersButton = new JRadioButton(Pooka.getProperty("Folder.new.folders.label", "Contains Folders"));
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(messagesButton);
    bg.add(foldersButton);
    
    typePanel.add(messagesButton);
    typePanel.add(foldersButton);
    
    Object[] inputPanels = new Object[] {
      messageLabel,
      typePanel
    };
    
    final String response = Pooka.getUIFactory().showInputDialog(inputPanels, Pooka.getProperty("Folder.new.title", "Create new Folder"));
    
    int type = javax.mail.Folder.HOLDS_MESSAGES;
    if (foldersButton.isSelected()) {
      type = javax.mail.Folder.HOLDS_FOLDERS;
    }
    
    final int finalType = type;
    
    if (response != null && response.length() > 0) {
      getStoreInfo().getStoreThread().addToQueue(new javax.swing.AbstractAction() {
	  public void actionPerformed(java.awt.event.ActionEvent e) {
	    try {
	      getStoreInfo().createSubFolder(response, finalType);
	    } catch (MessagingException me) {
	      final Exception fme = me;
	      SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
		    Pooka.getUIFactory().showError(fme.getMessage());
		  }
		});
	      
	      me.printStackTrace();
	    }
	  }
	} , new java.awt.event.ActionEvent(this, 0, "folder-new"));
    }
  }
  
  public String getStoreID() {
    if (store != null)
      return store.getStoreID();
    else
      return null;
  }
  
  public StoreInfo getStoreInfo() {
    return store;
  }
  
  /**
   * We override toString() so we can display the store URLName
   * without the password.
   */
  
  public String toString() {
    return displayName;
  }
  
  public boolean isConnected() {
    if (store != null) {
      return store.isConnected();
    } else 
      return false;
  }
  
  public Action[] defaultActions;
  
  public Action[] getDefaultActions() {
    return defaultActions;
  }
  
  class OpenAction extends AbstractAction {
    
    OpenAction() {
      super("file-open");
    }
    
    OpenAction(String nm) {
      super(nm);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (!store.isConnected())
	try {
	  store.connectStore();
	} catch (MessagingException me) {
	  // I should make this easier.
	  Pooka.getUIFactory().showError(Pooka.getProperty("error.Store.connectionFailed", "Failed to open connection to Mail Store.") + "\n" + Pooka.getProperty("error.sourceException", "The underlying exception reads:  ") + "\n" + me.getMessage());
	}
      javax.swing.JTree folderTree = ((FolderPanel)getParentContainer()).getFolderTree();
      folderTree.expandPath(folderTree.getSelectionPath());
    }
  }
  
  class SubscribeAction extends AbstractAction {
    
    SubscribeAction() {
      super("folder-subscribe");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {

      JFileChooser jfc =
	new JFileChooser(getStoreInfo().getStoreID(), new net.suberic.pooka.gui.filechooser.MailFileSystemView(getStoreInfo()));
      jfc.setMultiSelectionEnabled(true);
      jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      int returnValue =
	jfc.showDialog(getParentContainer(),
		       Pooka.getProperty("FolderEditorPane.Select",
					 "Select"));
      if (returnValue == JFileChooser.APPROVE_OPTION) {
	final net.suberic.pooka.gui.filechooser.FolderFileWrapper wrapper =
	  ((net.suberic.pooka.gui.filechooser.FolderFileWrapper)jfc.getSelectedFile());
	getStoreInfo().getStoreThread().addToQueue(new javax.swing.AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent ae) {
	      try {
		// if it doesn't exist, try to create it.
		if (! wrapper.exists()) {
		  wrapper.getFolder().create(Folder.HOLDS_MESSAGES);
		}
		String absFileName = wrapper.getAbsolutePath();
		int firstSlash = absFileName.indexOf('/');
		String normalizedFileName = absFileName;
		if (firstSlash >= 0)
		  normalizedFileName = absFileName.substring(firstSlash);
		
		if (Pooka.isDebug()) 
		  System.out.println("adding folder " + normalizedFileName);
		
		getStoreInfo().subscribeFolder(normalizedFileName);
	      } catch (MessagingException me) {
		final String folderName = wrapper.getName();
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      Pooka.getUIFactory().showError(Pooka.getProperty("error.creatingFolder", "Error creating folder ") + folderName);
		    }
		  });
	      }
	    }
	  },  new java.awt.event.ActionEvent(this, 0, "message-refresh"));
	
      }
    }
  }
    
  class TestAction extends AbstractAction {
    
    TestAction() {
      super("file-test");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      
    }
    
  }
  
  class DisconnectAction extends AbstractAction {
    
    DisconnectAction() {
      super("file-close");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
	getStoreInfo().disconnectStore();
      } catch (Exception ex) {
	System.out.println("caught exception:  " + ex.getMessage());
      }
    }
  }

  class NewFolderAction extends AbstractAction {
    
    NewFolderAction() {
      super("folder-new");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      newFolder();
    }
    
  }
  
  class EditAction extends AbstractAction {
    
    EditAction() {
      super("file-edit");
    }
    
    EditAction(String nm) {
      super(nm);
    }
	
    public void actionPerformed(java.awt.event.ActionEvent e) {
      Pooka.getUIFactory().showEditorWindow(getStoreInfo().getStoreProperty(), getStoreInfo().getStoreProperty(), "Store.editableFields.editor");
    }
  }    

  class StatusAction extends AbstractAction {
    
    StatusAction() {
      super("store-status");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      getStoreInfo().showStatus();
    }
  }    
}

