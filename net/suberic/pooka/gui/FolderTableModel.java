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

	if (addOrRem == FolderTableModel.ADD_MESSAGES) {
	    firstRow = data.size();
	    lastRow = firstRow + changedMsg.size();

	    data.addAll(changedMsg);
	    fireTableRowsInserted(firstRow, lastRow);
	} else if (addOrRem == FolderTableModel.REMOVE_MESSAGES) {
	    lastRow = ((Integer)changedMsg.elementAt(changedMsg.size() -1)).intValue();
	    firstRow = lastRow;
	    data.removeElementAt(lastRow);
	    
	    int currentRow;
	    for (int i = changedMsg.size() -2; i >= 0 ; i--) {
		currentRow = ((Integer)changedMsg.elementAt(i)).intValue();
		if (currentRow != firstRow -1) {
		    fireTableRowsDeleted(firstRow, lastRow);
		    lastRow = currentRow;
		    firstRow = currentRow;
		} else {
		    firstRow = currentRow;
		}
		data.removeElementAt(currentRow);
	    }
	    fireTableRowsDeleted(firstRow, lastRow);
	}
    }
    

}







