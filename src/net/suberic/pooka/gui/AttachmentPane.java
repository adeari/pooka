package net.suberic.pooka.gui;

import javax.swing.*;
import javax.mail.internet.*;
import javax.mail.*;
import java.util.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import javax.activation.*;
import javax.swing.table.AbstractTableModel;

import net.suberic.util.thread.*;
import net.suberic.util.swing.*;
import net.suberic.pooka.*;
import java.awt.*;

//import com.ice.jni.dde.JNIDDE;
//import com.ice.jni.registry.*;

/**
 * This class basically creates a visual list of the parts of a 
 * MimeMessage.
 */
public class AttachmentPane extends JPanel {

  /**
   * The AttachmentTableModel displays the MessageProxy's attachments
   * list as a JTable.
   */
  class AttachmentTableModel extends AbstractTableModel {
    MessageProxy msg;
    Vector columnNames;
    
    public AttachmentTableModel(MessageProxy newMsg) {
      msg=newMsg;
      columnNames = new Vector();
      columnNames.add(Pooka.getProperty("AttachmentPane.header.name", "Filename"));
      columnNames.add(Pooka.getProperty("AttachmentPane.header.type", "Type"));
    }
    
    public int getRowCount() {
      try {
	return msg.getAttachments().size();
      } catch (MessagingException me) {
	return 0;
      }
    }
    
    /**
     * As of now, we just have two columns:  file name and file type.
     * Maybe in the future we'll have an icon, too.
     */
    public int getColumnCount() {
      return 2;
    }
    
    /**
     * This gets the displayed value for each column in the table.
     */
    public Object getValueAt(int row, int column) {
      Vector v = null;
      try {
	v = msg.getAttachments();
	
	if (v != null && row < v.size()) {
	  if (column == 0) {
	    String name = (((Attachment)v.elementAt(row)).getName()); 
	    if (name != null)
	      return name;
	    else
	      return Pooka.getProperty("AttachmentPane.error.FileNameUnavailable", "Unavailable");
	  } else if (column == 1) {
	    
	    String contentType = ((Attachment)v.elementAt(row)).getMimeType().toString();    
	    if (contentType.indexOf(';') != -1)
	      contentType = contentType.substring(0, contentType.indexOf(';'));			
	    return contentType;
	  }
	}
      } catch (MessagingException me) {
      }
      // if it's not a valid request, just return null.
      
      return null;
    }
    
    /**
     * A convenience method to return a particular Attachment.
     *
     * Returns null if there is no entry at that row.
     */
    public Attachment getAttachmentAtRow(int row) {
      try {
	if ((row < msg.getAttachments().size()) && (row >= 0))
	  return (Attachment)msg.getAttachments().elementAt(row);
      } catch (MessagingException me) {
      }
      
      return null;
    }
    
    public String getColumnName(int columnIndex) {
      if (columnIndex >= 0 && columnIndex < columnNames.size())
	return (String)columnNames.elementAt(columnIndex);
      else
	return null;
    }
  } // AttachmentTableModel
  
  class SaveAttachmentThread extends Thread {
    
    Attachment attachment;
    File saveFile;
    ProgressDialog dialog;
    boolean running = true;
    
    SaveAttachmentThread(Attachment newAttachment, File newSaveFile) {
      attachment = newAttachment;
      saveFile = newSaveFile;
    }

    public void run() {
      InputStream decodedIS = null;
      BufferedOutputStream outStream = null;
      
      int attachmentSize = 0;
      
      try {
	decodedIS = attachment.getInputStream();
	attachmentSize = attachment.getSize();
	if (attachment.getEncoding() != null && attachment.getEncoding().equalsIgnoreCase("base64"))
	  attachmentSize = (int) (attachmentSize * .73);
	
	dialog = createDialog(attachmentSize);
	dialog.show();
	
	outStream = new BufferedOutputStream(new FileOutputStream(saveFile));
	int b=0;
	byte[] buf = new byte[32768];
	
	b = decodedIS.read(buf);
	while (b != -1 && running) {
	  outStream.write(buf, 0, b);
	  dialog.setValue(dialog.getValue() + b);
	  if (dialog.isCancelled())
	    running=false;

	  b = decodedIS.read(buf);
	}
	
      } catch (IOException ioe) {
	showError("Error saving file", ioe);
	cancelSave();
      } finally {
	if (outStream != null) {
	  try {
	    outStream.flush();
	    outStream.close();
	  } catch (IOException ioe) {}
	}
	if (dialog != null)
	  dialog.dispose();
      }
    }
    
