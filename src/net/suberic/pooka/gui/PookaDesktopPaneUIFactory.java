package net.suberic.pooka.gui;
import net.suberic.util.gui.propedit.PropertyEditorFactory;
import net.suberic.util.gui.propedit.DesktopPropertyEditorFactory;
import net.suberic.util.swing.*;
import net.suberic.pooka.*;
import net.suberic.pooka.gui.search.*;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.mail.MessagingException;

/**
 * This is an implementation of PookaUIFactory which creates InternalFrame
 * objects on a JDesktopPane.
 */
public class PookaDesktopPaneUIFactory implements PookaUIFactory {
  
  MessagePanel messagePanel = null;
  PropertyEditorFactory editorFactory = null;
  ThemeManager pookaThemeManager = null;

  public boolean showing = false;

  int maxErrorLine = 50;

    /**
     * Constructor.
     */
    public PookaDesktopPaneUIFactory() {
      pookaThemeManager = new ThemeManager("Pooka.theme", Pooka.getResources());
 
      editorFactory = new DesktopPropertyEditorFactory(Pooka.getResources());
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
  public MessageUI createMessageUI(MessageProxy mp) throws MessagingException {
    return createMessageUI(mp, null);
  }

  /**
   * Creates an appropriate MessageUI object for the given MessageProxy, 
   * using the provided MessageUI as a guideline.
   */
  public MessageUI createMessageUI(MessageProxy mp, MessageUI templateMui) throws javax.mail.MessagingException {
    // each MessageProxy can have exactly one MessageUI.
    if (mp.getMessageUI() != null)
      return mp.getMessageUI();
    
    boolean createExternal = (templateMui != null && templateMui instanceof MessageFrame);

    MessageUI mui;
    if (mp instanceof NewMessageProxy) {
      if (createExternal)
	mui = new NewMessageFrame((NewMessageProxy) mp);
      else
	mui = new NewMessageInternalFrame(getMessagePanel(), (NewMessageProxy) mp);
    } else {
      if (createExternal) {
	mui = new ReadMessageFrame(mp);
      } else {
	mui = new ReadMessageInternalFrame(getMessagePanel(), mp);
	((ReadMessageInternalFrame)mui).configureMessageInternalFrame();
      }
    }
    
    mp.setMessageUI(mui);
    return mui;
  }
  
  /**
   * Opens the given MessageProxy in the default manner for this UI.
   * Usually this will just be callen createMessageUI() and openMessageUI()
   * on it.  However, in some cases (Preview Panel without auto display)
   * it may be necessary to act differently.
   *
   * For this implementation, just calls mp.openWindow().
   */
  public void doDefaultOpen(MessageProxy mp) {
    if (mp != null)
      mp.openWindow();
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
    jif.setLocation(getMessagePanel().getNewWindowLocation(jif, true));
    
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
    
    ((DesktopPropertyEditorFactory) editorFactory).setDesktop(messagePanel);
    return messagePanel;
  }
  
  /**
   * Creates a JPanel which will be used to show messages and folders.
   *
   * This implementation creates an instance of MessagePanel.
   */
  public ContentPanel createContentPanel(PreviewContentPanel pcp) {
    messagePanel = new MessagePanel(Pooka.getMainPanel(), pcp);
    messagePanel.setSize(1000,1000);
    JScrollPane messageScrollPane = new JScrollPane(messagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    messagePanel.setDesktopManager(messagePanel.new ExtendedDesktopManager(messagePanel, messageScrollPane));
    messagePanel.setUIComponent(messageScrollPane);
    
    ((DesktopPropertyEditorFactory) editorFactory).setDesktop(messagePanel);
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
    String displayMessage = formatMessage(message);
    final ResponseWrapper fResponseWrapper = new ResponseWrapper();
    final String fDisplayMessage = displayMessage;
    final String fTitle = title;
    final int fType = type;
    Runnable runMe = new Runnable() {
	public void run() {
	  fResponseWrapper.setInt(JOptionPane.showInternalConfirmDialog(messagePanel, fDisplayMessage, fTitle, fType));
	}
      };
    
    if (! SwingUtilities.isEventDispatchThread()) {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception e) {
      }
    } else {
      runMe.run();
    }

    return fResponseWrapper.getInt();
  }
  
  /**
   * Shows a Confirm dialog with the given Object[] as the Message.
   */
  public int showConfirmDialog(Object[] messageComponents, String title, int type) {
    final ResponseWrapper fResponseWrapper = new ResponseWrapper();
    final Object[] fMessageComponents = messageComponents;
    final String fTitle = title;
    final int fType = type;
    Runnable runMe = new Runnable() {
	public void run() {
	  fResponseWrapper.setInt(JOptionPane.showInternalConfirmDialog(messagePanel, fMessageComponents, fTitle, fType));
	}
      };
    
    if (! SwingUtilities.isEventDispatchThread()) {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception e) {
      }
    } else {
      runMe.run();
    }

    return fResponseWrapper.getInt();
  }
  
