package net.suberic.pooka.gui;

/**
 * This is an implementation of PookaUIFactory which creates InternalFrame
 * objects on a JDesktopPane.
 */
public class PookaDesktopPaneUIFactory implements PookaUIFactory {
   
    MessagePanel messagePanel = null;

    /**
     * Constructor.
     */
    public PookaDesktopPaneUIFactory(MessagePanel newMessagePanel) {
	messagePanel = newMessagePanel;
    }

    /**
     * Creates an appropriate MessageUI object for the given MessageProxy.
     */
    public MessageUI createMessageUI(MessageProxy mp) {
	// each MessageProxy can have exactly one MessageUI.
	if (mp.getMessageUI() != null)
	    return mp.getMessageUI();
	
	MessageUI mui;
	if (mp instanceof NewMessageProxy) {
	    mui = new NewMessageWindow(getMessagePanel(), (NewMessageProxy) mp);
	} else
	    mui = new ReadMessageInternalFrame(getMessagePanel(), mp);
	
	return mui;
    }

    /**
     * Creates an appropriate FolderDisplayUI object for the given
     * FolderInfo.
     */
    public FolderDisplayUI createFolderDisplayUI(net.suberic.pooka.FolderInfo fi) {
	// a FolderInfo can only have one FolderDisplayUI.
	
	if (fi.getFolderDisplayUI() != null)
	    return fi.getFolderDisplayUI();

	FolderDisplayUI fw = new FolderWindow(fi, getMessagePanel());
	return fw;
    }

    /**
     * Returns the MessagePanel associated with this Factory.
     */
    public MessagePanel getMessagePanel() {
	return messagePanel;
    }
}
