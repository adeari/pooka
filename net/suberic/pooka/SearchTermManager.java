package net.suberic.pooka;
import javax.mail.search.*;
import javax.mail.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class generates SearchTerms from 
 */
public class SearchTermManager {

    HashMap labelToPropertyMap;
    Vector termLabels;
    HashMap labelToOperationMap;
    Vector operationLabels;

    Class stringTermClass;
    Class flagTermClass;
    
    String sourceProperty;

    public static String STRING_MATCH = "String";
    public static String BOOLEAN_MATCH = "Boolean";
    public static String DATE_MATCH = "Date";

    /**
     * Default constructor.  Initializes the labelToPropertyMap and the
     * termLabels Vector from the Pooka property.
     */
    public SearchTermManager(String propertyName) {
	sourceProperty = propertyName;
	try {
	    flagTermClass = Class.forName("javax.mail.search.FlagTerm");
	    stringTermClass = Class.forName("javax.mail.search.StringTerm");
	} catch (Exception e) { }
	createTermMaps(propertyName + ".searchTerms");
	createOperationMaps(propertyName + ".operations");
    }

    /**
     * Creates the labelToOperationMap and operationLabels from the given 
     * property, as well as the termLabels Vector.
     */
    private void createTermMaps(String propName) {
	Vector keys = Pooka.getResources().getPropertyAsVector(propName, "");
	termLabels = new Vector();
	if (keys != null) {
	    labelToPropertyMap = new HashMap();
	    for (int i = 0; i < keys.size(); i++) {
		String thisValue = propName + "." + (String) keys.elementAt(i);
		String thisLabel = Pooka.getProperty(thisValue + ".label", (String)keys.elementAt(i));
		labelToPropertyMap.put(thisLabel, thisValue);
		termLabels.add(thisLabel);
	    }
	} 
    }

    /**
     * Creates the labelToOperationMap and operationLabels from the given 
     * propery. 
     */
    private void createOperationMaps(String propName) {
	Vector keys = Pooka.getResources().getPropertyAsVector(propName, "");
	operationLabels = new Vector();
	if (keys != null) {
	    labelToOperationMap = new HashMap();
	    for (int i = 0; i < keys.size(); i++) {
		String thisValue = propName + "." + (String) keys.elementAt(i);
		String thisLabel = Pooka.getProperty(thisValue + ".label", (String)keys.elementAt(i));
		labelToOperationMap.put(thisLabel, thisValue);
		operationLabels.add(thisLabel);
	    }
	} 
    }

    /**
     * Generates a compound SearchTerm.
     */
    public SearchTerm generateCompoundSearchTerm(String[] properties, String operation) {
	SearchTerm[] terms = new SearchTerm[properties.length];
	for (int i = 0; i < properties.length; i++)
	    terms[i] = generateSearchTermFromProperty(properties[i]);

	if (operation.equalsIgnoreCase("and"))
	    return new AndTerm(terms);
	else if (operation.equalsIgnoreCase("or"))
	    return new OrTerm(terms);
	else
	    return null;
    }

    /**
     * Generates a SearchTerm from a single property root.  This method 
     * expects the following sub-properties to be set on the given
     * property:
     *
     * property.type should be set either to 'compound' or 'single'
     * 
     * for 'single' types:
     * property.searchTerm
     * property.operationProperty (optional)
     * property.pattern (optional)
     *
     * for 'compound' types:
     * property.subTerms
     * property.operation (should be 'or' or 'and')
     */
    public SearchTerm generateSearchTermFromProperty(String property) {
	String type = Pooka.getProperty(property + ".type", "single");
	if (type.equalsIgnoreCase("single")) {
	    String searchProperty = Pooka.getProperty(property + ".searchTerm", "");
	    String operationProperty = Pooka.getProperty(property + ".operationProperty", "");
	    String pattern = Pooka.getProperty(property + ".pattern", "");
	    return generateSearchTerm(searchProperty, operationProperty, pattern);
	} else if (type.equalsIgnoreCase("compound")) {
	    Vector subTermList = Pooka.getResources().getPropertyAsVector(property + ".subTerms", "");
	    String[] subTerms = new String[subTermList.size()];
	    for (int i = 0; i < subTerms.length; i++) 
		subTerms[i] = (String) subTermList.elementAt(i);
	    String operation = Pooka.getProperty(property + ".operation", "");
	    
	    return generateCompoundSearchTerm(subTerms, operation);
	} else
	    return null;
    }