  /**
   * This shows an Error Message window.
   */
  public void showError(String errorMessage, String title) {
    final String displayErrorMessage = formatMessage(errorMessage);
    final String fTitle = title;

    if (showing) {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    JOptionPane.showInternalMessageDialog(getMessagePanel(), displayErrorMessage, fTitle, JOptionPane.ERROR_MESSAGE);
	  }
	});
    } else
      System.out.println(errorMessage);
    
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
  public void showError(String errorMessage, Exception e) {
    showError(errorMessage, Pooka.getProperty("Error", "Error"), e);
  }
  
  /**
   * This shows an Error Message window.
   */
  public void showError(String errorMessage, String title, Exception e) {
    final String displayErrorMessage = formatMessage(errorMessage + ":  " + e.getMessage());
    final Exception fE = e;
    final String fTitle = title;
    if (showing) {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    JOptionPane.showInternalMessageDialog(getMessagePanel(), createErrorPanel(displayErrorMessage, fE), fTitle, JOptionPane.ERROR_MESSAGE);
	  }
	});
    } else
      System.out.println(errorMessage);

    //e.printStackTrace();
  }
  
  /**
   * This formats a display message.
   */
  public String formatMessage(String message) {
    return net.suberic.pooka.MailUtilities.wrapText(message, maxErrorLine, "\r\n", 5);
  }

  /**
   * This shows an Input window.
   */
  public String showInputDialog(String inputMessage, String title) {
    final String displayMessage = formatMessage(inputMessage);
    final String fTitle = title;
    final ResponseWrapper fResponseWrapper = new ResponseWrapper();

    Runnable runMe = new Runnable() {
	public void run() {
	  fResponseWrapper.setString(JOptionPane.showInternalInputDialog(getMessagePanel(), displayMessage, fTitle, JOptionPane.QUESTION_MESSAGE));
	}
      };
    
    if (! SwingUtilities.isEventDispatchThread()) {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception e) {
      }
    } else {
      runMe.run();
    }
    
    return fResponseWrapper.getString();
  }
  
  /**
   * This shows an Input window.  We include this so that the 
   * MessageProxy can call the method without caring about the actual
   * implementation of the dialog.
   */
  public String showInputDialog(Object[] inputPanes, String title) {
    final String fTitle = title;
    final Object[] fInputPanes = inputPanes;
    final ResponseWrapper fResponseWrapper = new ResponseWrapper();

    Runnable runMe = new Runnable() {
	public void run() {
	  fResponseWrapper.setString(JOptionPane.showInternalInputDialog(getMessagePanel(), fInputPanes, fTitle, JOptionPane.QUESTION_MESSAGE));
	}
      };
    
    if (! SwingUtilities.isEventDispatchThread()) {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception e) {
      }
    } else {
      runMe.run();
    }
    
    return fResponseWrapper.getString();
  }
  
  /**
   * Shows a message.
   */
  public void showMessage(String newMessage, String title) {
    final String displayMessage = formatMessage(newMessage);
    final String fTitle = title;

    Runnable runMe = new Runnable() {
	public void run() {
	  JTextArea displayPanel = new JTextArea(displayMessage);
	  JScrollPane scrollPane = new JScrollPane(displayPanel);
	  scrollPane.setMaximumSize(new java.awt.Dimension(400,400));

	  JOptionPane.showInternalMessageDialog((MessagePanel)Pooka.getMainPanel().getContentPanel(), scrollPane, fTitle, JOptionPane.PLAIN_MESSAGE);
	  //JOptionPane.showInternalMessageDialog((MessagePanel)Pooka.getMainPanel().getContentPanel(), displayMessage, fTitle, JOptionPane.PLAIN_MESSAGE);
	}
      };

    if (! SwingUtilities.isEventDispatchThread()) {
      try {
	SwingUtilities.invokeAndWait(runMe);
      } catch (Exception e) {
      }
    } else {
      runMe.run();
    }
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
    if (SwingUtilities.isEventDispatchThread()) {
      runMe.run();
    } else
      SwingUtilities.invokeLater(runMe);
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
   * Creates a ProgressDialog using the given values.
   */
  public ProgressDialog createProgressDialog(int min, int max, int initialValue, String title, String content) {
    return new ProgressInternalDialog(min, max, initialValue, title, content, getMessagePanel());
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

  /**
   * Checks to see if the given component is in the main Pooka frame.
   */
  public boolean isInMainFrame(java.awt.Component c) {
    java.awt.Window mainWindow = SwingUtilities.getWindowAncestor(messagePanel);
    java.awt.Window componentWindow = SwingUtilities.getWindowAncestor(c);
    return (mainWindow == componentWindow);
  }

  /**
   * Creates the panels for showing an error message.
   */
  public Object[] createErrorPanel(String message, Exception e) {
    Object[] returnValue = new Object[2];
    returnValue[0] = message;
    returnValue[1] = new net.suberic.util.swing.ExceptionDisplayPanel(Pooka.getProperty("error.showStackTrace", "Stack Trace"), e);

    return returnValue;
  }
}