    /**
     * Creates a progress dialog to show the downloading of an attachment.
     */
    public ProgressDialog createDialog(int attachmentSize) {
      ProgressDialog dlg;
      if (getMessageUI() != null) {
	dlg = getMessageUI().createProgressDialog(0, attachmentSize, 0, saveFile.getName(), saveFile.getName());
      } else {
	dlg = Pooka.getUIFactory().createProgressDialog(0, attachmentSize, 0, saveFile.getName(), saveFile.getName());
      }

      dlg.addCancelListener(new ProgressDialogListener() {
	  public void dialogCancelled() {
	    cancelSave();
	  }
	});
      return dlg;
    }
    
    public void cancelSave() {
      try {
	saveFile.delete();
      } catch (Exception e) {}
      dialog.dispose();
    }
  } // SaveAttachmentThread

  // i'm hardcoding these, but i doubt that will be too much of a problem.

  static int minTextWidth = 600;
  static int maxTextWidth = 800;
  static int minTextHeight = 600;
  static int maxTextHeight = 800;

  JTable table;
  AttachmentTableModel tableModel;
  MessageProxy message;
  JPanel displayPanel;
  Action[] defaultActions;

  public AttachmentPane (MessageProxy msg) {
    super();
    
    message=msg;
    defaultActions = createDefaultActions();
    
    tableModel = new AttachmentTableModel(message);
    
    table = new JTable(tableModel);
    
    tableModel.addTableModelListener(table);
    
    table.addMouseListener(new MouseAdapter() {
	public void mousePressed(MouseEvent e) {
	  if (e.getClickCount() == 2) {
	    Attachment selectedAttachment = getSelectedAttachment();
	    String actionCommand = Pooka.getProperty("AttachmentPane.2xClickAction", "file-open");
	    if (selectedAttachment != null) {
	      Action clickAction = getActionByName(actionCommand);
	      if (clickAction != null) {
		clickAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
		
	      }
	    }
	  } else if (e.isPopupTrigger()) {
	    // see if anything is selected
	    int rowIndex = getTable().rowAtPoint(e.getPoint());
	    if (rowIndex != -1) {
	      if (! getTable().isRowSelected(rowIndex)) {
		getTable().setRowSelectionInterval(rowIndex, rowIndex);
	      }
	      createPopupMenu().show(getTable(), e.getX(), e.getY());
	    }
	    
	  } 
	  
	}
	
	public void mouseReleased(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    // see if anything is selected
	    int rowIndex = getTable().rowAtPoint(e.getPoint());
	    if (rowIndex != -1) {
	      if (! getTable().isRowSelected(rowIndex)) {
		getTable().setRowSelectionInterval(rowIndex, rowIndex);
	      }
	      createPopupMenu().show(getTable(), e.getX(), e.getY());
	    }
	    
	  } 
	  
	}
      });

    JScrollPane jsp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jsp.getViewport().add(table);
    table.addNotify();
    this.add(jsp);
    
    // the width will be resized; the only important part here is 
    // the height.
    
    Dimension prefSize = this.getPreferredSize();
    int defaultHeight = Integer.parseInt(Pooka.getProperty("Pooka.attachmentPanel.vsize", "100"));
    if (prefSize.getHeight() > defaultHeight) {
      this.setPreferredSize(new Dimension((int)prefSize.getWidth(), defaultHeight));
    }
    Dimension jspPrefSize = jsp.getPreferredSize();
    if (jspPrefSize.getHeight() > defaultHeight - 15) {
      jsp.setPreferredSize(new Dimension((int)prefSize.getWidth(), defaultHeight - 15));
    }
    
    this.addFocusListener(new FocusAdapter() {
	public void focusGained(FocusEvent e) {
	  if (getTable() != null) {
	    if (getSelectedAttachment() == null) {
	      getTable().setRowSelectionInterval(0,0);
	    }
	    getTable().requestFocus();
	  }
	}
      });

