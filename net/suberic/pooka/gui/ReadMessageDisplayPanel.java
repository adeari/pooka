package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.gui.*;
import net.suberic.util.swing.HyperlinkMouseHandler;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.TextAction;
import java.util.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.*;
import java.io.File;

public class ReadMessageDisplayPanel extends MessageDisplayPanel {
  
  private JTextPane otherEditorPane = null;
  private JScrollPane otherScrollPane = null;
  
  private Box attachmentSlot = null;
  private Box cryptoSlot = null;

  public boolean firstShow = true;
  
  private static String WITH_ATTACHMENTS = "with";
  private static String WITHOUT_ATTACHMENTS = "without";
  
  private String editorStatus = WITHOUT_ATTACHMENTS;

  private DisplayStyleComboBox displayCombo = null;
  private DisplayStyleComboBox headerCombo = null;
  private net.suberic.pooka.gui.crypto.CryptoStatusDisplay cryptoStatusDisplay = null;

  Action[] defaultActions = new Action[] {
    new AttachmentPanelAction(),
    new FindAction(),
    new FindNextAction()
  };

  /**
   * Creates an empty MessageDisplayPanel.
   */
  public ReadMessageDisplayPanel() {
    super();
    
    this.setLayout(new CardLayout());
    
    this.addFocusListener(new FocusAdapter() {
	public void focusGained(FocusEvent e) {
	  if (editorStatus == WITHOUT_ATTACHMENTS) {
	    if (editorPane != null)
	      editorPane.requestFocus();
	  } else if (editorStatus == WITH_ATTACHMENTS) {
	    if (otherEditorPane != null)
	      otherEditorPane.requestFocus();
	  }
	}
      });
  }
  
  /**
   * Creates a MessageDisplayPanel from the given Message.
   */    
  public ReadMessageDisplayPanel(MessageUI newMsgUI) {
    super(newMsgUI);
    
    this.setLayout(new CardLayout());
    
    this.addFocusListener(new FocusAdapter() {
	public void focusGained(FocusEvent e) {
	  if (editorStatus == WITHOUT_ATTACHMENTS) {
	    if (editorPane != null)
	      editorPane.requestFocus();
	  } else if (editorStatus == WITH_ATTACHMENTS) {
	    if (otherEditorPane != null)
	      otherEditorPane.requestFocus();
	  }
	}
      });
  }
  
  /**
   * Configures the MessageDisplayPanel.  This includes creating all 
   * the necessary panels and populating those panels with the information
   * from the MessageProxy.
   */
  public void configureMessageDisplay() throws MessagingException {
    editorPane = new JTextPane();
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(new HyperlinkDispatcher());
    HyperlinkMouseHandler hmh = new HyperlinkMouseHandler(Integer.parseInt(Pooka.getProperty("Pooka.lineLength", "80")));
    editorPane.addMouseListener(hmh);
    editorPane.addMouseMotionListener(hmh);
    
    editorScrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    setDefaultFont(editorPane);
    
    // temp
    
    otherEditorPane = new JTextPane();
    otherEditorPane.setEditable(false);
    otherEditorPane.addHyperlinkListener(new HyperlinkDispatcher());
    otherEditorPane.addMouseListener(hmh);
    otherEditorPane.addMouseMotionListener(hmh);
    otherScrollPane = new JScrollPane(otherEditorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    setDefaultFont(otherEditorPane);
    
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    
    attachmentDisplayPanel = new JPanel();
    attachmentDisplayPanel.setLayout(new BoxLayout(attachmentDisplayPanel, BoxLayout.X_AXIS));
    attachmentSlot = new Box(BoxLayout.Y_AXIS);
    cryptoSlot = new Box(BoxLayout.Y_AXIS);
    
    attachmentDisplayPanel.add(Box.createHorizontalStrut(5));
    attachmentDisplayPanel.add(attachmentSlot);
    attachmentDisplayPanel.add(Box.createHorizontalStrut(5));
    attachmentDisplayPanel.add(cryptoSlot);
    attachmentDisplayPanel.add(Box.createHorizontalStrut(5));

    splitPane.setTopComponent(otherScrollPane);
    splitPane.setBottomComponent(attachmentDisplayPanel);
    
    this.add(WITH_ATTACHMENTS, splitPane);
    this.add(WITHOUT_ATTACHMENTS, editorScrollPane);
    
    keyBindings = new ConfigurableKeyBinding(this, "ReadMessageWindow.keyBindings", Pooka.getResources());
    keyBindings.setActive(getActions());
    
    editorPane.addMouseListener(new MouseAdapter() {
	
	public void mousePressed(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    showPopupMenu(editorPane, e);
	  }
	}

	public void mouseReleased(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    showPopupMenu(editorPane, e);
	  }
	}
      });
    
