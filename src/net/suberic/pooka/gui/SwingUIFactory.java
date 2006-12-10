package net.suberic.pooka.gui;
import net.suberic.util.swing.ProgressDialog;
import net.suberic.util.gui.IconManager;
import net.suberic.util.gui.propedit.PropertyEditorFactory;
import net.suberic.util.swing.*;
import net.suberic.pooka.*;
import net.suberic.pooka.gui.search.*;
import javax.swing.*;

/**
 * An abstract base class for PookaUIFactories.
 */
public abstract class SwingUIFactory implements PookaUIFactory {

  protected ThemeManager pookaThemeManager = null;
  protected MessageNotificationManager mMessageNotificationManager;
  protected IconManager mIconManager;
  protected PropertyEditorFactory editorFactory = null;

  public boolean showing = false;

  protected int maxErrorLine = 50;

  /**
   * Returns the PookaThemeManager for fonts and colors.
   */
  public ThemeManager getPookaThemeManager() {
    return pookaThemeManager;
  }

  /**
   * Creates an appropriate MessageUI object for the given MessageProxy.
   */
  public MessageUI createMessageUI(MessageProxy mp) throws javax.mail.MessagingException {
    return createMessageUI(mp, null);
  }

  /**
   * Creates an appropriate MessageUI object for the given MessageProxy,
   * using the provided MessageUI as a guideline.
   */
  public abstract MessageUI createMessageUI(MessageProxy mp, MessageUI mui) throws javax.mail.MessagingException;

  /**
   * Opens the given MessageProxy in the default manner for this UI.
   * Usually this will just be callen createMessageUI() and openMessageUI()
   * on it.  However, in some cases (Preview Panel without auto display)
   * it may be necessary to act differently.
   */
  public abstract void doDefaultOpen(MessageProxy mp);

  /**
   * Creates an appropriate FolderDisplayUI object for the given
   * FolderInfo.
   */
  public abstract FolderDisplayUI createFolderDisplayUI(net.suberic.pooka.FolderInfo fi);

  /**
   * Creates a ContentPanel which will be used to show messages and folders.
   */
  public abstract ContentPanel createContentPanel();

  /**
   * Shows an Editor Window with the given title, which allows the user
   * to edit the given property.
   */
  public void showEditorWindow(String title, String property) {
    showEditorWindow(title, property, property);
  }

  /**
   * Shows an Editor Window with the given title, which allows the user
   * to edit the given property, which is in turn defined by the
   * given template.
   */
  public abstract void showEditorWindow(String title, String property, String template);

  /**
   * Returns the PropertyEditorFactory used by this component.
   */
  public net.suberic.util.gui.propedit.PropertyEditorFactory getEditorFactory() {
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
          fResponseWrapper.setInt(JOptionPane.showConfirmDialog(Pooka.getMainPanel(), fDisplayMessage, fTitle, fType));
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
          fResponseWrapper.setInt(JOptionPane.showConfirmDialog(Pooka.getMainPanel(), fMessageComponents, fTitle, fType));
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
   * This shows an Input window.
   */
  public  String showInputDialog(String inputMessage, String title) {
    final String displayMessage = formatMessage(inputMessage);
    final String fTitle = title;
    final ResponseWrapper fResponseWrapper = new ResponseWrapper();

    Runnable runMe = new Runnable() {
        public void run() {
          fResponseWrapper.setString(JOptionPane.showInputDialog(Pooka.getMainPanel(), displayMessage, fTitle, JOptionPane.QUESTION_MESSAGE));
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
   * Shows an Input window.
   */
  public  String showInputDialog(Object[] inputPanels, String title) {
    final String fTitle = title;
    final Object[] fInputPanes = inputPanels;
    final ResponseWrapper fResponseWrapper = new ResponseWrapper();

    Runnable runMe = new Runnable() {
        public void run() {
          fResponseWrapper.setString(JOptionPane.showInputDialog(Pooka.getMainPanel(), fInputPanes, fTitle, JOptionPane.QUESTION_MESSAGE));
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
   * Shows a status message.
   */
  public void showStatusMessage(String newMessage) {
    final String msg = newMessage;
    Runnable runMe = new Runnable() {
        public void run() {
          if (Pooka.getMainPanel() != null && Pooka.getMainPanel().getInfoPanel() != null)
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
          if (Pooka.getMainPanel() != null && Pooka.getMainPanel().getInfoPanel() != null)
            Pooka.getMainPanel().getInfoPanel().clear();
        }
      };
    if (SwingUtilities.isEventDispatchThread())
      runMe.run();
    else
      SwingUtilities.invokeLater(runMe);
  }

  /**
   * Shows a message.
   */
  public void showMessage(String newMessage, String title) {
    final String displayMessage = formatMessage(newMessage);
    //final String displayMessage = newMessage;
    final String fTitle = title;

    Runnable runMe = new Runnable() {
        public void run() {
          JLabel displayPanel = new JLabel(displayMessage);
          JOptionPane.showMessageDialog(Pooka.getMainPanel(), displayMessage, fTitle, JOptionPane.PLAIN_MESSAGE);
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
   * Creates a ProgressDialog using the given values.
   */
  public abstract ProgressDialog createProgressDialog(int min, int max, int initialValue, String title, String content);

  /**
   * Shows a SearchForm with the given FolderInfos selected from the list
   * of the given allowedValues.
   */
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
   * This shows an Error Message window.
   */
  public void showError(String errorMessage, String title) {
    final String displayErrorMessage = formatMessage(errorMessage);
    final String fTitle = title;

    if (showing) {
      SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(Pooka.getMainPanel(), displayErrorMessage, fTitle, JOptionPane.ERROR_MESSAGE);
          }
        });
    } else
      System.out.println(errorMessage);

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
            JOptionPane.showMessageDialog(Pooka.getMainPanel(), createErrorPanel(displayErrorMessage, fE), fTitle, JOptionPane.ERROR_MESSAGE);
          }
        });
    } else
      System.out.println(errorMessage);

    //e.printStackTrace();
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
   * Gets the current MessageNotificationManager.
   */
  public MessageNotificationManager getMessageNotificationManager() {
    return mMessageNotificationManager;
  }


  /**
   * Gets the IconManager for this UI.
   */
  public net.suberic.util.gui.IconManager getIconManager() {
    return mIconManager;
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
