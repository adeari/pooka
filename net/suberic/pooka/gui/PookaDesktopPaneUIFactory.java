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
     * Shows an Editor Window with the given title, which allows the user
     * to edit the values in the properties Vector.  The given properties
     * will be shown according to the values in the templates Vector.
     * Note that there should be an entry in the templates Vector for
     * each entry in the properties Vector.
     */
    public void showEditorWindow(String title, java.util.Vector properties, java.util.Vector templates) {
	JInternalFrame jif = (JInternalFrame)getEditorFactory().createEditorWindow(title, properties, templates);
	getMessagePanel().add(jif);
	jif.setVisible(true);
	try {
	    jif.setSelected(true);
	} catch (java.beans.PropertyVetoException pve) {
	}
	    
    }

    /**
     * Shows an Editor Window with the given title, which allows the user
     * to edit the values in the properties Vector.
     */
    public void showEditorWindow(String title, java.util.Vector properties) {
	showEditorWindow(title, properties, properties);
    }

    /**
     * Shows an Editor Window with the given title, which allows the user
     * to edit the given property.
     */
    public void showEditorWindow(String title, String property) {
	java.util.Vector v = new java.util.Vector();
	v.add(property);
	showEditorWindow(title, v, v);
    }

    /**
     * Shows an Editor Window with the given title, which allows the user
     * to edit the given property, which is in turn defined by the 
     * given template.
     */
    public void showEditorWindow(String title, String property, String template) {
	java.util.Vector prop = new java.util.Vector();
	prop.add(property);
	java.util.Vector templ = new java.util.Vector();
	templ.add(template);
	showEditorWindow(title, prop, templ);
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

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(Object[] inputPanes, String title) {
	return JOptionPane.showInternalInputDialog((MessagePanel)Pooka.getMainPanel().getContentPanel(), inputPanes, title, JOptionPane.QUESTION_MESSAGE);
    }
    
}
