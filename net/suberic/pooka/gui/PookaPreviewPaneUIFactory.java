package net.suberic.pooka.gui;
import net.suberic.util.gui.propedit.PropertyEditorFactory;
import net.suberic.util.swing.ThemeManager;
import net.suberic.pooka.*;
import net.suberic.pooka.gui.search.*;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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

  ThemeManager pookaThemeManager = null;

  public boolean showing = false;

    /**
     * Constructor.
     */
    public PookaPreviewPaneUIFactory() {
      editorFactory = new PropertyEditorFactory(Pooka.getResources());
      pookaThemeManager = new ThemeManager("Pooka.theme", Pooka.getResources());
    }

  /**
   * Returns the ThemeManager for fonts and colors.
   */
  public ThemeManager getPookaThemeManager() {
    return pookaThemeManager;
  }

  /**
   * Creates an appropriate MessageUI object for the given MessageProxy.
   */
  public MessageUI createMessageUI(MessageProxy mp) {
    return createMessageUI(mp, null);
  }

  /**
   * Creates an appropriate MessageUI object for the given MessageProxy, 
   * using the provided MessageUI as a guideline.
   *
   * Note that this implementation ignores the mui component.
   */
  public MessageUI createMessageUI(MessageProxy mp, MessageUI templateMui) {
    // each MessageProxy can have exactly one MessageUI.
    if (mp.getMessageUI() != null)
      return mp.getMessageUI();
    
    MessageUI mui;
    if (mp instanceof NewMessageProxy) {
      mui = new NewMessageFrame((NewMessageProxy) mp);
    } else
      mui = new ReadMessageFrame(mp);
    
    mp.setMessageUI(mui);
    return mui;
  }
  
  /**
   * Opens the given MessageProxy in the default manner for this UI.
   * Usually this will just be callen createMessageUI() and openMessageUI()
   * on it.  However, in some cases (Preview Panel without auto display)
   * it may be necessary to act differently.
   *
   */
  public void doDefaultOpen(MessageProxy mp) {
    if (contentPanel.getAutoPreview()) {
      if (mp != null)
	mp.openWindow();
    } else {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    contentPanel.refreshCurrentMessage();
	  }
	});
    }
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
   * This implementation creates an instance of PreviewContentPanel.
   */
  public ContentPanel createContentPanel() {
    contentPanel = new PreviewContentPanel();
    contentPanel.setSize(1000,1000);
	
    return contentPanel;
  }
  
  /**
   * Creates a JPanel which will be used to show messages and folders.
   *
   * This implementation creates an instance PreviewConentPanel from a
   * given MessagePanel.
   */
  public ContentPanel createContentPanel(MessagePanel mp) {
    contentPanel = new PreviewContentPanel(mp);
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
     * Shows a Confirm dialog with the given Object[] as the Message.
     */
    public int showConfirmDialog(Object[] messageComponents, String title, int type) {
	return JOptionPane.showConfirmDialog(contentPanel.getUIComponent(), messageComponents, title, type);
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
     * Shows a message.
     */
    public void showMessage(String newMessage, String title) {
      JOptionPane.showMessageDialog(contentPanel.getUIComponent(), newMessage, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Shows a status message.
     */
    public void showStatusMessage(String newMessage) {
      final String msg = newMessage;
      Runnable runMe = new Runnable() {
	  public void run() {
	    Pooka.getMainPanel().getInfoPanel().setMessage(msg);
	  }
	};
      if (SwingUtilities.isEventDispatchThread())
	runMe.run();
      else
	SwingUtilities.invokeLater(runMe);

    }

  /**
   * Creates a ProgressDialog using the given values.
   */
  public ProgressDialog createProgressDialog(int min, int max, int initialValue, String title, String content) {
    return new ProgressDialogImpl(min, max, initialValue, title, content);
  }

    /**
     * Clears the main status message panel.
     */
    public void clearStatus() {
      Runnable runMe = new Runnable() {
	  public void run() {
	    Pooka.getMainPanel().getInfoPanel().clear();
	  }
	};
      if (SwingUtilities.isEventDispatchThread())
	runMe.run();
      else
	SwingUtilities.invokeLater(runMe);
      
    }

        /**
     * Shows a SearchForm with the given FolderInfos selected from the list
     * of the given allowedValues.
     */
    public void showSearchForm(net.suberic.pooka.FolderInfo[] selectedFolders, java.util.Vector allowedValues) {
	SearchForm sf = null;
	if (allowedValues != null)
	    sf = new SearchForm(selectedFolders, allowedValues);
	else
	    sf = new SearchForm(selectedFolders);
	
	boolean ok = false;
	int returnValue = -1;
	java.util.Vector tmpSelectedFolders = null;
	javax.mail.search.SearchTerm tmpSearchTerm = null;

	while (! ok ) {
	    returnValue = showConfirmDialog(new Object[] { sf }, Pooka.getProperty("title.search", "Search Folders"), JOptionPane.OK_CANCEL_OPTION);
	    if (returnValue == JOptionPane.OK_OPTION) {
	        tmpSelectedFolders = sf.getSelectedFolders();
		try {
		    tmpSearchTerm = sf.getSearchTerm();
		    ok = true;
		} catch (java.text.ParseException pe) {
		    showError(Pooka.getProperty("error.search.invalidDateFormat", "Invalid date format:  "), pe);
		    ok = false;
		}
	    } else {
		ok = true;
	    }
	}
	
	if (returnValue == JOptionPane.OK_OPTION) {
	    FolderInfo.searchFolders(tmpSelectedFolders, tmpSearchTerm);
	}
    }

    /**
     * Shows a SearchForm with the given FolderInfos selected.  The allowed
     * values will be the list of all available Folders.
     */
    public void showSearchForm(net.suberic.pooka.FolderInfo[] selectedFolders) {
	showSearchForm(selectedFolders, null);
    }

    /**
     * This tells the factory whether or not its ui components are showing
     * yet or not.
     */
    public void setShowing(boolean newValue) {
	showing=newValue;
    }
}
