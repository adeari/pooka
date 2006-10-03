package net.suberic.pooka.gui.propedit;
import java.util.*;
import net.suberic.util.gui.propedit.*;

/**
 * The controller class for the NewStoreWizard.
 */
public class NewStoreWizardController extends WizardController {

  /**
   * Creates a NewStoreWizardController.
   */
  public NewStoreWizardController(String sourceTemplate, WizardEditorPane wep) {
    super(sourceTemplate, wep);
  }
  /**
   * Checks the state transition to make sure that we can move from
   * state to state.
   */
  public void checkStateTransition(String oldState, String newState) throws PropertyValueVetoException {
    getEditorPane().setValue(oldState);
    if (newState.equals("userInfo") && oldState.equals("storeConfig")) {
      // load default values into the user configuration.
      System.err.println("moving to userInfo; setting default values.");
      String protocol = getManager().getProperty("NewStoreWizard.editors.store.protocol", "imap");
      System.err.println("protocol = " + protocol);
      if (protocol.equalsIgnoreCase("imap") || protocol.equalsIgnoreCase("pop3")) {
        String user = getManager().getProperty("NewStoreWizard.editors.store.user", "");
        String server = getManager().getProperty("NewStoreWizard.editors.store.server", "");
        System.err.println("setting username to " + user + "@" + server);
        getManager().setProperty("NewStoreWizard.editors.user.from", user + "@" + server);
        PropertyEditorUI fromEditor = getManager().getPropertyEditor("NewStoreWizard.editors.user.from");
        System.err.println("got fromEditor " + fromEditor);
        fromEditor.setOriginalValue(user + "@" + server);
        fromEditor.resetDefaultValue();

      } else {
        System.err.println("local store");
        String username = System.getProperty("user.name");
        String hostname = "localhost";
        try {
          java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
          hostname = localMachine.getHostName();

        } catch(java.net.UnknownHostException uhe) {
          // just use 'localhost'
        }
        String address = username + "@" + hostname;
        getManager().setProperty("NewStoreWizard.editors.user.from", address);
        PropertyEditorUI fromEditor = getManager().getPropertyEditor("NewStoreWizard.editors.user.from");
        System.err.println("got fromEditor " + fromEditor);
        fromEditor.setOriginalValue(address);
        fromEditor.resetDefaultValue();
      }
    } else if (newState.equals("outgoingServer") && oldState.equals("userInfo")) {
      // load default values into the smtp configuration.
      String protocol = getManager().getProperty("NewStoreWizard.editors.store.protocol", "imap");
      System.err.println("protocol = " + protocol);
      if (protocol.equalsIgnoreCase("imap") || protocol.equalsIgnoreCase("pop3")) {
        String user = getManager().getProperty("NewStoreWizard.editors.store.user", "");
        String server = getManager().getProperty("NewStoreWizard.editors.store.server", "");
        getManager().setProperty("NewStoreWizard.editors.smtp.user", user);
        PropertyEditorUI userEditor = getManager().getPropertyEditor("NewStoreWizard.editors.smtp.user");
        userEditor.setOriginalValue(user);
        userEditor.resetDefaultValue();

        getManager().setProperty("NewStoreWizard.editors.smtp.server", server);
        PropertyEditorUI serverEditor = getManager().getPropertyEditor("NewStoreWizard.editors.smtp.server");
        serverEditor.setOriginalValue(server);
        serverEditor.resetDefaultValue();
      } else {
        getManager().setProperty("NewStoreWizard.editors.smtp.server", "localhost");
        PropertyEditorUI serverEditor = getManager().getPropertyEditor("NewStoreWizard.editors.smtp.server");
        serverEditor.setOriginalValue("localhost");
        serverEditor.resetDefaultValue();

      }
    } else if (newState.equals("storeName") && oldState.equals("outgoingServer")) {
      String user = getManager().getProperty("NewStoreWizard.editors.store.user", "");
      String server = getManager().getProperty("NewStoreWizard.editors.store.server", "");
      String storeName = user + "@" + server;
      getManager().setProperty("NewStoreWizard.editors.store.storeName", storeName);
      PropertyEditorUI storeNameEditor = getManager().getPropertyEditor("NewStoreWizard.editors.store.storeName");
      storeNameEditor.setOriginalValue(storeName);
      storeNameEditor.resetDefaultValue();
    }
  }

  /**
   * Finsihes the wizard.
   */
  public void finishWizard() throws PropertyValueVetoException {
    Properties storeProperties = createStoreProperties();
    Properties userProperties = createUserProperties();
    Properties smtpProperties = createSmtpProperties();
    getManager().clearValues();

    addAll(storeProperties);
    addAll(userProperties);
    addAll(smtpProperties);

    getManager().commit();
    getEditorPane().getWizardContainer().closeWizard();
  }

  /**
   * Creates the storeProperties from the wizard values.
   */
  public Properties createStoreProperties() {
    Properties returnValue = new Properties();
    return returnValue;
  }

  /**
   * Creates the userProperties from the wizard values.
   */
  public Properties createUserProperties() {
    Properties returnValue = new Properties();

    return returnValue;
  }

  /**
   * Creates the smtpProperties from the wizard values.
   */
  public Properties createSmtpProperties() {
    Properties returnValue = new Properties();

    return returnValue;
  }

  /**
   * Adds all of the values from the given Properties to the
   * PropertyEditorManager.
   */
  void addAll(Properties props) {
    Set<String> names = props.stringPropertyNames();
    for (String name: names) {
      getManager().setProperty(name, props.getProperty(name));
    }
  }
}
