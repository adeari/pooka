package net.suberic.pooka.filter;
import net.suberic.pooka.gui.MessageProxy;
import java.util.Vector;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;

public class MoveFilterAction implements FilterAction {

    private FolderInfo targetFolder = null;
    private String folderName = null;

    public MoveFilterAction() {
    }

    /**
     * Runs the filterAction on each MessageProxy in the filteredMessages
     * Vector.
     *
     * @param filteredMessages messages which have met the filter condition
     * and need to have the FilterAction performed on them.
     *
     * @return messages which are removed from their original folder
     * by the filter.
     */
    public Vector performFilter(Vector filteredMessages) {
	Vector moved = new Vector();
	for (int i = 0; i < filteredMessages.size(); i++) {
	    //try {
	    MessageProxy current = (MessageProxy) filteredMessages.elementAt(i);
	    current.moveMessage(getTargetFolder());
	    moved.add(current);
	    // catch (MessagingException me) {
	    // failedMove.add(filteredMessage[i];
	    //}
	}
	return moved;
    }

    /**
     * Initializes the FilterAction from the sourceProperty given.
     * 
     * This takes the .targetFolder subproperty of the given sourceProperty
     * and assigns its value as the folderName String.
     */
    
    public void initializeFilter(String sourceProperty) {
	folderName = Pooka.getProperty(sourceProperty + ".targetFolder", "");
    }

    /**
     * Returns the targetFolder.  If the targetFolder has not yet been 
     * loaded. calls Pooka.getStoreManager.getFolder(folderName) to 
     * cache the targetFolder.
     */
    public FolderInfo getTargetFolder() {
	if (targetFolder == null)
	    targetFolder = Pooka.getStoreManager().getFolder(folderName);

	return targetFolder;
    }

}
