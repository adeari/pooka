package net.suberic.pooka.gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import javax.mail.MessagingException;
import net.suberic.pooka.*;

/**
 * A dialog that lets you choose whehter to cancel the sending or try
 * another mailserver.
 */
public class SendFailedDialog extends JPanel {

  // the resource for this component.
  static String P_RESOURCE = "SendFailedDialog";

  // the action commands
  static String S_ABORT = "abort";
  static String S_SEND_OTHER_SERVER = "send";
  static String S_SAVE_TO_OUTBOX = "outbox";

  // the mailserver action commands
  static String S_NOTHING = "nothing";
  static String S_SESSION_DEFAULT = "session";
  static String S_CHANGE_DEFAULT = "change_default";

  // the MessagingException
  MessagingException mException;

  // the original mailserver
  OutgoingMailServer mOriginalMailServer;

  // the display panel.
  JTextArea mMessageDisplay;

  // a JList that shows all available mailservers.
  JList mMailServerList = null;

  // a JRadioButton that shows the choices of what to do with the failed
  // send.
  ButtonGroup mActionButtons = null;

  // a JRadioButton that shows the choices of what to do with the newly
  // chosen mailserver.
  ButtonGroup mServerDefaultButtons = null;

  /**
   * Creates a new SendFailedDialog.
   */
  public SendFailedDialog(OutgoingMailServer pServer, MessagingException me) {
    mException = me;
    mOriginalMailServer = pServer;
  } 
  
  /**
   * Configures this component.
   */
  public void configureComponent() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel messagePanel = new JPanel();
    mMessageDisplay = new JTextArea(Pooka.getProperty("error.MessageUI.sendFailed", "Failed to send Message.") + "\n" + mException.getMessage());
    messagePanel.add(mMessageDisplay);

    JPanel buttonPanel = createActionPanel();

    JPanel actionPanel = createServerDefaultPanel();

    mMailServerList = createMailServerList();

    JPanel choicePanel = new JPanel();
    choicePanel.setBorder(BorderFactory.createEtchedBorder());
    choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.X_AXIS));

    choicePanel.add(buttonPanel);
    choicePanel.add(mMailServerList);
    choicePanel.add(actionPanel);

    this.add(messagePanel);
    this.add(choicePanel);
  }

  /**
   * Creates a JRadioButton to show the available actions.
   */
  protected JPanel createActionPanel() {
    JPanel returnValue = new JPanel();
    returnValue.setBorder(BorderFactory.createEtchedBorder());
    returnValue.setLayout(new BoxLayout(returnValue, BoxLayout.Y_AXIS));

    ButtonGroup choices = new ButtonGroup();

    JRadioButton abortButton = new JRadioButton();
    abortButton.setText(Pooka.getProperty(P_RESOURCE + ".cancel", "Cancel send"));
    abortButton.setActionCommand(S_ABORT);
    choices.add(abortButton);
    returnValue.add(abortButton);

    JRadioButton sendButton = new JRadioButton();
    sendButton.setText(Pooka.getProperty(P_RESOURCE + ".send", "Send using another server"));
    sendButton.setActionCommand(S_SEND_OTHER_SERVER);
    choices.add(sendButton);
    returnValue.add(sendButton);

    JRadioButton outboxButton = new JRadioButton();
    outboxButton.setText(Pooka.getProperty(P_RESOURCE + ".outbox", "Save to outbox"));
    outboxButton.setActionCommand(S_SAVE_TO_OUTBOX);
    choices.add(outboxButton);
    returnValue.add(outboxButton);

    mActionButtons = choices;

    return returnValue;
  }

  /**
   * Creates a JList to show the choices of mailservers.
   */
  public JList createMailServerList() {
    Vector v = Pooka.getOutgoingMailManager().getOutgoingMailServerList();
    JList returnValue = new JList(v);
    return returnValue;
  }

  /**
   * Creates a JRadioButton to show the choices of what to do with the 
   * newly selected mailserver.
   */
  public JPanel createServerDefaultPanel() {

    JPanel returnValue = new JPanel();
    returnValue.setBorder(BorderFactory.createEtchedBorder());
    returnValue.setLayout(new BoxLayout(returnValue, BoxLayout.Y_AXIS));

    ButtonGroup choices = new ButtonGroup();

    JRadioButton current = new JRadioButton();
    current.setText(Pooka.getProperty(P_RESOURCE + ".noDefault", "Keep default"));
    current.setActionCommand(S_NOTHING);
    choices.add(current);
    returnValue.add(current);

    current = new JRadioButton();
    current.setText(Pooka.getProperty(P_RESOURCE + ".defaultThisSession", "Set as default for this session"));
    current.setActionCommand(S_SESSION_DEFAULT);
    choices.add(current);
    returnValue.add(current);
 
    current = new JRadioButton();
    current.setText(Pooka.getProperty(P_RESOURCE + ".defaultPerm", "Set as default"));
    current.setActionCommand(S_CHANGE_DEFAULT);
    choices.add(current);
    returnValue.add(current);

    mServerDefaultButtons = choices;
    return returnValue;

  }

  /**
   * Whether or not to try a resend, or just fail.
   */
  public boolean resendMessage() {
    return false;
  }

  /**
   * The MailServer selected.
   */
  public OutgoingMailServer getMailServer() {
    return null;
  }

  /**
   * What to do with the selected MailServer.
   */
  public int getMailServerAction() {
    return -1;
  }
}
