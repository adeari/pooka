package net.suberic.pooka;

import javax.activation.*;
import java.util.*;
import java.io.*;

/**
 * FullMailcapCommandMap accepts both x-java-* type mailcap entries
 * as well as standard, external-to-java entries.
 */

public class FullMailcapCommandMap extends MailcapCommandMap {
    private Vector mailcapMaps;
    private static String externalLauncher = "net.suberic.pooka.ExternalLauncher";
    private File sourceFile = null;;
    
    public FullMailcapCommandMap() {
	/**
	 * this adds the following files/resources, in order:
	 * The file or resource named META-INF/mailcap.default 
	 * (usually found only in the activation.jar file). 
	 *
	 * The file or resource named META-INF/mailcap. 
	 * The file <java.home>/lib/mailcap. 
	 * The file .mailcap (or mailcap.txt) in the user's home directory. 
	 */

	mailcapMaps = new Vector();

	InputStream is = new Object().getClass().getResourceAsStream("/META-INF/mailcap.default");
	if (is != null)
	    addMailcapFile(is);

	is = new Object().getClass().getResourceAsStream("/META-INF/mailcap");
	if (is != null)
	    addMailcapFile(is);

	try {
	    addMailcapFile(System.getProperty("java.home") + System.getProperty("file.separator") + "lib" + System.getProperty("file.separator") + "mailcap");
	} catch (IOException ioe) {
	    // if it doesn't exist, it's ok.
	}
	
	try {
	    if (System.getProperty("file.separator").equals("\\")) {
		addMailcapFile(System.getProperty("user.home") + "\\mailcap.txt");
		sourceFile = new File(System.getProperty("user.home") + "\\mailcap.txt");
	    } else {
		addMailcapFile(System.getProperty("user.home") + System.getProperty("file.separator") + ".mailcap");
		sourceFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".mailcap");
	    }
	}  catch (IOException ioe) {
	    // if it doesn't exist, it's ok.
	}
    }

    public FullMailcapCommandMap(String mailcapFilename) throws java.io.IOException {
	this();
	addMailcapFile(mailcapFilename);
    }

    public FullMailcapCommandMap(InputStream is) {
	this();
	addMailcapFile(is);
    }

    public CommandInfo[] getAllCommands(java.lang.String mimeType) {

    /*
     * this just goes through the list of mailcap objects stored
     * and calls getAllCommands() on each. 
     */

	Vector foundCommands = new Vector();
	    for (int i = mailcapMaps.size() - 1 ; i >= 0; i--) {
		MailcapMap mc = (MailcapMap)mailcapMaps.elementAt(i);
		CommandInfo[] cis = mc.getAllCommands(mimeType);
		if (cis != null)
		    for (int j = 0; j < cis.length; j++)
			foundCommands.add(cis[j]);
	    }

	
	if (foundCommands.size() == 0)
	    return null;
	
	CommandInfo[] returnValue = new CommandInfo[foundCommands.size()];
	for (int k = 0; k < foundCommands.size(); k++)
	    returnValue[k] = (CommandInfo)foundCommands.elementAt(k);

	return returnValue;
	    
    }

    public CommandInfo getCommand(java.lang.String mimeType, java.lang.String cmdName) {

	for (int i = mailcapMaps.size(); i >= 0; i--) {
	    MailcapMap mc = (MailcapMap)mailcapMaps.elementAt(i);
	    CommandInfo cis = mc.getSpecificCommand(mimeType, cmdName);
	    if (cis != null)
		return cis;

	    cis = mc.getGenericCommand(mimeType, cmdName);
	    if (cis != null)
		return cis;
	}
	
	return null;
    }

    public CommandInfo[] getPreferredCommands(java.lang.String mimeType) {
	return getAllCommands(mimeType);
    }

    public void addMailcap(java.lang.String mail_cap) {
	MailcapMap mc = (MailcapMap)mailcapMaps.lastElement();
	mc.addMailcapEntry(mail_cap);
	if (sourceFile != null)
	    writeEntryToSourceFile(mail_cap);
    }

    /**
     * This writes the entry to the mailcap file.  Note that it actually
     * ends up overwriting _all_ entries for that particular mime type.
     */
    private void writeEntryToSourceFile(String mail_cap) {
	int semicolonIndex = mail_cap.indexOf(';');
	if (semicolonIndex > -1) {
	    String mimeType = mail_cap.substring(0, semicolonIndex +1);
	    try {
		if (!sourceFile.exists())
		    sourceFile.createNewFile();

		File outputFile  = sourceFile.createTempFile(sourceFile.getName(), ".tmp", sourceFile.getParentFile());

		BufferedReader readSourceFile = new BufferedReader(new FileReader(sourceFile));
		BufferedWriter writeSourceFile = new BufferedWriter(new FileWriter(outputFile));
		currentLine = readSourceFile.readLine();
		while (currentLine != null) {
		    equalsLoc = currentLine.indexOf(';');
		    if (equalsLoc != -1) {
			String key = currentLine.substring(0, equalsLoc);
			if (!mimeType.equals(key)) {
			    writeSourceFile.write(currentLine);
			    writeSourceFile.newLine();
			}
		    }
		}

		writeSourceFile.write(mail_cap);
		writeSourceFile.newLine();

		readSourceFile.close();
		writeSourceFile.flush();
		writeSourceFile.close();

		// if you don't delete the .old file first, then the
                // rename fails under Windows.
                String oldSourceName = sourceFile.getAbsolutePath() + ".old";
                File oldSource = new File (oldSourceName);
                if (oldSource.exists())
                  oldSource.delete();

		String fileName = new String(sourceFile.getAbsolutePath());
		sourceFile.renameTo(oldSource);
		outputFile.renameTo(new File(fileName));
		oldSource.delete();
	    } catch (Exception e) {
	    }
	}
    }

    public void addMailcapFile(java.lang.String mailcapFileName) throws IOException {
	FileInputStream fis = new FileInputStream(mailcapFileName);
	addMailcapFile(fis);
    }

    public void addMailcapFile(InputStream is) {
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader reader = new BufferedReader(isr);
	
	MailcapMap map = new MailcapMap();

	try {
	    String nextLine = reader.readLine();
	    while (nextLine != null) {
		if (!(nextLine.startsWith("#")))
		    map.addMailcapEntry(nextLine);
		nextLine=reader.readLine();
	    }
	   
	    mailcapMaps.add(map);
	} catch (Exception e) {
	    System.out.println("an error happened." + e.getMessage());
	    e.printStackTrace();
	    
	} finally {
	    try {
		reader.close();
	    } catch (IOException ioe) {
	    }
	    try {
		isr.close();
	    } catch (IOException ioe) {
	    }
	}

    }

    public static String getExternalLauncher() {
	return externalLauncher;
    }

    public static void setExternalLauncher(String newVal) {
	externalLauncher = newVal;
    }

    private class MailcapMap {
	// these are Hashtables of Vectors.  the Vectors should all be
	// CommandInfos.

	private Hashtable specificMap;
	private Hashtable genericMap;

	MailcapMap() {
	    specificMap=new Hashtable();
	    genericMap=new Hashtable();
	}

	CommandInfo[] getAllCommands(String mimeType) {
	    Vector foundCommands = new Vector();
	    Vector v = (Vector)specificMap.get(mimeType);

	    if (v != null) {
		for (int i = 0; i < v.size(); i++) 
		    foundCommands.add(v.elementAt(i));
	    }
	    
	    v = (Vector)genericMap.get(getGenericMimeType(mimeType));

	    if (v != null) 
		for (int j = 0; j < v.size(); j++) 
		    foundCommands.add(v.elementAt(j));

	    if (foundCommands.size() == 0)
		return null;
	    
	    CommandInfo[] commandsAsArray = new CommandInfo[foundCommands.size()];

	    for (int i = 0; i < foundCommands.size(); i++) 
		commandsAsArray[i] = (CommandInfo)foundCommands.elementAt(i);
	    
	    return commandsAsArray;
	}

	CommandInfo getSpecificCommand(String mimeType, String verb) {
	    return getCommandFromVector((Vector)specificMap.get(mimeType), verb);
	}

	CommandInfo getGenericCommand(String mimeType, String verb) {
	    return getCommandFromVector((Vector)genericMap.get(getGenericMimeType(mimeType)), verb);
	}
	
	CommandInfo getCommandFromVector(Vector v, String verb) {
	    if (v == null)
		return null;
	    
	    CommandInfo ci;
	    
	    for (int i = 0; i < v.size(); i++) {
		ci = (CommandInfo)v.elementAt(i);
		if (matches(ci, verb))
		    return ci;
	    }
	    
	    return null;
	}
	
	synchronized void addMailcapEntry(String mailcapLine) {
	    String mimeType;
	    String command;
	    String verb;

	    StringTokenizer tokens = new StringTokenizer(mailcapLine, ";");
	    if (tokens.hasMoreTokens()) {
		mimeType = stripWhiteSpace(new StringBuffer(tokens.nextToken()));
		Hashtable addTable;
		int i = mimeType.indexOf('/');
		if (( i != -1 ) && (i != mimeType.length()-1) && (mimeType.charAt(i+1) == '*')) {
		   addTable = genericMap;
		   mimeType = mimeType.substring(0, i-1);
		} else {
		    addTable = specificMap;
		}

		while (tokens.hasMoreTokens()) {
		    String[] verbClass = parseCommand(stripWhiteSpace(new StringBuffer(tokens.nextToken())));
		    if (verbClass != null) {
			Vector v = (Vector)addTable.get(mimeType);
			if (v != null) 
			    v.add(new CommandInfo(verbClass[0], verbClass[1]));
			else {
			    v = new Vector();
			    v.add(new CommandInfo(verbClass[0], verbClass[1]));
			    addTable.put(mimeType, v);
			}
		    }   
		} // while
	    } // if tokens.hasMoreTokens()
	}

	String getGenericMimeType(String original) {
	    int slash = original.indexOf('/');
	    if (slash == -1)
		return original;
	    else
		return original.substring(0, slash);
	}

	boolean matches (CommandInfo ci, String verb) {
	    return (ci.getCommandName().equals(verb));
	}
	
	String stripWhiteSpace(StringBuffer sb) {
	    int i = 0;
	    if (sb.charAt(i) == ' ' || sb.charAt(i) == '\t') {
		while (i < sb.length() && (sb.charAt(i) == ' ' || sb.charAt(i) == '\t'))
		    i++;
		sb.delete(0, i);
	    }

	    i = sb.length();
	    if (sb.charAt(i-1) == ' ' || sb.charAt(i-1) == '\t') {
		while (sb.charAt(i-1) == ' ' || sb.charAt(i-1) == '\t')
		    i--;
		sb.delete(i, sb.length());
	    }
	    return sb.toString();
	}

	/**
	 * returns a 2 dimensional String, where 0 = the command
	 * verb, and 1 = the classname.
	 *
	 * if no x-java- is found, the whole string is taken to 
	 * be the verb, and the class defaults to 
	 * FullMailcapCommandMap.externalLauncher.
	 */

	String[] parseCommand(String command) {
	    if (command == null || command.length() < 1)
		return null;
	    if (! command.startsWith("x-java-"))
		return new String[] {command, FullMailcapCommandMap.getExternalLauncher()};
	    else {
		int equalsIndex = command.indexOf('=');
		if (equalsIndex < 8 || equalsIndex == command.length()-1)
		    return null;
		else return new String[] {command.substring(7,equalsIndex), command.substring(equalsIndex+1)};
	    } 
	}
	
	
    }

}
	




