package net.suberic.pooka.gui;
import net.suberic.util.gui.PropertyEditorFactory;
import net.suberic.pooka.Pooka;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

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
    public PookaDesktopPaneUIFactory() {
	
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
     * Creates a JPanel which will be used to show messages and folders.
     *
     * This implementation creates an instance of MessagePanel.
     */
    public ContentPanel createContentPanel() {
	messagePanel = new MessagePanel(Pooka.getMainPanel());
	messagePanel.setSize(1000,1000);
	JScrollPane messageScrollPane = new JScrollPane(messagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	messagePanel.setDesktopManager(messagePanel.new ExtendedDesktopManager(messagePanel, messageScrollPane));
	messagePanel.setUIComponent(messageScrollPane);
	
	return messagePanel;
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

    /**
     * Shows a Confirm dialog.
     */
    public int showConfirmDialog(String message, String title, int type) {
	return JOptionPane.showInternalConfirmDialog(messagePanel, message, title, type);
    }

    /**
     * This shows an Error Message window.
     */
    public void showError(String errorMessage, String title) {
	JOptionPane.showInternalMessageDialog(getMessagePanel(), errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This shows an Error Message window.  
     */
    public void showError(String errorMessage) {
	showError(errorMessage, Pooka.getProperty("Error", "Error"));
    }

    /**
     * This shows an Error Message window.
     */
    public void showError(String errorMessage, String title, Exception e) {
	showError(errorMessage + e.getMessage(), title);
	e.printStackTrace();
    }

    /**
     * This shows an Input window.
     */
    public String showInputDialog(String inputMessage, String title) {
	return JOptionPane.showInternalInputDialog(getMessagePanel(), inputMessage, title, JOptionPane.QUESTION_MESSAGE);
    }
    
}
