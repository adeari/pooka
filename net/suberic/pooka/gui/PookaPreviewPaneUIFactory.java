package net.suberic.pooka.gui;

/**
 * This is an implementation of PookaUIFactory which creates a single
 * panel which shows the list of messages in the folder and a preview
 * pane which shows the message itself.  You should also be able to
 * open messages in individual Frames.  New messages go into individual
 * Frames, also.
 */
public class PookaPreviewPaneUIFactory implements PookaUIFactory {
   
    MessagePanel messagePanel = null;

    /**
     * Constructor.
     */
    public PookaPreviewPaneUIFactory(MessagePanel newMessagePanel) {
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
	    mui = new NewMessageFrame((NewMessageProxy) mp);
	} else
	    mui = new ReadMessageFrame(mp);
	
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
