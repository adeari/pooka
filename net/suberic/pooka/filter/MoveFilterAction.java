package net.suberic.pooka.filter;
import net.suberic.pooka.gui.MessageProxy;
import java.util.Vector;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;

public class MoveFilterAction {

    private FolderInfo targetFolder;

    public MoveFilterAction() {
    }

    /**
     * Runs the filterAction on each MessageProxy in the filteredMessages
     * array.  
     *
     * @param filteredMessages messages which have met the filter condition
     * and need to have the FilterAction performed on them.
     *
     * @return messages which are not removed from their original folder
     * by the filter.
     */
    public MessageProxy[] performFilter(MessageProxy[] filteredMessages) {
	Vector failedMove = new Vector();

	for (int i = 0; i < filteredMessages.length; i++) {
	    //try {
	    filteredMessages[i].moveMessage(targetFolder);
	    // catch (MessagingException me) {
	    // failedMove.add(filteredMessage[i];
	    //}
	}

	MessageProxy[] returnValue = new MessageProxy[failedMove.size()];
	for (int i = 0; i < failedMove.size(); i++) 
	    returnValue[i] = (MessageProxy)failedMove.elementAt(i);

	return returnValue;
    }

    /**
     * Initializes the FilterAction from the sourceProperty given.
     * 
     * This takes the .targetFolder subproperty of the given sourceProperty
     * and initializes the targetFolder from it.
     */
    
    public void intitializeFilter(String sourceProperty) {
	String folderID = Pooka.getProperty(sourceProperty + ".targetFolder", "");
	targetFolder = Pooka.getStoreManager().getFolder(folderID);
    }
}
