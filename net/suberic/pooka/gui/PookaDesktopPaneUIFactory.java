package net.suberic.pooka.gui;
import net.suberic.util.gui.PropertyEditorFactory;
import net.suberic.pooka.Pooka;
import javax.swing.JInternalFrame;


/**
 * This is an implementation of PookaUIFactory which creates InternalFrame
 * objects on a JDesktopPane.
 */
public class PookaDesktopPaneUIFactory implements PookaUIFactory {
   
    MessagePanel messagePanel = null;
    PropertyEditorFactory editorFactory = null;
    
    /**
     * Constructor.
     */
    public PookaDesktopPaneUIFactory(MessagePanel newMessagePanel) {
	messagePanel = newMessagePanel;

	editorFactory = new PookaDesktopPropertyEditorFactory(Pooka.getResources());
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
	    mui = new NewMessageInternalFrame(getMessagePanel(), (NewMessageProxy) mp);
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

	FolderDisplayUI fw = new FolderInternalFrame(fi, getMessagePanel());
	return fw;
    }

    /**
     * Shows an Editor Window for the properties in the properties 
     * Vector with the given title.
     */
    public void showEditorWindow(String title, java.util.Vector properties) {
	JInternalFrame jif = (JInternalFrame)getEditorFactory().createEditorWindow(title, properties);
	getMessagePanel().add(jif);
	jif.setVisible(true);
	try {
	    jif.setSelected(true);
	} catch (java.beans.PropertyVetoException pve) {
	}
	    
    }

    /**
     * Returns the MessagePanel associated with this Factory.
     */
    public MessagePanel getMessagePanel() {
	return messagePanel;
    }

    /**
     * Returns the PropertyEditorFactory used by this component.
     */
    public PropertyEditorFactory getEditorFactory() {
	return editorFactory;
    }
}