    if (getMessageProxy() != null) {
      resetEditorText();
    } else {
      ((CardLayout)getLayout()).show(this, WITHOUT_ATTACHMENTS);
      editorStatus = WITHOUT_ATTACHMENTS;
    }
    
  }


  /**
   * This sets the text of the editorPane to the content of the current
   * message.
   * 
   * Should only be called from within the FolderThread for the message.
   *
   * Also updates the current keybindings.
   */
  public void resetEditorText() throws MessagingException {
    // ok.  here's how this has to go:  we need to load the information from
    // the message on the message editor thread, but then actually do the
    // display changing on the awt event thread.  seem simple enough?

    // assume that we're actually on the FolderThread for now.

    if (getMessageProxy() != null) {
      StringBuffer messageText = new StringBuffer();
      
      String content = null;

      String contentType = "text/plain";

      boolean displayHtml = false;

      int msgDisplayMode = getMessageProxy().getDisplayMode();

      // figure out html vs. text
      if (Pooka.getProperty("Pooka.displayHtml", "").equalsIgnoreCase("true")) {
	if (getMessageProxy().getMessageInfo().isHtml()) {
	  if (msgDisplayMode > MessageProxy.TEXT_ONLY) 
	    displayHtml = true;
	  
	} else if (getMessageProxy().getMessageInfo().containsHtml()) {
	  if (msgDisplayMode >= MessageProxy.HTML_PREFERRED)
	    displayHtml = true;
	  
	} else {
	  // if we don't have any html, just display as text.
	}
      }

      // set the content
      if (msgDisplayMode == MessageProxy.RFC_822) {
	content = getMessageProxy().getMessageInfo().getRawText();
      } else {
	if (displayHtml) {
	  contentType = "text/html";

	  if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	    content = getMessageProxy().getMessageInfo().getHtmlAndTextInlines(true, showFullHeaders());
	  } else {
	    content = getMessageProxy().getMessageInfo().getHtmlPart(true, showFullHeaders());
	  }
	} else {
	  if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	    // Is there only an HTML part?  Regardless, we've determined that 
	    // we will still display it as text.
	    if (getMessageProxy().getMessageInfo().isHtml())
	      content = getMessageProxy().getMessageInfo().getHtmlAndTextInlines(true, showFullHeaders());
	    else
	      content = getMessageProxy().getMessageInfo().getTextAndTextInlines(true, showFullHeaders());
	  } else {
	    // Is there only an HTML part?  Regardless, we've determined that 
	    // we will still display it as text.
	    if (getMessageProxy().getMessageInfo().isHtml())
	      content = getMessageProxy().getMessageInfo().getHtmlPart(true, showFullHeaders());
	    else
	      content = getMessageProxy().getMessageInfo().getTextPart(true, showFullHeaders());
	  }
	}
      }
      
      if (content != null)
	messageText.append(content);

      final String finalMessageText = messageText.toString();
      final String finalContentType = contentType;
      final boolean hasAttachments = getMessageProxy().hasAttachments();
      final boolean hasEncryption = (getMessageProxy().getMessageInfo() == null) ? false : getMessageProxy().getMessageInfo().hasEncryption();
      final boolean contentIsNull = (content == null);

      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    if (getDisplayCombo() != null)
	      getDisplayCombo().styleUpdated(getMessageProxy().getDisplayMode(), getMessageProxy().getHeaderMode());
	    
	    if (getHeaderCombo() != null && getHeaderCombo() != getDisplayCombo()) {
	      getHeaderCombo().styleUpdated(getMessageProxy().getDisplayMode(), getMessageProxy().getHeaderMode());
	    }
	    
	    if (! contentIsNull) {
	      if (hasAttachments) {
		try {
		  otherEditorPane.setContentType(finalContentType);
		  otherEditorPane.setEditable(false);
		  otherEditorPane.setText(finalMessageText);
		  otherEditorPane.setCaretPosition(0);
		} catch (Exception e) {
		  // if we can't show the html, just set the type as text/plain.
		  otherEditorPane.setEditorKit(new javax.swing.text.StyledEditorKit());
		  
		  otherEditorPane.setEditable(false);
		  otherEditorPane.setText(finalMessageText);
		  otherEditorPane.setCaretPosition(0);
		}
	      } else {
		try {
		  editorPane.setContentType(finalContentType);
		  editorPane.setEditable(false);
		  editorPane.setText(finalMessageText);
		  editorPane.setCaretPosition(0);
		} catch (Exception e) {
		  // if we can't show the html, just set the type as 
		  // text/plain.
		  
		  editorPane.setEditorKit(new javax.swing.text.StyledEditorKit());
		  
		  editorPane.setEditable(false);
		  editorPane.setText(finalMessageText);
		  editorPane.setCaretPosition(0);
		  
		}
	      }
	    }
	    
	    if (hasAttachments) {
	      attachmentPanel = new AttachmentPane(getMessageProxy());
	      fillAttachmentSlot(attachmentPanel);

	      ((CardLayout) getLayout()).show(ReadMessageDisplayPanel.this, WITH_ATTACHMENTS);
	      editorStatus = WITH_ATTACHMENTS;
	      
	      if (splitPane != null && attachmentPanel != null) {
		double paneHeight = splitPane.getSize().getHeight();
		if (paneHeight <= 0)
		  paneHeight = splitPane.getPreferredSize().getHeight();
		splitPane.setDividerLocation((int)(paneHeight - attachmentPanel.getPreferredSize().getHeight()));
	      } else {
		splitPane.setDividerLocation(400);
	      }
	      
	    } else {
	      ((CardLayout) getLayout()).show(ReadMessageDisplayPanel.this, WITHOUT_ATTACHMENTS);
	      editorStatus = WITHOUT_ATTACHMENTS;
	    }
	    
	    if (hasEncryption) {
	      net.suberic.pooka.gui.crypto.CryptoStatusDisplay csd = new net.suberic.pooka.gui.crypto.CryptoPanel();
	      setCryptoStatusDisplay(csd);
	      MessageCryptoInfo cryptoInfo = getMessageProxy().getMessageInfo().getCryptoInfo();
	      if (cryptoInfo != null)
		csd.cryptoUpdated(cryptoInfo);

	      fillCryptoSlot((JComponent) csd);
	    } else {
	      fillCryptoSlot(null);
	    }
	  }
	});

    } else {
      
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  // if getMessageProxy() == null
	  editorPane.setEditable(false);
	  editorPane.setText("");
	  editorPane.setCaretPosition(0);
	  
	  otherEditorPane.setEditable(false);
	  otherEditorPane.setText("");
	  otherEditorPane.setCaretPosition(0);
	  
	  ((CardLayout) getLayout()).show(ReadMessageDisplayPanel.this, WITHOUT_ATTACHMENTS);
	  editorStatus = WITHOUT_ATTACHMENTS;
	} 
	});
    }
    
    keyBindings.setActive(getActions());

    SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  ReadMessageDisplayPanel.this.repaint();
	}
      });
  }
  
  /**
   * Shows whether or now we want to show the full headers.
   */
  public boolean showFullHeaders() {
    if (getMessageProxy() != null)
      return (getMessageProxy().getHeaderMode() == MessageProxy.HEADERS_FULL);
    else
      return false;
  }
  
  /**
   * Shows the given Component in the Attachment slot.
   */
  public void fillAttachmentSlot(JComponent component) {
    if (attachmentSlot != null) {
      Component[] children = attachmentSlot.getComponents();
      for (int i = 0; children != null && i < children.length; i++) {
	attachmentSlot.remove(children[i]);
      }

      if (component != null)
	attachmentSlot.add(component);
    }

  }

  /**
   * Shows the given Component in the Crypto slot.
   */
  public void fillCryptoSlot(JComponent component) {
    if (cryptoSlot != null) {
      Component[] children = cryptoSlot.getComponents();
      for (int i = 0; children != null && i < children.length; i++) {
	cryptoSlot.remove(children[i]);
      }

      if (component != null)
	cryptoSlot.add(component);
    }

  }

  /**
   * This registers the Keyboard action not only for the FolderWindow
   * itself, but also for pretty much all of its children, also.  This
   * is to work around something which I think is a bug in jdk 1.2.
   * (this is not really necessary in jdk 1.3.)
   *
   * Overrides JComponent.registerKeyboardAction(ActionListener anAction,
   *            String aCommand, KeyStroke aKeyStroke, int aCondition)
   */
  
  public void registerKeyboardAction(ActionListener anAction,
	  String aCommand, KeyStroke aKeyStroke, int aCondition) {
    super.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
    
    if (attachmentPanel != null)
      attachmentPanel.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
    editorPane.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
    editorScrollPane.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
    if (splitPane != null)
      splitPane.registerKeyboardAction(anAction, aCommand, aKeyStroke, aCondition);
  }
  
  /**
   * This unregisters the Keyboard action not only for the FolderWindow
   * itself, but also for pretty much all of its children, also.  This
   * is to work around something which I think is a bug in jdk 1.2.
   * (this is not really necessary in jdk 1.3.)
   *
   * Overrides JComponent.unregisterKeyboardAction(KeyStroke aKeyStroke)
   */
  
  public void unregisterKeyboardAction(KeyStroke aKeyStroke) {
    super.unregisterKeyboardAction(aKeyStroke);
    
    if (attachmentPanel != null)
      attachmentPanel.unregisterKeyboardAction(aKeyStroke);
    editorPane.unregisterKeyboardAction(aKeyStroke);
    editorScrollPane.unregisterKeyboardAction(aKeyStroke);
    splitPane.unregisterKeyboardAction(aKeyStroke);
  }
  
  /**
   * This creates and shows a PopupMenu for this component.  
   */
  public void showPopupMenu(JComponent component, MouseEvent e) {
    ConfigurablePopupMenu popupMenu = new ConfigurablePopupMenu();
    popupMenu.configureComponent("ReadMessageWindow.popupMenu", Pooka.getResources());	
    popupMenu.setActive(getActions());
    MessageUI mui = getMessageUI();
    if (mui instanceof net.suberic.util.swing.ThemeSupporter) {
      try {
	Pooka.getUIFactory().getPookaThemeManager().updateUI((net.suberic.util.swing.ThemeSupporter) mui, popupMenu, true);
      } catch (Exception etwo) {
	System.err.println("error setting theme:  " + etwo);
      }
    }
    popupMenu.show(component, e.getX(), e.getY());
    
  }
  
  /**
   * This sets the size of the MessageDisplayPanel to a reasonable
   * default value.
   */
  public void sizeToDefault() {
    Dimension prefSize = getDefaultEditorPaneSize();
    if (editorPane != null && editorScrollPane != null) {
      JScrollBar vsb = editorScrollPane.getVerticalScrollBar();
      if (vsb != null)
	prefSize.setSize(prefSize.getWidth() + vsb.getPreferredSize().getWidth(), prefSize.getHeight());
      editorScrollPane.setPreferredSize(prefSize);
      if (otherScrollPane != null) {
	otherScrollPane.setPreferredSize(prefSize);
      }
      this.setPreferredSize(prefSize);
      if (splitPane != null && attachmentPanel != null) {
	splitPane.setPreferredSize(prefSize);
	double paneHeight = splitPane.getSize().getHeight();
	if (paneHeight <= 0)
	  paneHeight = splitPane.getPreferredSize().getHeight();
	splitPane.setDividerLocation((int)(paneHeight - attachmentPanel.getPreferredSize().getHeight()));
      }
    } else {
      this.setSize(prefSize);
    }
  }
  
  public void addNotify() {
    super.addNotify();
    
    if (firstShow) {
      sizeToDefault();
      firstShow = false;
    }
    
  }

  public DisplayStyleComboBox getDisplayCombo() {
    return displayCombo;
  };
  public void setDisplayCombo(DisplayStyleComboBox dscb) {
    displayCombo = dscb;
  }
  public DisplayStyleComboBox getHeaderCombo() {
    return headerCombo;
  };
  public void setHeaderCombo(DisplayStyleComboBox dscb) {
    headerCombo = dscb;
  }

  /**
   * Shows the current display of the encryption status.
   */
  public net.suberic.pooka.gui.crypto.CryptoStatusDisplay getCryptoStatusDisplay() {
    return cryptoStatusDisplay;
  }

  /**
   * Sets the current display of the encryption status.
   */
  public void setCryptoStatusDisplay( net.suberic.pooka.gui.crypto.CryptoStatusDisplay newDisplay) {
    cryptoStatusDisplay = newDisplay;
  }

  /**
   * Returns the current EditorPane being used.
   */
  public JTextPane getCurrentEditorPane() {
    if (editorStatus == WITHOUT_ATTACHMENTS) {
      return editorPane;
    } else {
      return otherEditorPane;
    }
  }
  

  
  //------- Actions ----------//
  
  /**
   * Returns this panel's actions.
   */
  public Action[] getActions() {
    
    Action[] actionList = defaultActions;
    
    if (getMessageProxy() != null)
      actionList = TextAction.augmentList(actionList, getMessageProxy().getActions());
    
    Action[] subActions = null;
    if (editorStatus == WITHOUT_ATTACHMENTS) {
      if (editorPane != null) {
	subActions = editorPane.getActions();
      } 
    } else {
      // if we have an attachment pane, we need to check to see if the 
      // attachment pane is selected or not.

      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (focusOwner != null && attachmentPanel != null && SwingUtilities.isDescendingFrom(focusOwner, attachmentPanel)) {
	subActions = attachmentPanel.getActions();
      } else {
	if (otherEditorPane != null) {
	  subActions = otherEditorPane.getActions();
	}
      } 

    }
    
    if (subActions != null)
      return TextAction.augmentList(actionList, subActions);
    else
      return actionList;
  }
  
  /**
   * Selects the Attachment panel.
   */
  public class AttachmentPanelAction extends AbstractAction {
    AttachmentPanelAction() {
      super("message-select-attachment");
    }
    
    public void actionPerformed(ActionEvent e) {
      if (attachmentPanel != null) {
	attachmentPanel.requestFocus();
      }
    }
  }

  /**
   * Finds the given String in the Message body.
   */
  public class FindAction extends AbstractAction {
    FindAction() {
      super("message-find");
    }
    
    public void actionPerformed(ActionEvent e) {
      searchMessage();
    }
  }

  /**
   * Finds the given String in the Message body.
   */
  public class FindNextAction extends AbstractAction {
    FindNextAction() {
      super("message-find-next");
    }
    
    public void actionPerformed(ActionEvent e) {
      searchAgain();
    }
  }

  /**
   * Selects the Editor panel.
   */
  public class EditorPanelAction extends AbstractAction {
    EditorPanelAction() {
      super("message-select-editor");
    }
    
    public void actionPerformed(ActionEvent e) {
      if (editorStatus == WITHOUT_ATTACHMENTS) {
	if (editorPane != null)
	  editorPane.requestFocus();
      } else if (editorStatus == WITH_ATTACHMENTS) {
	if (otherEditorPane != null)
	  otherEditorPane.requestFocus();
      }
    }
  }

}
