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
  
  public boolean firstShow = true;
  
  public static int HEADERS_DEFAULT = 0;
  public static int HEADERS_FULL = 1;
  
  private static String WITH_ATTACHMENTS = "with";
  private static String WITHOUT_ATTACHMENTS = "without";
  
  private String editorStatus = WITHOUT_ATTACHMENTS;
  
  int headerStyle = ReadMessageDisplayPanel.HEADERS_DEFAULT;
  boolean showFullHeaders = false;
  
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
  public ReadMessageDisplayPanel(MessageProxy newMsgProxy) {
    super(newMsgProxy);
    
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
    
    attachmentScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    splitPane.setTopComponent(otherScrollPane);
    splitPane.setBottomComponent(attachmentScrollPane);
    
    this.add(WITH_ATTACHMENTS, splitPane);
    this.add(WITHOUT_ATTACHMENTS, editorScrollPane);
    
    keyBindings = new ConfigurableKeyBinding(this, "ReadMessageWindow.keyBindings", Pooka.getResources());
    keyBindings.setActive(getActions());
    
    editorPane.addMouseListener(new MouseAdapter() {
	
	public void mousePressed(MouseEvent e) {
	  if (SwingUtilities.isRightMouseButton(e)) {
	    showPopupMenu(editorPane, e);
	  }
	}
      });
    
    if (msg != null) {
      resetEditorText();
    } else {
      ((CardLayout)getLayout()).show(this, WITHOUT_ATTACHMENTS);
      editorStatus = WITHOUT_ATTACHMENTS;
    }
    
  }
  
  /**
   * Configures the style of this DisplayPanel.
   */
  public void configureInterfaceStyle(HashMap uiStyle) {

    Color c = null;

    c = (Color) uiStyle.get("background");
    if (c != null) {
      editorPane.setBackground(c);
      otherEditorPane.setBackground(c);
    }

    c = (Color) uiStyle.get("foreground");
    if (c != null) {
      editorPane.setForeground(c);
      otherEditorPane.setForeground(c);
    }

    /*
      c = uiStyle.get("disabledText");
      if (c != null) {
      editorPane.setDisabledText(c);
      }
      
      c = uiStyle.get("select");
      if (c != null) {
      editorPane.setSelectColor(c);
      }
      
      c = uiStyle.get("focus");
      if (c != null) {
      editorPane.setFocusColor(c);
      }
    */

    Font f = (Font) uiStyle.get("font");
    if (f != null) {
      editorPane.setFont(f);
      otherEditorPane.setFont(f);
    }

  }

  /**
   * This sets the text of the editorPane to the content of the current
   * message.
   */
  public void resetEditorText() throws MessagingException {
    if (getMessageProxy() != null) {
      StringBuffer messageText = new StringBuffer();
      
      String content = null;
      
      if ((Pooka.getProperty("Pooka.displayHtml", "").equalsIgnoreCase("true") && getMessageProxy().getMessageInfo().isHtml()) || (Pooka.getProperty("Pooka.displayHtmlAsDefault", "").equalsIgnoreCase("true") && getMessageProxy().getMessageInfo().containsHtml())) {
	
	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	  content = getMessageProxy().getMessageInfo().getHtmlAndTextInlines(true, showFullHeaders());
	} else {
	  content = getMessageProxy().getMessageInfo().getHtmlPart(true, showFullHeaders());
	}
	
	editorPane.setContentType("text/html");
	otherEditorPane.setContentType("text/html");
	
      } else {
	
	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	  // Is there only an HTML part?  Regardless, we will still display it as text.
	  if (getMessageProxy().getMessageInfo().isHtml())
	    content = getMessageProxy().getMessageInfo().getHtmlAndTextInlines(true, showFullHeaders());
	  else
	    content = getMessageProxy().getMessageInfo().getTextAndTextInlines(true, showFullHeaders());
	} else {
	  // Is there only an HTML part?  Regardless, we will still display it as text.
	  if (getMessageProxy().getMessageInfo().isHtml())
	    content = getMessageProxy().getMessageInfo().getHtmlPart(true, showFullHeaders());
	  else
	    content = getMessageProxy().getMessageInfo().getTextPart(true, showFullHeaders());
	}

	editorPane.setContentType("text/plain");
        otherEditorPane.setContentType("text/plain");
      }
      
      if (content != null) {
	try {
	  messageText.append(content);

	  editorPane.setEditable(false);
	  editorPane.setText(messageText.toString());
	  editorPane.setCaretPosition(0);
	    
	  otherEditorPane.setEditable(false);
	  otherEditorPane.setText(messageText.toString());
	  otherEditorPane.setCaretPosition(0);
	} catch (Exception e) {
	  // if we can't show the html, just set the type as text/plain.

	  // oops--can't do it that way.  :)
	  //editorPane.setContentType("text/plain");
	  //otherEditorPane.setContentType("text/plain");

	  editorPane.setEditorKit(new javax.swing.text.StyledEditorKit());
	  otherEditorPane.setEditorKit(new javax.swing.text.StyledEditorKit());

	  editorPane.setEditable(false);
	  editorPane.setText(messageText.toString());
	  editorPane.setCaretPosition(0);
	    
	  otherEditorPane.setEditable(false);
	  otherEditorPane.setText(messageText.toString());
	  otherEditorPane.setCaretPosition(0);
	}
      } 
      
      if (getMessageProxy().hasAttachments()) {
	attachmentPanel = new AttachmentPane(msg);
	attachmentScrollPane.setViewportView(attachmentPanel);
	((CardLayout)getLayout()).show(this, WITH_ATTACHMENTS);
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
	((CardLayout)getLayout()).show(this, WITHOUT_ATTACHMENTS);
	editorStatus = WITHOUT_ATTACHMENTS;
      }
    } else {
      // if getMessageProxy() == null
      editorPane.setEditable(false);
      editorPane.setText("");
      editorPane.setCaretPosition(0);
      
      otherEditorPane.setEditable(false);
      otherEditorPane.setText("");
      otherEditorPane.setCaretPosition(0);
      ((CardLayout)getLayout()).show(this, WITHOUT_ATTACHMENTS);
      editorStatus = WITHOUT_ATTACHMENTS;

    }
    
    this.repaint();
  }
  
  public boolean showFullHeaders() {
    return showFullHeaders;
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
  
  //------- Actions ----------//
  
  public Action[] getActions() {
    
    Action[] actionList = null;
	
    if (msg != null)
      actionList = msg.getActions();
    
    if (actionList != null) {
      if (editorPane != null && editorPane.getActions() != null) 
	return TextAction.augmentList(actionList, editorPane.getActions());
      else
	return editorPane.getActions();
    }
    return actionList;
    }
  
}