    createKeyBindings();
  }
  
  /**
   * Returns the display panel for the AttachmentPane.  This will normally
   * contain just the AttachmentTable.
   */
  public JPanel getDisplayPanel() {
    return displayPanel;
  }
  
  /**
   * getSelectedAttachment() will return the selected Attachment.
   */
  
  public Attachment getSelectedAttachment() {
    return getTableModel().getAttachmentAtRow(getTable().getSelectedRow());
  }
  
  /**
   * This opens up the selected Attachment using the default handler
   * for the Attachment's Mime type.
   */
  public void openSelectedAttachment() {
    Attachment attachment = getSelectedAttachment();
    if (attachment != null) {
      DataHandler dh = null;
      dh = attachment.getDataHandler();
      
      if (dh != null) {
	dh.setCommandMap(Pooka.getMailcap());
	if (Pooka.isDebug()) {
	  CommandInfo[] cis = dh.getAllCommands();
	  if (cis != null && cis.length > 0) {
	    for (int i = 0; i < cis.length; i++) {
	      System.out.println(cis[i].getCommandName() + ", " + cis[i].getCommandClass());
	    } 
	  } else {
	    System.out.println("No commands for mimetype.");
	  }
	}
	CommandInfo[] cmds = dh.getPreferredCommands();
	if (cmds != null && cmds[0] != null) {
	  Object beanViewer = dh.getBean(cmds[0]);
	  if (beanViewer instanceof Frame) {
	    Frame frameViewer = (Frame)beanViewer;
	    try {
	      frameViewer.setTitle(attachment.getName());
	      frameViewer.setSize(frameViewer.getPreferredSize());
	    } catch (Exception e) {
	    }
	    frameViewer.show();
	  } else if (beanViewer instanceof Component) {
	    String title = attachment.getName();
	    openAttachmentWindow((Component)beanViewer, title, false);
	  } else if (beanViewer instanceof ExternalLauncher) {
	    ((ExternalLauncher)beanViewer).show();
	  } else if (beanViewer instanceof com.sun.mail.handlers.text_plain) {
	    // sigh
	    JTextPane jtp = new JTextPane();
	    try {
	      String content = (String) attachment.getContent();
	      if (attachment.isHtml()) {
		jtp.setContentType("text/html");
	      }
	      jtp.setText(content);
	      jtp.setEditable(false);
	      openAttachmentWindow(new JScrollPane(jtp), attachment.getName(), true);
	    } catch (IOException ioe) {
	      showError("Error showing attachment:  ", ioe);
	    }
	  } else if (cmds[0].getCommandClass().equals("net.suberic.pooka.ExternalLauncher")) {
	    try {
	      ExternalLauncher el = new ExternalLauncher();
	      el.setCommandContext(cmds[0].getCommandName(), null);
	      el.show();
	    } catch (IOException ioe) {
	      //
	    }
	  }
	} else {
	    /*
	  if (isWindows()) {
	    try {
	      JNIDDE dde=new JNIDDE();

	      String extension = ".tmp";
	      String filename = dh.getName();
	      int dotLoc = filename.lastIndexOf('.');
	      if (dotLoc > 0) {
		  extension = filename.substring(dotLoc);
	      }
	      File tmpFile = File.createTempFile("pooka_", extension);
	      
	      FileOutputStream fos = new FileOutputStream(tmpFile);
	      dh.writeTo(fos);
	      fos.close();

	      tmpFile.deleteOnExit();

	      JNIDDE.shellExecute("open", tmpFile.getAbsolutePath(), null, tmpFile.getAbsoluteFile().getParent(), JNIDDE.SW_SHOWNORMAL);
	    } catch (Throwable e) {
		System.err.println("got exception " + e);
		e.printStackTrace();
		openWith();
	    }
	  } else {
	    */
	    openWith();
	}
      }
    }
  }

  /**
   * Opens either a JFrame or a JInternalFrame, whichever is appropriate,
   * with the given Component as a content pane and the given title.
   */
  private void openAttachmentWindow(Component pContent, String pTitle, boolean pResize) {
    final Component content = pContent;
    final String title = pTitle;
    final boolean resize = pResize;

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
    MessageUI mui = message.getMessageUI();
    if (mui != null && mui instanceof JInternalFrame) {
      JDesktopPane desktop = ((JInternalFrame) mui).getDesktopPane();
      JInternalFrame jif = new JInternalFrame(title, true, true, true, true);
      jif.getContentPane().add(content);
      jif.pack();
      if (resize) {
	// let's be reasonable here....
	Dimension frameSize = jif.getSize();
	if (frameSize.width < minTextWidth) {
	  frameSize.width = minTextWidth;
	} else if (frameSize.width > maxTextWidth) {
	  frameSize.width = maxTextWidth;
	}
	
	if (frameSize.height < minTextHeight) {
	  frameSize.height = minTextHeight;
	} else if (frameSize.height > maxTextHeight) {
	  frameSize.height = maxTextHeight;
	}
      
	jif.setSize(frameSize);
      }
      
      desktop.add(jif);
      if (desktop instanceof MessagePanel) {
	jif.setLocation(((MessagePanel) desktop).getNewWindowLocation(jif, false));
      }
      jif.setVisible(true);
      try {
	jif.setSelected(true);
      } catch (java.beans.PropertyVetoException e) {
      } 
    } else {
      JFrame frame = new JFrame(title);
      frame.getContentPane().add(content);
      frame.pack();

      if (resize) {
	// let's be reasonable here....
	Dimension frameSize = frame.getSize();
	if (frameSize.width < minTextWidth) {
	  frameSize.width = minTextWidth;
	} else if (frameSize.width > maxTextWidth) {
	  frameSize.width = maxTextWidth;
	}
	
	if (frameSize.height < minTextHeight) {
	  frameSize.height = minTextHeight;
	} else if (frameSize.height > maxTextHeight) {
	  frameSize.height = maxTextHeight;
	}
      
	frame.setSize(frameSize);
      }
      frame.show();
    }
      }
      });
  }
  
  /**
   * This opens the Attachment with the program of the user's choice.
   */
  public void openWith() {
    if (Pooka.isDebug())
      System.out.println("calling AttachmentPane.openWith()");
    
    Attachment attachment = getSelectedAttachment();
    String mType;
    try {
      mType = attachment.getMimeType().toString();
      if (mType.indexOf(';') != -1)
	mType = mType.substring(0, mType.indexOf(';'));
      
      String inputMessage = Pooka.getProperty("AttchmentPane.openWith.message", "Enter the command with which \nto open the attchment.");
      String inputTitle = Pooka.getProperty("AttachmentPane.openWith.title", "Open Attachment With");
      String makeDefaultLabel = Pooka.getProperty("AttachmentPane.openWith.makeDefaultMessage", "Make default command?");
      
      JLabel toggleMsgLabel = new JLabel(makeDefaultLabel);
      toggleMsgLabel.setForeground(Color.getColor("Black"));
      JRadioButton toggleButton = new JRadioButton();
      JPanel togglePanel = new JPanel();
      togglePanel.add(toggleMsgLabel);
      togglePanel.add(toggleButton);

      Object[] messageArray = new Object[2];
      messageArray[0] = inputMessage;
      messageArray[1] = togglePanel;
      String cmd = null;
      if (getMessageUI() != null)
	cmd = getMessageUI().showInputDialog(messageArray, inputTitle);
      else
	cmd = Pooka.getUIFactory().showInputDialog(messageArray, inputTitle);
      
      if (cmd != null) {
	if (cmd.indexOf("%s") == -1)
	  cmd = cmd.concat(" %s");
	
	if (toggleButton.isSelected()) {
	  String newMailcap = new String(mType.toLowerCase() + ";" + cmd);
	  ((FullMailcapCommandMap)Pooka.getMailcap()).addMailcap(newMailcap);
	}
	
	DataHandler dh = null;
	
	dh = attachment.getDataHandler();
	if (dh != null) {
	  dh.setCommandMap(Pooka.getMailcap());
	  ExternalLauncher el = new ExternalLauncher();
	  el.setCommandContext(cmd, dh);
	  el.show();
	}
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * This opens up a JFileChooser to let the user choose under what
   * name and where the selected Attachment should be saved.  It then
   * calls saveFileAs() to save the file.
   */
  public void saveAttachment() {
    Attachment attachment = getSelectedAttachment();
    if (attachment != null) {
      JFileChooser saveChooser;
      String currentDirectoryPath = Pooka.getProperty("Pooka.tmp.currentDirectory", "");
      if (currentDirectoryPath == "")
	saveChooser = new JFileChooser();
      else
	saveChooser = new JFileChooser(currentDirectoryPath);
      
      String fileName = attachment.getName();
      if (fileName != null)
	saveChooser.setSelectedFile(new File(fileName));
      
      int saveConfirm = saveChooser.showSaveDialog(this);
      Pooka.getResources().setProperty("Pooka.tmp.currentDirectory", saveChooser.getCurrentDirectory().getPath(), true);
      if (saveConfirm == JFileChooser.APPROVE_OPTION) {
	try {
	  saveFileAs(attachment, saveChooser.getSelectedFile());
	} catch (IOException exc) {
	  showError(Pooka.getProperty("error.SaveFile", "Error saving file") + ":\n", Pooka.getProperty("error.SaveFile", "Error saving file"), exc);
	}
      }
    }
  }
  
  /**
   * This actually saves the Attachment as the File saveFile.
   */
  public void saveFileAs(Attachment mbp, File saveFile) throws IOException {
    SaveAttachmentThread thread = new SaveAttachmentThread(mbp, saveFile);
    thread.start();
    
  }

  /**
   * Returns whether or not we're running on a Windows platform.
   */
  public boolean isWindows() {
    return (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1);
  }

  /**
   * This removes the Attachment from the message.
   */
  public void removeAttachment() {
    int selectedIndex = getTable().getSelectedRow();
    Attachment attachmentToRemove = getSelectedAttachment();
    ((NewMessageProxy)message).detachFile(attachmentToRemove);
  }
  
  public AttachmentTableModel getTableModel() {
    return tableModel;
  }
  
  /**
   * Gets the table with all of the attachment entries.
   */
  public JTable getTable() {
    return table;
  }
  
  /**
   * Creates the popup menu for this component.
   */
  protected JPopupMenu createPopupMenu() {
    net.suberic.util.gui.ConfigurablePopupMenu popupMenu = new net.suberic.util.gui.ConfigurablePopupMenu();
    String key;
    if (message instanceof NewMessageProxy)
	key = "AttachmentPane.NewMsgActions";
    else 
      key = "AttachmentPane.Actions";
    popupMenu.configureComponent(key, Pooka.getResources());	
    popupMenu.setActive(getActions());
    MessageUI mui = ((MessageProxy)message).getMessageUI();
    if (mui instanceof net.suberic.util.swing.ThemeSupporter) {
      try {
	Pooka.getUIFactory().getPookaThemeManager().updateUI((net.suberic.util.swing.ThemeSupporter) mui, popupMenu, true);
      } catch (Exception etwo) {
	System.err.println("error setting theme:  " + etwo);
      }
    }
    return popupMenu;
  }
  
  /**
   * Creates the ConfigurableKeyBindings for this component.
   */
  protected void createKeyBindings() {
    String key;
    if (message instanceof NewMessageProxy)
      key = "AttachmentPane.newMsgKeyBindings";
    else 
      key = "AttachmentPane.keyBindings";
    
    net.suberic.util.gui.ConfigurableKeyBinding keyBindings = new net.suberic.util.gui.ConfigurableKeyBinding(getTable(), key, Pooka.getResources());
    keyBindings.setActive(getActions());

  }
  

  /**
   * Returns the given Action.
   */
  public Action getActionByName(String actionName) {
    Action[] actionList = getActions();
    for (int i = 0; i < actionList.length; i++) {
      if (actionName.equals((String)actionList[i].getValue(Action.NAME))) {
	return actionList[i];
      }
    }
    return null;
    
  }
  
  /**
   * Creates the default actions for this pane.
   */
  public Action[] createDefaultActions() {
    if (message instanceof NewMessageProxy)
      return new Action[] {
	new RemoveAction()
	  };
	else {
	  ActionThread storeThread = message.getFolderInfo().getParentStore().getStoreThread();
	  return new Action[] {
	    new ActionWrapper(new OpenAction(), storeThread),
	    new ActionWrapper(new OpenWithAction(), storeThread),
	    new ActionWrapper(new SaveAsAction(), storeThread)
	      };
	}
  }
  
  public Action[] getActions() {
    return getDefaultActions();
  }
    
  public Action[] getDefaultActions() {
    return defaultActions;
  }
  
  public MessageUI getMessageUI() {
    return message.getMessageUI();
  }

  /**
   * Shows an error message, either on the MessageUI if there is one, or
   * if not, on the main Pooka frame.
   */
  public void showError(String message, Exception ioe) {
    MessageUI mui = getMessageUI();
    if (mui != null) {
      mui.showError(message,ioe);
    } else {
      Pooka.getUIFactory().showError(message,ioe);
    }
  }

  /**
   * Shows an error message, either on the MessageUI if there is one, or
   * if not, on the main Pooka frame.
   */
  public void showError(String message, String title, Exception ioe) {
    MessageUI mui = getMessageUI();
    if (mui != null) {
      mui.showError(message, title, ioe);
    } else {
      Pooka.getUIFactory().showError(message, title, ioe);
    }
  }

  //------------------------------------//
  
  class OpenAction extends AbstractAction {
    OpenAction() {
      super("file-open");
    }
    
    public void actionPerformed(ActionEvent e) {
      openSelectedAttachment();
    }
  }
  
  class OpenWithAction extends AbstractAction {
    OpenWithAction() {
      super("file-open-with");
    }
    
    public void actionPerformed(ActionEvent e) {
      openWith();
    }
  }
  
  class SaveAsAction extends AbstractAction {
    SaveAsAction() {
      super("file-save-as");
    }
    
    public void actionPerformed(ActionEvent e) {
      saveAttachment();
    }
  }
  
  class RemoveAction extends AbstractAction {
    RemoveAction() {
      super("file-remove");
    }
    
    public void actionPerformed(ActionEvent e) {
      removeAttachment();
    }
  }
  
}
    

