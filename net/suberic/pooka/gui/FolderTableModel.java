package net.suberic.pooka.gui;
import javax.swing.table.AbstractTableModel;
import javax.swing.SwingUtilities;
import net.suberic.util.swing.RunnableAdapter;
import java.util.*;


/**
 * This class holds the information about the Messages in a Folder.
 * It actually uses a List of MessageProxys, but, for the Row information,
 * just returns the values from MessageProxy.getTableInformation().
 * It also uses a List of column names.
 *
 */

public class FolderTableModel extends AbstractTableModel {
  static int ADD_MESSAGES = 0;
  static int REMOVE_MESSAGES = 1;

  private List data;
  private List columnNames;
  private List columnSizes;
  private int currentSortColumn = -1;
  private boolean currentAscending = true;
  //private List displayData;
  private List columnKeys;
  
  public FolderTableModel(List newData, List newColumnNames, List newColumnSizes, List newColumnKeys) {
    data=newData;
    columnNames = newColumnNames;
    columnSizes = newColumnSizes;
    columnKeys = newColumnKeys;
  }
  
  public int getColumnCount() {
    return columnNames.size();
  }
  
  public int getRowCount() {
    return data.size();
  }
  
  public String getColumnName(int col) {
    return (String)columnNames.get(col);
  }
  
  /**
   * This returns the value at the given row and column.
   * 
   * note that i actually catch any ArrayOutOfBoundsExceptions and 
   * return a new Object if this happens.
   *
   * As defined in javax.swing.table.TableModel, more or less
   */
  public Object getValueAt(int row, int col) {
    try {
      MessageProxy mp = (MessageProxy) data.get(row);
      if (mp == null)
	return "null";
      else {
	if (! mp.isLoaded()) {
	  return (net.suberic.pooka.Pooka.getProperty("FolderTableModel.unloadedCell", "loading..."));
	} else {
	  Object key = columnKeys.get(col);
	  Object returnValue = ((MessageProxy)data.get(row)).getTableInfo().get(key);
	  // FIXME this is making us load stuff on the display thread.
	  if (returnValue == null) {
	    try {
	      MessageProxy proxy = (MessageProxy)data.get(row);
	      HashMap tableInfo = proxy.getTableInfo();
	      Set keySet = tableInfo.keySet();
	      if (! keySet.contains(key)) {
		if (key instanceof String) {
		  String propertyName = (String)key;
		  
		  if (propertyName.startsWith("FLAG")) 
		    tableInfo.put(propertyName, proxy.getMessageFlag(propertyName));
		  else if (propertyName.equals("attachments"))
		    tableInfo.put(propertyName, new BooleanIcon(proxy.getMessageInfo().hasAttachments(), net.suberic.pooka.Pooka.getProperty("FolderTable.Attachments.icon", "")));
		  else if (propertyName.equals("crypto"))
		    tableInfo.put(propertyName, new BooleanIcon(proxy.getMessageInfo().hasEncryption(), net.suberic.pooka.Pooka.getProperty("FolderTable.Crypto.icon", "")));
		  else if (propertyName.equalsIgnoreCase("subject"))
		    tableInfo.put(propertyName, proxy.new SubjectLine((String) proxy.getMessageInfo().getMessageProperty(propertyName)));
		  else if (propertyName.equalsIgnoreCase("from")) 
		    tableInfo.put(key, proxy.new AddressLine((String) proxy.getMessageInfo().getMessageProperty(propertyName)));
		  else
		    tableInfo.put(key, proxy.getMessageInfo().getMessageProperty(propertyName));
		} else if (key instanceof SearchTermIconManager) {
		  SearchTermIconManager stm = (SearchTermIconManager) key;
		  tableInfo.put(key, new net.suberic.pooka.gui.SearchTermIcon(stm, proxy));
		} else if (key instanceof net.suberic.pooka.RowCounter) {
		  tableInfo.put(key, key);
		}
		
		returnValue = ((MessageProxy)data.get(row)).getTableInfo().get(key);
	      }
	    } catch (Exception e) {
	      // ignore.
	    }
	  }
	  return returnValue;
	}
      }
    } catch (ArrayIndexOutOfBoundsException ae) {
      return  (net.suberic.pooka.Pooka.getProperty("FolderTableModel.exceptionCell", "exception"));
    }
  }
  
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
  
