package net.suberic.pooka.gui;
import java.awt.CardLayout;
import javax.swing.*;
import net.suberic.pooka.UserProfile;
import java.io.IOException;
import java.util.HashMap;
import net.suberic.pooka.Pooka;
import net.suberic.util.gui.*;
import java.awt.BorderLayout;
import javax.swing.event.ListSelectionListener;

/**
 * A Content Panel which shows a JSplitPane, with a PreviewFolderPanel in
 * the top section and a PreviewMessagePanel in the bottom section.
 */
public class PreviewContentPanel extends JPanel implements ContentPanel, MessageUI {

    private JPanel folderDisplay = null;
    private ReadMessageDisplayPanel messageDisplay;
    private JPanel messageCardPanel;

    private JSplitPane splitPanel;

    private PreviewFolderPanel current = null;

    private ConfigurableToolbar toolbar;
    HashMap cardTable = new HashMap();

    private ListSelectionListener selectionListener;

    private boolean savingOpenFolders;

    /**
     * Creates a new PreviewContentPanel.
     */
    public PreviewContentPanel() {
	folderDisplay = new JPanel();
	folderDisplay.setLayout(new CardLayout());
	folderDisplay.add("emptyPanel", new JPanel());
	
	messageDisplay = new ReadMessageDisplayPanel();

	try {
	  messageDisplay.configureMessageDisplay();
	} catch (javax.mail.MessagingException me) {
	  // showError();
	}

	splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, folderDisplay, messageDisplay);

	
	toolbar = new ConfigurableToolbar("FolderWindowToolbar", Pooka.getResources());

	this.setLayout(new BorderLayout());

	this.add("North", toolbar);
	this.add("Center", splitPanel);

	this.setPreferredSize(new java.awt.Dimension(Integer.parseInt(Pooka.getProperty("Pooka.contentPanel.hsize", "600")), Integer.parseInt(Pooka.getProperty("Pooka.contentPanel.vsize", Pooka.getProperty("Pooka.vsize","570")))));
	this.setSize(getPreferredSize());
	folderDisplay.setPreferredSize(new java.awt.Dimension(Integer.parseInt(Pooka.getProperty("Pooka.contentPanel.hsize", "600")), Integer.parseInt(Pooka.getProperty("Pooka.contentPanel.dividerLocation", "200"))));
	folderDisplay.setSize(folderDisplay.getPreferredSize());

	splitPanel.setDividerLocation(Integer.parseInt(Pooka.getProperty("Pooka.contentPanel.dividerLocation", "200")));

