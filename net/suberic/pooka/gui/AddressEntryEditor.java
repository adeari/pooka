package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import net.suberic.util.*;
import net.suberic.util.gui.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Cursor;
import java.util.Properties;

/**
 * A property editor which edits an AddressBookEntry.
 */
public class AddressEntryEditor extends CompositeEditorPane {
  AddressBookEntry entry;

  /**
   * Creates an AddressEntryEditor from an AddressBookEntry and a 
   * VariableBundle.
   */
  public AddressEntryEditor(PropertyEditorFactory newFactory, AddressBookEntry newEntry, VariableBundle sourceBundle) {
    super(BoxLayout.X_AXIS);
    entry = newEntry;
    System.out.println("creating new AddressEntryEditor; newFactory = " + newFactory + ", newEntry = " + newEntry);
    PropertyEditorFactory nFactory;
    VariableBundle wrappedBundle = new VariableBundle(entry.getProperties(), sourceBundle);
    
    if (newFactory instanceof PookaDesktopPropertyEditorFactory) {
      nFactory = new PookaDesktopPropertyEditorFactory(wrappedBundle);
      ((DesktopPropertyEditorFactory)newFactory).setDesktop(((DesktopPropertyEditorFactory)newFactory).getDesktop());
    } else {
      nFactory = new PookaExternalPropertyEditorFactory(wrappedBundle);
    }
    //PropertyEditorFactory nFactory; = new AddressPropertyEditorFactory(newFactory, newEntry);
    configureEditor(nFactory, "currentAddress", "currentAddress", nFactory.getBundle(), true);
  }
  
  public void setValue() {
    if (isEnabled()) {
      for (int i = 0; i < editors.size(); i++) {
	((DefaultPropertyEditor)(editors.elementAt(i))).setValue();
      }
    }

    VariableBundle wrappedBundle = factory.getBundle();
    try {
      entry.setAddress(new javax.mail.internet.InternetAddress(wrappedBundle.getProperty("currentAddress.address")));
    } catch (javax.mail.internet.AddressException ae) {

    }
    entry.setPersonalName(wrappedBundle.getProperty("currentAddress.personalName"));
    entry.setFirstName(wrappedBundle.getProperty("currentAddress.firstName"));
    entry.setLastName(wrappedBundle.getProperty("currentAddress.lastName"));
  }
  
  public class AddressPropertyEditorFactory extends PropertyEditorFactory {
    PropertyEditorFactory wrappedFactory;
    VariableBundle wrappedBundle;
    
    public AddressPropertyEditorFactory(PropertyEditorFactory newWrappedFactory, AddressBookEntry entry) {
      super(new VariableBundle(entry.getProperties(), newWrappedFactory.getBundle()));
      wrappedFactory = newWrappedFactory;
      Properties props = entry.getProperties();
      wrappedBundle = new VariableBundle(props, wrappedFactory.getBundle());
      System.out.println("for ref, currentAddress.personalName in the wrappedBundle is " + wrappedBundle.getProperty("currentAddress.personalName", ""));
      System.out.println("meanwhile,  currentAddress.personalName in the props is " + props.getProperty("currentAddress.personalName"));
    }

    public DefaultPropertyEditor createEditor(String property) {
      return wrappedFactory.createEditor(property);
    }
    public DefaultPropertyEditor createEditor(String property, String typeTemplate) {
      return wrappedFactory.createEditor(property, typeTemplate);
    }

    public VariableBundle getBundle() {
      return wrappedBundle;
    }
  }
}
