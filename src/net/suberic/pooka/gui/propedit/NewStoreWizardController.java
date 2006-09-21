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

      } else {
        System.err.println("local store");
        getManager().setProperty("NewStoreWizard.editors.user.from", "user@localhost");

      }
    }
  }


}
