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
import net.suberic.pooka.Pooka;
import net.suberic.pooka.ExternalLauncher;
import java.awt.*;

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
	    if (msg.getAttachments() != null)
		return msg.getAttachments().size();
	    else
		return 0;
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
	    Vector v = msg.getAttachments();
	    if (v != null && row < v.size()) {
		if (column == 0)
		    try {
			String name = (((MimeBodyPart)v.elementAt(row)).getFileName()); 
			if (name != null)
			    return name;
			else
			    return Pooka.getProperty("AttachmentPane.error.FileNameUnavailable", "Unavailable");
		    } catch (MessagingException me) {
			return Pooka.getProperty("AttachmentPane.error.FileNameUnavailable", "Unavailable");
		    }
		else if (column == 1)
		    try {
			return (((MimeBodyPart)v.elementAt(row)).getContentType());    
		    } catch (MessagingException me) {
			return Pooka.getProperty("AttachmentPane.error.FileTypeUnavailable", "Unavailable");
		    }
	    }
	    // if it's not a valid request, just return null.
	    return null;
	}

	/**
	 * A convenience method to return a particular MimeBodyPart
	 *
	 * Returns null if there is no entry at that row.
	 */
	public MimeBodyPart getPartAtRow(int row) {
	    if (row < msg.getAttachments().size())
		return (MimeBodyPart)msg.getAttachments().elementAt(row);
	    else
		return null;
	}

	public String getColumnName(int columnIndex) {
	    if (columnIndex >= 0 && columnIndex < columnNames.size())
		return (String)columnNames.elementAt(columnIndex);
	    else
		return null;
	}
    }

    JTable table;
    AttachmentTableModel tableModel;
    JPopupMenu popupMenu;
    MessageProxy message;
    Action[] defaultActions;

    public AttachmentPane (MessageProxy msg) {
	super();

	message=msg;
	defaultActions = createDefaultActions();

	popupMenu = createPopupMenu();

	tableModel = new AttachmentTableModel(message);

	table = new JTable(tableModel);

	tableModel.addTableModelListener(table);

	table.addMouseListener(new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {
		if (e.getClickCount() == 2) {
		    MimeBodyPart selectedPart = getSelectedPart();
		    String actionCommand = Pooka.getProperty("AttachmentPane.2xClickAction", "file-open");
		    if (selectedPart != null) {
			Action clickAction = getActionByName(actionCommand);
			if (clickAction != null) {
			    clickAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
			    
			}
		    }
		} else if (SwingUtilities.isRightMouseButton(e)) {
		    getPopupMenu().show(AttachmentPane.this, e.getX(), e.getY());
		} 
	       
	    }
	});

	this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

	// the width will be resized; the only important part here is 
	// the height.

	Dimension prefSize = this.getPreferredSize();
	int defaultHeight = Integer.parseInt(Pooka.getProperty("Pooka.attachmentPanel.vsize", "100"));
	if (prefSize.getHeight() > defaultHeight)
	    this.setPreferredSize(new Dimension((int)prefSize.getWidth(), defaultHeight));
	
    }


    /**
     * getSelectedPart() will return the selected MimeBodyPart.
     */

    public MimeBodyPart getSelectedPart() {
	return getTableModel().getPartAtRow(getTable().getSelectedRow());
    }

    /**
     * This opens up the selected Attachment using the default handler
     * for the Attachment's Mime type.
     */
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
		} else {
		    openWith();
		}
	    }
	}
    }

    /**
     * This opens the Attachment with the program of the user's choice.
     */
    public void openWith() {
	if (Pooka.isDebug())
	    System.out.println("calling AttachmentPane.openWith()");

	String inputMessage = Pooka.getProperty("AttchmentPane.openWith.message", "Enter the command with which \nto open the attchment.");
	String inputTitle = Pooka.getProperty("AttachmentPane.openWith.title", "Open Attachment With");

	String newCmd = message.getMessageWindow().showInputDialog(inputMessage, inputTitle);

	if (newCmd != null) {
	    if (newCmd.indexOf("%s") == -1)
		newCmd = newCmd.concat(" %s");
	    
	    MimeBodyPart mbp = getSelectedPart();
	    if (mbp != null) {
		DataHandler dh = null;
		try {
		    dh = mbp.getDataHandler();
		} catch (MessagingException me) {
		}
		if (dh != null) {
		    dh.setCommandMap(Pooka.getMailcap());
		    try {
			ExternalLauncher el = new ExternalLauncher();
			el.setCommandContext(newCmd, dh);
			el.show();
		    } catch (IOException ioe) {
				//
		    }
		}
	    }
	}
    }
    

    /**
     * This opens up a JFileChooser to let the user choose under what
     * name and where the selected Attachment should be saved.  It then
     * calls saveAttachmentAs() to save the file.
     */
    public void saveAttachment() {
	MimeBodyPart mbp = getSelectedPart();
	if (mbp != null) {
	    JFileChooser saveChooser = new JFileChooser();

	    try {
		String fileName = mbp.getFileName();
		if (fileName != null)
		    saveChooser.setSelectedFile(new File(fileName));
	    } catch (MessagingException me) {
	    }

	    int saveConfirm = saveChooser.showSaveDialog(this);
	    if (saveConfirm == JFileChooser.APPROVE_OPTION) 
		try {
		    saveFileAs(mbp, saveChooser.getSelectedFile());
		} catch (IOException exc) {
		    message.getMessageWindow().showError(Pooka.getProperty("error.SaveFile", "Error saving file") + ":\n", Pooka.getProperty("error.SaveFile", "Error saving file"), exc);
		}
	}
    }

    /**
     * This actually saves the MimeBodyPart as the File saveFile.
     */
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

    /**
     * This removes the Attachment from the message.
     */

    public void removeAttachment() {
	int selectedIndex = getTable().getSelectedRow();
	MimeBodyPart mbp = getSelectedPart();
	((NewMessageProxy)message).detachFile(mbp);
    }

    public JPopupMenu getPopupMenu() {
	return popupMenu;
    }

    public AttachmentTableModel getTableModel() {
	return tableModel;
    }

    public JTable getTable() {
	return table;
    }

    protected JPopupMenu createPopupMenu() {
	String key;
	if (message instanceof NewMessageProxy)
	    key = "AttachmentPane.NewMsgActions";
	else 
	    key = "AttachmentPane.Actions";

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
	Action[] actionList = getActions();
	for (int i = 0; i < actionList.length; i++) {
	    if (actionName.equals((String)actionList[i].getValue(Action.NAME))) {
		return actionList[i];
	    }
	}
	return null;
		
    }

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
	    System.out.println("open-with");
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
    

