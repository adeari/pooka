package net.suberic.util.prefs;
import java.util.*;
import java.util.prefs.*;
import java.sql.*;

/**
 * A Preference implementation that stores it values in a database via JDBC.
 */
public class JDBCPreferences extends AbstractPreferences {

  /**
   * Creaes a new JDBCPreferences instance.
   */
  public JDBCPreferences(AbstractPreferences pParent, String pTableName) {
    super(pParent, pTableName);
  }

  protected String getSpi(String pKey) {
    return null;
  }

  protected void putSpi(String pKey, String pValue) {

  }

  protected void removeSpi(String pKey) {
  }

  protected void removeNodeSpi() throws BackingStoreException {

  }

  protected String[] childrenNamesSpi() throws BackingStoreException {
    return null;
  }

  protected String[] keysSpi() throws BackingStoreException {
    return null;
  }


  protected AbstractPreferences childSpi(String pName) {
    return null;
  }

  protected void syncSpi() throws BackingStoreException {
  }

  protected void flushSpi() throws BackingStoreException {
  }

}
