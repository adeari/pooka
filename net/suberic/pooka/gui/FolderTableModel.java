package net.suberic.pooka.gui;
import javax.swing.table.AbstractTableModel;
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
    
    
    public FolderTableModel(Vector newData, Vector newColumnNames) {
	data=newData;
	columnNames = newColumnNames;
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
    
    public Object getValueAt(int row, int col) {
	return ((MessageProxy)data.elementAt(row)).getTableInfo().elementAt(col);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }

    public MessageProxy getMessageProxy(int rowNumber) {
	return (MessageProxy)(data.elementAt(rowNumber));
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
	int firstRow, lastRow;

	if (changedMsg != null && changedMsg.size() > 0) {
	    if (addOrRem == FolderTableModel.ADD_MESSAGES) {
		firstRow = data.size() + 1;
		lastRow = firstRow + changedMsg.size() -1;
		
		data.addAll(changedMsg);
		fireTableRowsInserted(firstRow, lastRow);
		System.out.println("inserted rows " + firstRow + " to " + lastRow + ".");
	    } else if (addOrRem == FolderTableModel.REMOVE_MESSAGES) {
		for (int i = 0; i < changedMsg.size() ; i++) {
		    int rowNumber = data.indexOf(changedMsg.elementAt(i));
		    if (rowNumber > -1) {
			fireTableRowsDeleted(rowNumber, rowNumber);
			data.removeElement(changedMsg.elementAt(i));
		    }
		}
	    }
	} else {
	    System.out.println("got an empty/null added or deleted event.");
	}
    }
    

}







