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
      //System.err.println("moving to userInfo; setting default values.");
      String protocol = getManager().getProperty("NewStoreWizard.editors.store.protocol", "imap");
      //System.err.println("protocol = " + protocol);
      if (protocol.equalsIgnoreCase("imap") || protocol.equalsIgnoreCase("pop3")) {
        String user = getManager().getProperty("NewStoreWizard.editors.store.user", "");
        String server = getManager().getProperty("NewStoreWizard.editors.store.server", "");
        //System.err.println("setting username to " + user + "@" + server);
        getManager().setProperty("NewStoreWizard.editors.user.from", user + "@" + server);
        PropertyEditorUI fromEditor = getManager().getPropertyEditor("NewStoreWizard.editors.user.from");
        //System.err.println("got fromEditor " + fromEditor);
        fromEditor.setOriginalValue(user + "@" + server);
        fromEditor.resetDefaultValue();

      } else {
        //System.err.println("local store");
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
        //System.err.println("got fromEditor " + fromEditor);
        fromEditor.setOriginalValue(address);
        fromEditor.resetDefaultValue();
      }
    } else if (newState.equals("outgoingServer") && oldState.equals("userInfo")) {
      // load default values into the smtp configuration.
      String protocol = getManager().getProperty("NewStoreWizard.editors.store.protocol", "imap");
      //System.err.println("protocol = " + protocol);
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

    String accountName = getManager().getCurrentProperty("NewStoreWizard.editors.store.storeName", "testStore");
    String protocol = getManager().getCurrentProperty("NewStoreWizard.editors.store.protocol", "imap");
    if (protocol.equalsIgnoreCase("imap")) {
      returnValue.setProperty("Store." + accountName + ".server", getManager().getCurrentProperty("NewStoreWizard.editors.store.server", ""));
      returnValue.setProperty("Store." + accountName + ".user", getManager().getCurrentProperty("NewStoreWizard.editors.store.user", ""));
      returnValue.setProperty("Store." + accountName + ".password", getManager().getCurrentProperty("NewStoreWizard.editors.store.password", ""));
      returnValue.setProperty("Store." + accountName + ".port", getManager().getCurrentProperty("NewStoreWizard.editors.store.port", ""));

      returnValue.setProperty("Store." + accountName + ".useSubscribed", "true");
      returnValue.setProperty("Store." + accountName + ".SSL", getManager().getCurrentProperty("NewStoreWizard.editors.store.SSL", "none"));
      returnValue.setProperty("Store." + accountName + ".cacheMode", getManager().getCurrentProperty("NewStoreWizard.editors.store.cacheMode", "headersOnly"));
    } else if (protocol.equalsIgnoreCase("pop3")) {
      returnValue.setProperty("Store." + accountName + ".server", getManager().getCurrentProperty("NewStoreWizard.editors.store.server", ""));
      returnValue.setProperty("Store." + accountName + ".user", getManager().getCurrentProperty("NewStoreWizard.editors.store.user", ""));
      returnValue.setProperty("Store." + accountName + ".password", getManager().getCurrentProperty("NewStoreWizard.editors.store.password", ""));
      returnValue.setProperty("Store." + accountName + ".port", getManager().getCurrentProperty("NewStoreWizard.editors.store.port", ""));

      returnValue.setProperty("Store." + accountName + ".SSL", getManager().getCurrentProperty("NewStoreWizard.editors.store.SSL", "none"));
      returnValue.setProperty("Store." + accountName + ".useMaildir", "true");
      /*
      returnValue.setProperty("Store." + accountName + ".leaveMessagesOnServer", getManager().getCurrentProperty("NewStoreWizard.editors.store.leaveOnServer", "true"));
      if (manager.getCurrentProperty("NewStoreWizard.editors.store.leaveOnServer", "true").equalsIgnoreCase("true")) {
        returnValue.setProperty("Store." + accountName + ".deleteOnServerOnLocalDelete", "true");
      }
      */
    } else if (protocol.equalsIgnoreCase("mbox")) {
      returnValue.setProperty("Store." + accountName + ".inboxLocation", getManager().getCurrentProperty("NewStoreWizard.editors.store.inboxLocation", "/var/spool/mail/" + System.getProperty("user.name")));
      returnValue.setProperty("Store." + accountName + ".mailDirectory", getManager().getCurrentProperty("NewStoreWizard.editors.store.mailDirectory", getManager().getCurrentProperty("Pooka.cacheDirectory", System.getProperty("user.home") + java.io.File.separator + ".pooka")));
    } else if (protocol.equalsIgnoreCase("maildir")) {
      returnValue.setProperty("Store." + accountName + ".mailDir", getManager().getCurrentProperty("NewStoreWizard.editors.store.mailDir", System.getProperty("user.home") + java.io.File.separator + "Maildir"));
    }

    List<String> storeList = getManager().getPropertyAsList("Store", "");
    storeList.add(accountName);

    returnValue.setProperty("Store", net.suberic.util.VariableBundle.convertToString(storeList));

    return returnValue;
  }

  /**
   * Creates the userProperties from the wizard values.
   */
  public Properties createUserProperties() {
    Properties returnValue = new Properties();

    String accountName = getManager().getCurrentProperty("NewStoreWizard.editors.store.storeName", "testStore");

    String defaultUser = getManager().getCurrentProperty("NewStoreWizard.editors.user.userProfile", "__default");
    if (defaultUser.equals("__new")) {

    } else {
      returnValue.setProperty("Store." + accountName + ".defaultProfile", defaultUser);
    }
    return returnValue;
  }

  /**
   * Creates the smtpProperties from the wizard values.
   */
  public Properties createSmtpProperties() {
    Properties returnValue = new Properties();

    String accountName = getManager().getCurrentProperty("NewStoreWizard.editors.store.storeName", "testStore");

    String smtpServer = getManager().getCurrentProperty("NewStoreWizard.editors.user.smtp.outgoingServer", "__default");
    if (smtpServer.equals("__new")) {

    } else {
      //returnValue.setProperty("Store." + accountName + ".defaultProfile", defaultUser);
    }
    //returnValue.setProperty("Store." + accountName + ".connection", Pooka.getProperty("Pooka.connection.defaultName", "default"));
    //returnValue.setProperty("OutgoingServer." + smtpServerName + ".sendOnConnect", "true");
    return returnValue;
  }

  /**
   * Adds all of the values from the given Properties to the
   * PropertyEditorManager.
   */
  void addAll(Properties props) {
    Set<String> names = props.stringPropertyNames();
    for (String name: names) {
      System.err.println("setting property " + name + ", value " + props.getProperty(name));
      getManager().setProperty(name, props.getProperty(name));
    }
  }
}
