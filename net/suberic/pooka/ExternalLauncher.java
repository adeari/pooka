package net.suberic.pooka;

import javax.activation.*;
import java.io.*;
import java.util.StringTokenizer;

/**
 * This class is a generic class which will allow an external program
 * to start up and access a file.
 */
public class ExternalLauncher implements CommandObject, Runnable {

    private String verb;
    private DataHandler dh;

    public ExternalLauncher() {
    };

    /**
     * This sets the CommandContext to the given command and DataHandler.
     * Note that for this implementation, the verb is expected to bet the
     * external command which is run, with %s representing the name of
     * the temporary file.
     *
     * As specified in javax.activation.CommandObject.
     */
    public void setCommandContext(java.lang.String newVerb,
				  DataHandler newDh)
	throws java.io.IOException {
	verb = newVerb;
	dh = newDh;
    }

    /**
     * This starts the run() method in a separate Thread.  It is implemented
     * this way so as to present the same interface as a CommandObject which
     * extends Window.
     */
    public void show() {
	Thread t = new Thread(this, "External Viewer");
	t.run();
    }

    /**
     * This is the main method for the ExternalLaucher.  It creates a 
     * temporary file from the DataHandler and then uses the verb command,
     * along with the wrappers specified by ExternalLauncher.fileHandler
     * and ExternalLauncher.cmdWrapper, to access the file itself.
     *
     * As specified by java.lang.Runnable.
     */
    public void run() {
	try {
	    File tmpFile = File.createTempFile("pooka", ".tmp");
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    dh.writeTo(fos);
	    
	    String fileHandler = Pooka.getProperty("ExternalLauncher.fileHandler." + java.io.File.separator, null);
	    String wrapper = Pooka.getProperty("ExternalLauncher.cmdWrapper." + java.io.File.separator, null);
	    String fileName = tmpFile.getAbsolutePath();

	    String parsedVerb;
	    String[] cmdArray;

	    if (fileHandler != null && wrapper != null) { 

		parsedVerb = substituteString(fileHandler, "%v", verb);
		parsedVerb = substituteString(parsedVerb, "%s", fileName);
		
		StringTokenizer tok = new StringTokenizer(wrapper);
		cmdArray = new String[tok.countTokens()];
		for (int i = 0; tok.hasMoreTokens(); i++) {
		    String currentString = tok.nextToken();
		    if (currentString.equals("%v"))
			cmdArray[i] = parsedVerb;
		    else
			cmdArray[i]=currentString;
		}
	    } else {
		parsedVerb = substituteString(verb, "%s", fileName);
		
		StringTokenizer tok = new StringTokenizer(wrapper);
		cmdArray = new String[tok.countTokens()];
		for (int i = 0; tok.hasMoreTokens(); i++) {
		    String currentString = tok.nextToken();
		    cmdArray[i]=tok.nextToken();
		}
		
		tmpFile.deleteOnExit();
	    }

	    Runtime.getRuntime().exec(cmdArray);

	} catch (java.io.IOException ioe) {
	    System.out.println("Error opening temp file " + ioe.getMessage());
	}
    }
    
    /**
     * This method subsitutes all occurances of key with value in String
     * original.
     */
    public String substituteString(String original, String key, String value) {
	// you know, i'm already doing this for the replyIntro; maybe i
	// should generalize these both a bit...

	StringBuffer modifiedString = new StringBuffer(original);
	int current = original.lastIndexOf(key, original.length());
	while (current != -1) {
	    modifiedString.replace(current, current + key.length(), value);
	    current = original.substring(0, current).lastIndexOf(key, current);
	}
	
	return modifiedString.toString();
    }
}


