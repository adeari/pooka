package net.suberic.pooka.gui;
import net.suberic.util.gui.PropertyEditorFactory;
import net.suberic.pooka.Pooka;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This is an implementation of PookaUIFactory which creates a single
 * panel which shows the list of messages in the folder and a preview
 * pane which shows the message itself.  You should also be able to
 * open messages in individual Frames.  New messages go into individual
 * Frames, also.
 */
public class PookaPreviewPaneUIFactory implements PookaUIFactory {
   
    PreviewContentPanel contentPanel = null;
    PropertyEditorFactory editorFactory = null;

    /**
     * Constructor.
     */
    public PookaPreviewPaneUIFactory() {
	editorFactory = new PookaExternalPropertyEditorFactory(Pooka.getResources());
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

	PreviewFolderPanel fw = new PreviewFolderPanel(contentPanel, fi);
	contentPanel.addPreviewPanel(fw, fi.getFolderID());
	return fw;
	    
    }

    /**
     * Creates a JPanel which will be used to show messages and folders.
     *
     * This implementation creates an instance of MessagePanel.
     */
    public ContentPanel createContentPanel() {
	contentPanel = new PreviewContentPanel();
	contentPanel.setSize(1000,1000);
	
	return contentPanel;
    }

    /**
     * Shows an Editor Window with the given title, which allows the user
     * to edit the values in the properties Vector.  The given properties
     * will be shown according to the values in the templates Vector.
     * Note that there should be an entry in the templates Vector for
     * each entry in the properties Vector.
     */
    public void showEditorWindow(String title, java.util.Vector properties, java.util.Vector templates) {
	JFrame jf = (JFrame)getEditorFactory().createEditorWindow(title, properties, templates);
	jf.show();
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
     * This shows an Confirm Dialog window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
    */    
    public int showConfirmDialog(String messageText, String title, int type) {
	return JOptionPane.showConfirmDialog(contentPanel.getUIComponent(), messageText, title, type);
    }

    /**
     * This shows an Error Message window.  We include this so that
     * the MessageProxy can call the method without caring abou the
     * actual implementation of the Dialog.
     */
    public void showError(String errorMessage, String title) {
	JOptionPane.showMessageDialog(contentPanel.getUIComponent(), errorMessage, title, JOptionPane.ERROR_MESSAGE);
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
	return JOptionPane.showInputDialog(contentPanel.getUIComponent(), inputMessage, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * This shows an Input window.  We include this so that the 
     * MessageProxy can call the method without caring about the actual
     * implementation of the dialog.
     */
    public String showInputDialog(Object[] inputPanes, String title) {
	return JOptionPane.showInputDialog(contentPanel.getUIComponent(), inputPanes, title, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Returns the PropertyEditorFactory used by this component.
     */
    public PropertyEditorFactory getEditorFactory() {
	return editorFactory;
    }

    /**
     * Shows a status message.
     */
    public void showStatusMessage(String newMessage) {
	Pooka.getMainPanel().getInfoPanel().setMessage(newMessage);
    }

    /**
     * Clears the main status message panel.
     */
    public void clearStatus() {
	Pooka.getMainPanel().getInfoPanel().clear();
    }
}
