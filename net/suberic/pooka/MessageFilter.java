package net.suberic.pooka;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import net.suberic.pooka.gui.MessageProxy;
import net.suberic.pooka.filter.FilterAction;
import java.util.Vector;

/**
 * This represents a MessageFilter.  It contains a SearchTerm and an Action
 * which is done on any messages which match the SearchTerm.
 */
public class MessageFilter {
    private SearchTerm searchTerm;
    private FilterAction action;

    /**
     * Create a MessageFilter from a SearchTerm and a FilterAction.
     */
    public MessageFilter(SearchTerm newSearchTerm, FilterAction newAction) {
	searchTerm = newSearchTerm;
	action = newAction;
    }

    /**
     * Create a MessageFilter from a String which represents a Pooka
     * property.  
     *
     */
    public MessageFilter(String sourceProperty) {
	
    }

    /**
     * This runs the searchTerm test for each MessageProxy in the
     * messages Vector.  Each MessageProxy that matches the searchTerm
     * then has performFilter() run on it.
     * 
     * @return:  all messages removed from the current folder.
     */
    public Vector filterMessages(Vector messages) {
	Vector matches = new Vector();
	for (int i = 0; i < messages.size(); i++) {
	    if (searchTerm.match(((MessageProxy)messages.elementAt(i)).getMessage()))
		matches.add(messages.elementAt(i));
	}

	return performFilter(matches);
    }

    /**
     * Actually performs the FilterAction on the given MessageProxy array.
     * 
     * @param filteredMessages A Vector of MessageProxy objects that are to
     * have the filter performed on them.  
     *
     * @return  all messagesremoved from the current folder.
     */
    public Vector performFilter(Vector filteredMessages) {
	return action.performFilter(filteredMessages);
    }

    // accessor methods.

    public SearchTerm getSearchTerm() {
	return searchTerm;
    }

    public void setSearchTerm(SearchTerm newTerm) {
	searchTerm = newTerm;
    }

    public FilterAction getAction() {
	return action;
    }

    public void setAction(FilterAction newAction) {
	action=newAction;
    }
}