  /**
   * This returns the MessageProxy for the given row.
   */
  public MessageProxy getMessageProxy(int rowNumber) {
    try {
      return (MessageProxy)(data.get(rowNumber));
    } catch (ArrayIndexOutOfBoundsException ae) {
      return null;
    }
  }
  
  /**
   * This returns the row number for the given MessageProxy, 
   * or -1 if the MessageProxy does not exist in the table.
   */
  
  public int getRowForMessage(MessageProxy mp) {
    return data.indexOf(mp);
  }
  
  /**
   * This adds a List of new MessageProxys to the FolderTableModel.
   */
  public void addRows(List newRows) {
    addOrRemoveRows(newRows, FolderTableModel.ADD_MESSAGES);
  }
  
  /**
   * This removes a List of MessageProxys from the FolderTableModel.
   */
  public void removeRows(List rowsDeleted) {
    
    addOrRemoveRows(rowsDeleted, FolderTableModel.REMOVE_MESSAGES);
  }
  
  /**
   * This is a single synchronized method to make sure that we don't 
   * add and/or delete two things at once.  This is usually called
   * from addRows() or removeRows().
   */
  
  public synchronized void addOrRemoveRows(List changedMsg, int addOrRem) {
    final int firstRow, lastRow;
    
    if (changedMsg != null && changedMsg.size() > 0) {
      if (addOrRem == FolderTableModel.ADD_MESSAGES) {
	firstRow = data.size() + 1;
	lastRow = firstRow + changedMsg.size() -1;
	
	data.addAll(changedMsg);
	if (! SwingUtilities.isEventDispatchThread())
	  try {
	    SwingUtilities.invokeAndWait(new RunnableAdapter() {
		public void run() {
		  fireTableRowsInserted(firstRow, lastRow);
		}
	      });
	  } catch (Exception e) {
	  }
	else 
	  fireTableRowsInserted(firstRow, lastRow);
	
      } else if (addOrRem == FolderTableModel.REMOVE_MESSAGES) {
	for (int i = 0; i < changedMsg.size() ; i++) {
	  final int rowNumber = data.indexOf(changedMsg.get(i));
	  if (rowNumber > -1) {
	    data.remove(changedMsg.get(i));
	    
	    if ( ! SwingUtilities.isEventDispatchThread())
	      try {
		SwingUtilities.invokeAndWait(new RunnableAdapter() {
		    public void run() {
		      fireTableRowsDeleted(rowNumber, rowNumber);
		    }
		  });
	      } catch (Exception e) {
	      }
	    else
	      fireTableRowsDeleted(rowNumber, rowNumber);
	    
	  }
	}
      }
    } else {
      System.out.println("got an empty/null added or deleted event.");
      if (changedMsg == null) {
	System.out.println("changedMsg was null.");

      } else if ( changedMsg.size() > 0) {
	System.out.println("changedMsg.size() = 0");
      }
      Thread.currentThread().dumpStack();
    }
  }
  
    public int getColumnSize(int columnIndex) {
      try {
	return Integer.parseInt((String)columnSizes.get(columnIndex));
      } catch (Exception e) {
	return 0;
      }
    }
  
  
  // all of this is the sorting code.
  
  /**
   * This is a class which compares the given MessageProxy's by the
   * value of the particular column in the TableInfo.
   */
  protected class RowComparator implements Comparator {
    
    public int column;
    public Object columnKey;

    public RowComparator(int newColumn) {
      column = newColumn;
      columnKey = columnKeys.get(column);
    }
    