	selectionListener = new ListSelectionListener() {
		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
		    selectedMessageChanged();
		}
	    };

	this.setSavingOpenFolders(Pooka.getProperty("Pooka.saveOpenFoldersOnExit", "false").equalsIgnoreCase("true"));
	
    }

  /**
   * Creates a new PreviewContentPanel from an existing MessagePanel.
   */
  public PreviewContentPanel(MessagePanel mp) {
    this();
    
    // go through each window on the MessagePanel.
    
    JInternalFrame[] frames = mp.getAllFrames();
    
    String selectedID = null;
    
    for (int i = 0; i < frames.length; i++) {
      if (frames[i] instanceof FolderInternalFrame) {
	PreviewFolderPanel newPP = new PreviewFolderPanel(this, (FolderInternalFrame) frames[i]);
	net.suberic.pooka.FolderInfo fi = newPP.getFolderInfo();
	String folderID = fi.getFolderID();
	fi.setFolderDisplayUI(newPP);
	addPreviewPanel(newPP, folderID);
	if (frames[i].isSelected()) {
	  selectedID = folderID;
	} else if (selectedID == null) {
	  // if it gets overriden later, that's great.
	  selectedID = folderID;
	}
      } else if (frames[i] instanceof MessageInternalFrame) {
	if (frames[i].isSelected()) {
	  if (frames[i] instanceof ReadMessageInternalFrame) {
	    selectedID = ((ReadMessageInternalFrame) frames[i]).getMessageProxy().getMessageInfo().getFolderInfo().getFolderID();
	  }
	}
	((MessageInternalFrame) frames[i]).detachWindow();
      }
    }
    
    if (selectedID != null)
      showFolder(selectedID);
  }
  
  public void savePanelSize() {
    Pooka.setProperty("Pooka.contentPanel.hsize", Integer.toString(getUIComponent().getWidth()));
    Pooka.setProperty("Pooka.contentPanel.vsize", Integer.toString(getUIComponent().getHeight()));
    Pooka.setProperty("Pooka.contentPanel.dividerLocation", Integer.toString(splitPanel.getDividerLocation()));
  }
  
  /**
   * Shows the PreviewFolderPanel indicated by the given FolderId.
   */
  public void showFolder(String folderId) {
    if (current != null) {
      current.getFolderDisplay().getMessageTable().getSelectionModel().removeListSelectionListener(selectionListener);
    }
    current = (PreviewFolderPanel) cardTable.get(folderId);
    
    ((CardLayout)folderDisplay.getLayout()).show(folderDisplay, folderId);
    if (current != null) {
      current.getFolderDisplay().getMessageTable().getSelectionModel().addListSelectionListener(selectionListener);
    }
    
    selectedMessageChanged();
    folderDisplay.repaint();
  }
  
  /**
   * This should be called every time the selected message changes.
   */
  public void selectedMessageChanged() {
    refreshCurrentMessage();
    refreshActiveMenus();
    refreshCurrentUser();
  }
  
  /**
   * This refreshes the currently previewed message.
   */
  public void refreshCurrentMessage() {
    if (current != null) {
      MessageProxy mp = current.getFolderDisplay().getSelectedMessage();
      if (! (mp instanceof MultiMessageProxy)) {
	messageDisplay.setMessageUI(this);
	try {
	  messageDisplay.resetEditorText();
	  if (mp != null && mp.getMessageInfo() != null)
	    mp.getMessageInfo().setSeen(true);
	} catch (javax.mail.MessagingException me) {
	  //showError();
	}
      }
    }
  }

  /**
   * Registers a PreviewFolderPanel for a particular FolderID.
   */
  public void addPreviewPanel(PreviewFolderPanel newPanel, String folderId) {
    cardTable.put(folderId, newPanel);
    folderDisplay.add(newPanel, folderId);
  }
  
  /**
   * Removes the PreviewPanel for a particular FolderID.
   */
  public void removePreviewPanel(String folderId) {
    PreviewFolderPanel panel = (PreviewFolderPanel)cardTable.get(folderId);
    if (panel != null) {
      folderDisplay.remove(panel);
      cardTable.remove(folderId);
    }
  }
  
  
  /**
   * This gets the FolderInfo associated with the first name in the
   * folderList Vector, and attempts to display the FolderPanel for it.
   *
   * Normally called at startup if Pooka.openSavedFoldersOnStartup
   * is set.
   */
  public void openSavedFolders(java.util.Vector folderList) {
    if (folderList != null && folderList.size() > 0) {
      net.suberic.pooka.FolderInfo fInfo = Pooka.getStoreManager().getFolderById((String)folderList.elementAt(0));  
      if (fInfo != null && fInfo.getFolderNode() != null) {
	FolderNode fNode = fInfo.getFolderNode();
	fNode.makeVisible(); 
	Action a = fNode.getAction("file-open");
	a.actionPerformed(new java.awt.event.ActionEvent(this, 0, "file-open"));
      }
    }
  }
  
  /**
   * Saves the open folder.
   */
  public void saveOpenFolders() {
    if (current != null && current.getFolderInfo() != null) {
      String folderId = current.getFolderInfo().getFolderID();
      Pooka.setProperty("Pooka.openFolderList", folderId);
    }
  }
  
  /**
   * returns whether or not we're saving open folders.
   */
  public boolean isSavingOpenFolders() {
    return savingOpenFolders;
  }
  
  /**
   * sets whether or not we're saving open folders.
   */
  public void setSavingOpenFolders(boolean newValue) {
    savingOpenFolders=newValue;
  }
  
  /**
   * Returns the UI component for this ContentPanel.
   *
   * Returns this object.
   *
   * As specified in interface net.suberic.pooka.gui.ContentPanel.
   */
  public javax.swing.JComponent getUIComponent() {
    return this;
  }
  
  /**
   * Sets the UI component for this ContentPanel.
   *
   * A no-op.  The PreviewContentPanel is always its own UIComponent.
   *
   * As specified in interface net.suberic.pooka.gui.ContentPanel.
   */
  public void setUIComponent(javax.swing.JComponent comp) {
    // no-op.
  }
  
  /**
   * This method shows a help screen.  At the moment, it just takes the
   * given URL, creates a JInteralFrame and a JEditorPane, and then shows
   * the doc with those components.
   */
  public void showHelpScreen(String title, java.net.URL url) {
    JFrame jf = new JFrame(title);
    JEditorPane jep = new JEditorPane();
    try {
      jep.setPage(url);
    } catch (IOException ioe) {
      jep.setText(Pooka.getProperty("err.noHelpPage", "No help available."));
    }
    jep.setEditable(false);
    jf.setSize(500,500);
    jf.getContentPane().add(new JScrollPane(jep));
    jf.show();
  }
  
  /**
   * Returns the currently showing PreviewPanel.
   */
  public PreviewFolderPanel getCurrentPanel() {
    return current;
  }
  
  /**
   * Refreshes the currently available actions.
   */
  public void refreshActiveMenus() {
    toolbar.setActive(getActions());
    Pooka.getMainPanel().refreshActiveMenus();
  }
  
  /**
   * Refreshes the current default Profile.
   */
  public void refreshCurrentUser() {
    Pooka.getMainPanel().refreshCurrentUser();
  }
  
  /**
   * Gets the currently selected MessageProxy, if any.
   */
  public MessageProxy getMessageProxy() {
    if (current != null && current.getFolderDisplay() != null) 
      return current.getFolderDisplay().getSelectedMessage();
    else
      return null;
  }

  /**
   * Opens the current message ui, if any.
   */
  public void openMessageUI() {
    // no-op here.
  }
  
  /**
   * Closes the current message ui, if any.
   */
  public void closeMessageUI() {
    // no-op here.
  }

  public void setBusy(boolean newValue) {
    // no-op here.
  }
  
  public void setEnabled(boolean newValue) {
    // no-op here.
  }

  /**
   * This shows an Confirm Dialog window.  We include this so that
   * the MessageProxy can call the method without caring abou the
   * actual implementation of the Dialog.
   */    
  public int showConfirmDialog(String messageText, String title, int type) {
	return JOptionPane.showConfirmDialog(this, messageText, title, type);
    }

    /**
     * This shows an Confirm Dialog window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */    
    public int showConfirmDialog(String messageText, String title, int optionType, int iconType) {
	return JOptionPane.showConfirmDialog(this, messageText, title, optionType, iconType);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title) {
	JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage) {
	showError(errorMessage, Pooka.getProperty("Error", "Error"));
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, Exception e) {
	showError(errorMessage, Pooka.getProperty("Error", "Error"), e);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring about the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title, Exception e) {
	showError(errorMessage + e.getMessage(), title);
	e.printStackTrace();
    }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(String inputMessage, String title) {
	return JOptionPane.showInputDialog(this, inputMessage, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(Object[] inputPanes, String title) {
	return JOptionPane.showInputDialog(this, inputPanes, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * This shows a Message window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
  public void showMessageDialog(String message, String title) {
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
  }
  
  /**
   * Gets the actions for the current component, if any.
   */
  public Action[] getActions() {
    Action[] returnValue = null;
    if (current != null)
      returnValue = current.getActions();
    
    if (returnValue == null)
      return messageDisplay.getActions();
    else {
      if (messageDisplay.getActions() != null)
	return javax.swing.text.TextAction.augmentList(returnValue, messageDisplay.getActions());
      else
	return returnValue;
    }
  }

    /**
     * Get the default profile for the current component, if any.
     */
    public UserProfile getDefaultProfile() {
	if (current != null)
	    return current.getDefaultProfile();
	else if (messageDisplay != null)
	    return messageDisplay.getDefaultProfile();
	else
	    return null;
    }

    public HashMap getCardTable() {
	return cardTable;
    }

    public ListSelectionListener getSelectionListener() {
	return selectionListener;
    }
}

