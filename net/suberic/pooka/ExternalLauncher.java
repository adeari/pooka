package net.suberic.pooka;

import javax.activation.*;
import java.io.*;

public class ExternalLauncher implements CommandObject, Runnable {

    private String verb;
    private DataHandler dh;

    public ExternalLauncher() {
    };

    public void setCommandContext(java.lang.String newVerb,
				  DataHandler newDh)
	throws java.io.IOException {
	verb = newVerb;
	dh = newDh;
    }

    public void show() {
	Thread t = new Thread(this, "External Viewer");
	t.run();
    }

    public void run() {
	try {
	    File tmpFile = File.createTempFile("pooka", ".tmp");
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    dh.writeTo(fos);
	    
	    String wrapper = Pooka.getProperty("ExternalLauncher.cmdWrapper." + java.io.File.separator, null);
	    if (wrapper != null) 
		verb = insertWrapper(verb, wrapper);
	    
	    StringBuffer cmd = new StringBuffer(verb);		
		
	    String fileName = tmpFile.getAbsolutePath();
	    
	    int currentFile = verb.lastIndexOf("%s", verb.length());
	    while (currentFile != -1) {
		cmd.replace(currentFile, currentFile +2, fileName);
		currentFile = verb.lastIndexOf("%s", currentFile -1);
	    }
	    
	    System.out.println("running command " + cmd.toString());
	    Runtime.getRuntime().exec(cmd.toString());
	} catch (java.io.IOException ioe) {
	    System.out.println("Error opening temp file " + ioe.getMessage());
	}
    }
    
    public String insertWrapper(String origCmd, String cmdWrapper) {
	System.out.println("inserting wrapper " + cmdWrapper + " on " + origCmd);
	StringBuffer modifiedCmd = new StringBuffer(cmdWrapper);
	int currentCmd = cmdWrapper.lastIndexOf("%v", cmdWrapper.length());
	while (currentCmd != -1) {
	    modifiedCmd.replace(currentCmd, currentCmd +2, origCmd);
	    currentCmd = cmdWrapper.lastIndexOf("%v", currentCmd -1);
	}
	
	System.out.println("returning " + modifiedCmd);
	
	return modifiedCmd.toString();
    }
}


