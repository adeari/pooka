package net.suberic.pooka.gui;

import javax.swing.*;
import javax.mail.internet.*;
import javax.mail.*;
import java.util.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import javax.activation.*;

import net.suberic.pooka.Pooka;
import net.suberic.pooka.ExternalLauncher;
import java.awt.*;

/**
 * This class basically creates a visual list of the parts of a 
 * MimeMessage.
 */

public class AttachmentPane extends JPanel {
    JList listBox;
    Hashtable mappingTable = new Hashtable();
    JPopupMenu popupMenu;

    public AttachmentPane (MimeMessage mMsg) {
	super();

	// first see if we really should be creating this or not.
	Object content = null;
	try {
	    content = mMsg.getContent();
	} catch (Exception e) {
	    return;
	}
	
	if (content == null || !(content instanceof MimeMultipart))
	    return;


	// so we probably should actually have an attachmentPane.

	MimeMultipart msgContent = (MimeMultipart) content;

	Vector listElements = new Vector();

	try {
	    for (int i = 0; i < msgContent.getCount(); i++) {
		MimeBodyPart mbp = (MimeBodyPart)msgContent.getBodyPart(i);
		String title = mbp.getContentType();
		listElements.add(title);
		mappingTable.put(title, mbp);
	    }
	} catch (javax.mail.MessagingException me) {
	}

	popupMenu = createPopupMenu();

	listBox = new JList(listElements);

	listBox.addMouseListener(new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
		    getPopupMenu().show(AttachmentPane.this, e.getX(), e.getY());
		}
	       
	    }
	});

	this.add(new JScrollPane(listBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }


    /**
     * getSelectedPart() will return the selected MimeBodyPart, as 
     * converted from the mappingTable.
     */

    public MimeBodyPart getSelectedPart() {
	String selectedTitle = (String)listBox.getSelectedValue();

	if (selectedTitle == null) 
	    return null;
	else {
	    return (MimeBodyPart)mappingTable.get(selectedTitle);
	}
	
    }

    public void openSelectedAttachment() {
	MimeBodyPart mbp = getSelectedPart();
	if (mbp != null) {
	    DataHandler dh = null;
	    try {
		dh = mbp.getDataHandler();
	    } catch (MessagingException me) {
	    }
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
			    frameViewer.setTitle(mbp.getFileName());
			} catch (Exception e) {
			}
			frameViewer.show();
		    } else if (beanViewer instanceof Component) {
			JFrame frame = new JFrame();
			frame.getContentPane().add((Component)beanViewer);
			frame.show();
		    } else if (beanViewer instanceof ExternalLauncher) {
			((ExternalLauncher)beanViewer).show();
		    } else if (cmds[0].getCommandClass().equals("net.suberic.pooka.ExternalLauncher")) {
			try {
			    ExternalLauncher el = new ExternalLauncher();
			    el.setCommandContext(cmds[0].getCommandName(), null);
			    el.show();
			} catch (IOException ioe) {
			    //
			}
		    }
		}
	    }
	}
    }

    public void openWith() {
    }

    public void saveAttachment() {
	MimeBodyPart mbp = getSelectedPart();
	if (mbp != null) {
	    JFileChooser saveChooser = new JFileChooser();

	    try {
		saveChooser.setSelectedFile(new File(mbp.getFileName()));
	    } catch (MessagingException me) {
	    }

	    int saveConfirm = saveChooser.showSaveDialog(this);
	    if (saveConfirm == JFileChooser.APPROVE_OPTION) 
		try {
		    saveFileAs(mbp, saveChooser.getSelectedFile());
		} catch (IOException exc) {
		    //JOptionPane.showInternalMessageDialog(((FolderPanel)getParentContainer()).getMainPanel().getMessagePanel(), Pooka.getProperty("error.SaveFile", "Error saving file") + "\n" + ioe.getMessage());
		    System.out.println(Pooka.getProperty("error.SaveFile", "Error saving file") + "\n" + exc.getMessage());
		}
	}
    }

    public void saveFileAs(MimeBodyPart mbp, File saveFile) throws IOException {
	InputStream decodedIS;
	BufferedOutputStream outStream;

	try {
	    decodedIS = mbp.getInputStream();
	} catch (MessagingException me) {
	    throw new IOException(me.getMessage());
	}
	outStream = new BufferedOutputStream(new FileOutputStream(saveFile));
	try {
	    int b=0;
	    byte[] buf = new byte[32768];
	    
	    b = decodedIS.read(buf);
	    while (b != -1) {
		outStream.write(buf, 0, b);
		b = decodedIS.read(buf);
	    }

	} finally {
	    outStream.flush();
	    outStream.close();
	}
    }


    public JPopupMenu getPopupMenu() {
	return popupMenu;
    }

    protected JPopupMenu createPopupMenu() {
	String key = "AttachmentPane.Actions";
	StringTokenizer iKeys = null;
	try {
	    iKeys = new StringTokenizer(Pooka.getProperty(key), ":");
	} catch (MissingResourceException mre) {
	    try {
		System.err.println(Pooka.getProperty("error.NoSuchResource") + " " + mre.getKey());
	    } catch (MissingResourceException mretwo) {
		System.err.println("Unable to load resource " + mre.getKey());
	    } finally {
	      return null;
	    }
	}

	String currentToken;
	JPopupMenu menu;
	
	menu = new JPopupMenu();

	while (iKeys.hasMoreTokens()) {
	    currentToken=iKeys.nextToken();
	    if (currentToken.equals("-")) {
		menu.addSeparator();
	    } else {
		JMenuItem mi = createMenuItem(key, currentToken);
		menu.add(mi);
	    }
	}
	return menu;
    }

    protected JMenuItem createMenuItem(String menuID, String menuItemID) {
	JMenuItem mi;
	try {
	    mi = new JMenuItem(Pooka.getProperty(menuID + "." + menuItemID + ".Label"));
	} catch (MissingResourceException mre) {
	    mi = new JMenuItem(menuItemID);
	}
	
	java.net.URL url = null;
	
	try {
	    url = this.getClass().getResource(Pooka.getProperty(menuID + "." + menuItemID + ".Image"));
	} catch (MissingResourceException mre) {
	}
	if (url != null) {
	    mi.setHorizontalTextPosition(JButton.RIGHT);
	    mi.setIcon(new ImageIcon(url));
	}
	
	String cmd = Pooka.getProperty(menuID + "." + menuItemID + ".Action", menuItemID);
	    
	mi.setActionCommand(cmd);
	
	Action itemAction = getActionByName(cmd);
	if (itemAction != null) {
	    mi.addActionListener(itemAction);
	    mi.setEnabled(true);
	}
	return mi;
    }

    public Action getActionByName(String actionName) {
	System.out.println("getting action " + actionName);
	Action[] actionList = getActions();
	for (int i = 0; i < actionList.length; i++) {
	    if (actionName.equals((String)actionList[i].getValue(Action.NAME))) {
		System.out.println("matched actionName " + actionName);
		return actionList[i];
	    }
	}
	return null;
		
    }

    public Action[] defaultActions = {
	new OpenAction(),
	new OpenWithAction(),
	new SaveAsAction()
    };

    public Action[] getActions() {
	return getDefaultActions();
    }
    
    public Action[] getDefaultActions() {
	return defaultActions;
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
	    super("file-open");
	}
	
        public void actionPerformed(ActionEvent e) {
	    openSelectedAttachment();
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

}
    

