package net.suberic.pooka;

/**
 * Defines the methods used to store Addresses.
 */
public interface AddressBook {

  public void configureAddressBook(String id);

  /**
   * Gets and appropriate AddressMatcher.
   */
  public AddressMatcher getAddressMatcher();

  /**
   * Adds an AddressBookEntry to the AddressBook.
   */
  public void addAddress(AddressBookEntry newEntry);

  /**
   * Removes an AddressBookEntry from the AddressBook.
   */
  public void removeAddress(AddressBookEntry removeEntry);

  /**
   * Gets the ID for this address book.
   */
  public String getAddressBookID();
}
