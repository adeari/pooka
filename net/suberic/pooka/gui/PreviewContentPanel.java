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
public class PreviewContentPanel extends JPanel implements ContentPanel {

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

	this.setPreferredSize(new java.awt.Dimension(Integer.parseInt(Pooka.getProperty("Pooka.messagePanel.hsize", "600")), Integer.parseInt(Pooka.getProperty("Pooka.messagePanel.vsize", Pooka.getProperty("Pooka.vsize","570")))));
	this.setSize(getPreferredSize());
	folderDisplay.setPreferredSize(new java.awt.Dimension(Integer.parseInt(Pooka.getProperty("Pooka.messagePanel.hsize", "600")), Integer.parseInt(Pooka.getProperty("Pooka.folderDisplay.vsize", "200"))));
	folderDisplay.setSize(folderDisplay.getPreferredSize());

	selectionListener = new ListSelectionListener() {
		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
		    selectedMessageChanged();
		}
	    };

	this.setSavingOpenFolders(Pooka.getProperty("Pooka.saveOpenFoldersOnExit", "false").equalsIgnoreCase("true"));
	
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
		messageDisplay.setMessageProxy(mp);
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
     * Gets the actio s for the current component, if any.
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

}

