package net.suberic.pooka.gui;
import javax.swing.table.AbstractTableModel;
import javax.swing.SwingUtilities;
import net.suberic.util.swing.RunnableAdapter;
import java.util.Vector;

/**
 * This class holds the information about the Messages in a Folder.
 * It actually uses a Vector of MessageProxys, but, for the Row information,
 * just returns the values from MessageProxy.getTableInformation().
 * It also uses a Vector of column names.
 *
 */

public class FolderTableModel extends AbstractTableModel {
    static int ADD_MESSAGES = 0;
    static int REMOVE_MESSAGES = 1;

    private Vector data;
    private Vector columnNames;
    private Vector columnSizes;
    
    public FolderTableModel(Vector newData, Vector newColumnNames, Vector newColumnSizes) {
	data=newData;
	columnNames = newColumnNames;
	columnSizes = newColumnSizes;
    }

    public int getColumnCount() {
	return columnNames.size();
    }
    
    public int getRowCount() {
	return data.size();
    }
    
    public String getColumnName(int col) {
	return (String)columnNames.elementAt(col);
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
	    return ((MessageProxy)data.elementAt(row)).getTableInfo().elementAt(col);
	} catch (ArrayIndexOutOfBoundsException ae) {
	    return new Object();
	}
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }

    public MessageProxy getMessageProxy(int rowNumber) {
	try {
	    return (MessageProxy)(data.elementAt(rowNumber));
	} catch (ArrayIndexOutOfBoundsException ae) {
	    return null;
	}
    }


    /**
     * This adds a Vector of new MessageProxys to the FolderTableModel.
     */
    public void addRows(Vector newRows) {
	addOrRemoveRows(newRows, FolderTableModel.ADD_MESSAGES);
    }

    /**
     * This removes a Vector of MessageProxys from the FolderTableModel.
     */
    public void removeRows(Vector rowsDeleted) {
	addOrRemoveRows(rowsDeleted, FolderTableModel.REMOVE_MESSAGES);
    }

    /**
     * This is a single synchronized method to make sure that we don't 
     * add and/or delete two things at once.  This is usually called
     * from addRows() or removeRows().
     */
    
    public synchronized void addOrRemoveRows(Vector changedMsg, int addOrRem) {
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
		    final int rowNumber = data.indexOf(changedMsg.elementAt(i));
		    if (rowNumber > -1) {
			data.removeElement(changedMsg.elementAt(i));

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
	}
    }

    public int getColumnSize(int columnIndex) {
	try {
	    return Integer.parseInt((String)columnSizes.elementAt(columnIndex));
	} catch (Exception e) {
	    return 0;
	}
    }
}