    /**
     * This compares two row objects (MessageProxy's) by column.
     */
    public int compare(Object row1, Object row2) {
      // Check for nulls.
      
      // If both values are null, return 0.
      if (row1 == null && row2 == null) {
	return 0; 
      } else if (row1 == null) { // Define null less than everything. 
	return -1; 
      } else if (row2 == null) { 
	return 1; 
      }
      
      
      Object o1 = ((MessageProxy)row1).getTableInfo().get(columnKey);
      Object o2 = ((MessageProxy)row2).getTableInfo().get(columnKey);
      
      // again, check for nulls.
      if (o1 == null && o2 == null) {
	return 0; 
      } else if (o1 == null) { // Define null less than everything. 
	return -1; 
      } else if (o2 == null) { 
	return 1; 
      }
      
      Class type = o1.getClass();
      
      if (type.getSuperclass() == java.lang.Number.class) {
	Number n1 = (Number)o1;
	double d1 = n1.doubleValue();
	Number n2 = (Number)o2;
	double d2 = n2.doubleValue();
	
	if (d1 < d2) {
	  return -1;
	} else if (d1 > d2) {
	  return 1;
	} else {
	  return 0;
	}
      } else if (type == java.util.Date.class) {
	Date d1 = (Date)o1;
	long n1 = d1.getTime();
	Date d2 = (Date)o2;
	long n2 = d2.getTime();
	
	if (n1 < n2) {
	  return -1;
	} else if (n1 > n2) {
	  return 1;
	} else {
	  return 0;
	}
      } else if (type == String.class) {
	String s1 = (String)o1;
	String s2    = (String)o2;
	int result = s1.compareTo(s2);
	
	if (result < 0) {
	  return -1;
	} else if (result > 0) {
	  return 1;
	} else {
	  return 0;
	}
      } else if (type == Boolean.class) {
	Boolean bool1 = (Boolean)o1;
	boolean b1 = bool1.booleanValue();
	Boolean bool2 = (Boolean)o2;
	boolean b2 = bool2.booleanValue();
	
	if (b1 == b2) {
	  return 0;
	} else if (b1) { // Define false < true
	  return 1;
	} else {
	  return -1;
	}
      } else {
	try {
	  
	  if (Class.forName("java.lang.Comparable").isAssignableFrom(type)) {
	    return ((Comparable)o1).compareTo(o2);
	  }
	} catch (ClassNotFoundException cnfe) {
	  System.out.println("couldn't find class comparable.");
	}
	
	Object v1 = o1;
	String s1 = v1.toString();
	Object v2 = o2;
	String s2 = v2.toString();
	int result = s1.compareTo(s2);
	
	if (result < 0) {
	  return -1;
	} else if (result > 0) {
	  return 1;
	} else {
	  return 0;
	}
      }
    }
    
    public boolean equals(Object comparator2) {
      return super.equals(comparator2);
    }
  }
  
  /**
   * This is for the reverse sort.
   */
  public class ReverseRowComparator extends RowComparator {
    public ReverseRowComparator(int newColumn) {
      super(newColumn);
    }
    
    public int compare(Object row1, Object row2) {
      return (super.compare(row1, row2) * -1);
    }
  }
  
  /**
   * This sorts the data List by the value of the given column.
   */
    public void sortByColumn(int column, boolean ascending) {
      if (ascending)
	java.util.Collections.sort(data, new RowComparator(column));
      else
	java.util.Collections.sort(data, new ReverseRowComparator(column));
      
      this.fireTableChanged(new javax.swing.event.TableModelEvent(this)); 
      
      currentSortColumn = column;
      currentAscending = ascending;
    }
  

  /**
   * Sorts by the given column.  If the column is already the one that 
   * is sorted by, then reverses the sort.
   */
  public void sortByColumn(int column) {
    if (column == currentSortColumn) {
      sortByColumn(column, !currentAscending);
    } else {
      sortByColumn(column, true);
    }
  }
  
  /**
   * Returns all of the MessageProxies in this FolderTableModel.
   */
  public List getAllProxies() {
    return new Vector(data);
  }
}