    /**
     * Generates a SearchTerm from the given property and pattern.
     *
     * This method used the .class subproperty of the given searchProperty
     * String to determine what type of SearchTerm to create.  If the
     * .class is an instance of FlagTerm, the .flag subproperty is used
     * to determine which flag to test.  If the .class is an instance
     * of StringTerm, then .ignoreCase is checked to see whether or not
     * to ignore case (default to false).
     *
     * This also uses the operationProperty to determine whether to make
     * this a positive or negative search (is or is not), or, in the case
     * of comparison searches, a greater than or less than search.
     *
     */
    public SearchTerm generateSearchTerm(String searchProperty, String operationProperty, String pattern) {
	SearchTerm term = null;
	try {
	    String className = Pooka.getProperty(searchProperty + ".class", "");
	    Class stClass = Class.forName(className);
	    if (stringTermClass.isAssignableFrom(stClass)) {

		boolean ignoreCase = Pooka.getProperty(searchProperty + ".ignoreCase", "false").equals("true");
		
		// check for the special cases.
		if (className.equals("javax.mail.search.RecipientStringTerm")) {

		    String recipientType = Pooka.getProperty(searchProperty + ".recipientType", "to");
		    if (recipientType.equalsIgnoreCase("to"))
			term = new RecipientStringTerm(javax.mail.Message.RecipientType.TO, pattern);
		    else if (recipientType.equalsIgnoreCase("cc"))
			term = new RecipientStringTerm(javax.mail.Message.RecipientType.CC, pattern);
		    else if (recipientType.equalsIgnoreCase("toorcc"))
			term = new OrTerm(new RecipientStringTerm(javax.mail.Message.RecipientType.CC, pattern), new RecipientStringTerm(javax.mail.Message.RecipientType.TO, pattern));


		    if (Pooka.getProperty(operationProperty, "").equalsIgnoreCase("not")) 
			term = new NotTerm(term);
			
		} else if (className.equals("javax.mail.search.HeaderTerm")) {

		    term = new HeaderTerm(Pooka.getProperty(searchProperty + ".header", ""), pattern);
		    if (Pooka.getProperty(operationProperty, "").equalsIgnoreCase("not")) 
			term = new NotTerm(term);


		} else {

		// default case for StringTerms

		    java.lang.reflect.Constructor termConst = stClass.getConstructor(new Class[] {Class.forName("java.lang.String")});
		    term = (SearchTerm) termConst.newInstance(new Object[] { pattern});

		    if (Pooka.getProperty(operationProperty, "").equalsIgnoreCase("not")) 
			term = new NotTerm(term);

		}
	    } else if (flagTermClass.isAssignableFrom(stClass)) {
		
		term = new FlagTerm(getFlags(Pooka.getProperty(searchProperty + ".flag", "")), Pooka.getProperty(searchProperty + ".value", "true").equalsIgnoreCase("true"));
		if (Pooka.getProperty(operationProperty, "").equalsIgnoreCase("not")) 
		    term = new NotTerm(term);
		
	    } else {
		
		// default case for any term.
		term = (SearchTerm) stClass.newInstance();
		if (Pooka.getProperty(operationProperty, "").equalsIgnoreCase("not")) 
		    term = new NotTerm(term);
				    
	    }
	} catch (Exception e) {
	    System.out.println("caught Exception generating SearchTerm: " + e);
	    e.printStackTrace();
	    return null;
	}
	
	return term;
    }

    public Flags getFlags(String flagName) {
	if (flagName.equalsIgnoreCase("answered"))
	    return new Flags(Flags.Flag.ANSWERED);
	else if (flagName.equalsIgnoreCase("deleted"))
	    return new Flags(Flags.Flag.DELETED);
	else if (flagName.equalsIgnoreCase("draft"))
	    return new Flags(Flags.Flag.DRAFT);
	else if (flagName.equalsIgnoreCase("flagged"))
	    return new Flags(Flags.Flag.FLAGGED);
	else if (flagName.equalsIgnoreCase("recent"))
	    return new Flags(Flags.Flag.RECENT);
	else if (flagName.equalsIgnoreCase("seen"))
	    return new Flags(Flags.Flag.SEEN);

	return new Flags(flagName);
    }

    // accessor methods

    public HashMap getLabelToPropertyMap() { 
	return labelToPropertyMap;
    }

    public Vector getTermLabels() {
	return termLabels;
    }

    public HashMap getLabelToOperationMap() {
	return labelToOperationMap;
    }

    public Vector getOperationLabels() {
	return operationLabels;
    }
}

