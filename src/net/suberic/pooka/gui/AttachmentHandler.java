package net.suberic.pooka.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.activation.*;

import net.suberic.pooka.*;
import net.suberic.util.swing.*;

import com.ice.jni.dde.JNIDDE;
import com.ice.jni.registry.*;

/**
 * Handles opening, saving, etc. attachments.
 */
public class AttachmentHandler {

  // i'm hardcoding these, but i doubt that will be too much of a problem.

  static int minTextWidth = 600;
  static int maxTextWidth = 800;
  static int minTextHeight = 600;
  static int maxTextHeight = 800;

  MessageProxy mProxy;

  /**
   * Creates a new AttachmentHandler instance.
   */
  public AttachmentHandler(MessageProxy pProxy) {
    mProxy = pProxy;
  }
  
  /**
   * Returns the associated MessageProxy.
   */
  public MessageProxy getMessageProxy() {
    return mProxy;
  }

  /**
   * Returns the associated MessageUI.
   */
  public MessageUI getMessageUI() {
    return mProxy.getMessageUI();
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

  /**
   * This opens up the selected Attachment using the default handler
   * for the Attachment's Mime type.
   */
  public void openAttachment(Attachment pAttachment) {
    if (pAttachment != null) {
      DataHandler dh = null;
      dh = pAttachment.getDataHandler();
      
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
	      frameViewer.setTitle(pAttachment.getName());
	      frameViewer.setSize(frameViewer.getPreferredSize());
	    } catch (Exception e) {
	    }
	    frameViewer.show();
	  } else if (beanViewer instanceof Component) {
	    String title = pAttachment.getName();
	    openAttachmentWindow((Component)beanViewer, title, false);
	  } else if (beanViewer instanceof ExternalLauncher) {
	    ((ExternalLauncher)beanViewer).show();
	  } else if (beanViewer instanceof com.sun.mail.handlers.text_plain || beanViewer instanceof com.sun.mail.handlers.text_html) {
	    // sigh
	    JTextPane jtp = new JTextPane();
	    try {
	      String content = (String) pAttachment.getContent();
	      if (pAttachment.isHtml()) {
		jtp.setContentType("text/html");
	      }
	      jtp.setText(content);
	      jtp.setEditable(false);
	      openAttachmentWindow(new JScrollPane(jtp), pAttachment.getName(), true);
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
	  } else {
	    openWith(pAttachment);
	  }
	} else if (isWindows()) {
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
	    openWith(pAttachment);
	  }
	} else {
	  openWith(pAttachment);
	}
      }
    }
  }
  
  /**
   * Returns whether or not we're running on a Windows platform.
   */
  public boolean isWindows() {
    return (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1);
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
	MessageUI mui = getMessageUI();
	if ((mui != null && mui instanceof JInternalFrame) || (mui == null && Pooka.getUIFactory() instanceof PookaDesktopPaneUIFactory) ) {
	  JDesktopPane desktop = ((PookaDesktopPaneUIFactory) Pooka.getUIFactory()).getMessagePanel();
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
  public void openWith(Attachment pAttachment) {
    if (Pooka.isDebug())
      System.out.println("calling AttachmentHandler.openWith()");

    String mType;
    try {
      mType = pAttachment.getMimeType().toString();
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
	
	dh = pAttachment.getDataHandler();
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
  public void saveAttachment(Attachment pAttachment, Component pComponent) {

    if (pAttachment != null) {
      JFileChooser saveChooser;
      String currentDirectoryPath = Pooka.getProperty("Pooka.tmp.currentDirectory", "");
      if (currentDirectoryPath == "")
	saveChooser = new JFileChooser();
      else
	saveChooser = new JFileChooser(currentDirectoryPath);
      
      String fileName = pAttachment.getName();
      if (fileName != null)
	saveChooser.setSelectedFile(new File(fileName));
      
      int saveConfirm = saveChooser.showSaveDialog(pComponent);
      Pooka.getResources().setProperty("Pooka.tmp.currentDirectory", saveChooser.getCurrentDirectory().getPath(), true);
      if (saveConfirm == JFileChooser.APPROVE_OPTION) {
	try {
	  saveFileAs(pAttachment, saveChooser.getSelectedFile());
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

}

