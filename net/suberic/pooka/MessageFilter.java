package net.suberic.pooka;
import javax.mail.*;
import javax.mail.search.SearchTerm;
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
	searchTerm = Pooka.getSearchManager().generateSearchTermFromProperty(sourceProperty);
	
	action = generateFilterAction(sourceProperty + ".action");
    }

    /**
     * Generates a FilterAction from the given property.
     */

    public FilterAction generateFilterAction(String actionProperty) {
	String className = Pooka.getProperty(actionProperty + ".class", "");
	try {
	    Class filterClass = Class.forName(className);
	    FilterAction newAction = (FilterAction)filterClass.newInstance();
	    newAction.initializeFilter(actionProperty);
	    return newAction;
	} catch (Exception e) {
	    System.out.println("caught exception initializing filter " + e);
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * This runs the searchTerm test for each MessageInfo in the
     * messages Vector.  Each MessageInfo that matches the searchTerm
     * then has performFilter() run on it.
     * 
     * @return:  all messages removed from the current folder.
     */
    public Vector filterMessages(Vector messages) {
	Vector matches = new Vector();
	for (int i = 0; i < messages.size(); i++) {
	    if (searchTerm.match(((net.suberic.pooka.gui.MessageProxy)messages.elementAt(i)).getMessageInfo().getMessage()))
		matches.add(messages.elementAt(i));
	}

	return performFilter(matches);
    }

    /**
     * Actually performs the FilterAction on the given MessageInfo array.
     * 
     * @param filteredMessages A Vector of MessageInfo objects that are to
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
