package net.suberic.pooka;
import net.suberic.pooka.gui.*;
import java.awt.*;
import javax.swing.*;

public class Pooka {
    static public net.suberic.util.VariableBundle resources;
    static public String localrc;
    static public java.text.SimpleDateFormat dateFormatter;
    static public javax.activation.CommandMap mailcap;

    static public void main(String argv[]) {
	localrc = new String (System.getProperty("user.home") + System.getProperty("file.separator") + ".pookarc"); 

	try {
	    resources = new net.suberic.util.VariableBundle(new java.io.FileInputStream(localrc), new net.suberic.util.VariableBundle(new Object().getClass().getResourceAsStream("/net/suberic/pooka/Pookarc"), "net.suberic.pooka.Pooka"));
       	} catch (Exception e) {
	    resources = new net.suberic.util.VariableBundle(new Object().getClass().getResourceAsStream("/net/suberic/pooka/Pookarc"), "net.suberic.pooka.Pooka");
	}

	dateFormatter = new java.text.SimpleDateFormat(Pooka.getProperty("DateFormat", "EEE, MMM dd, yyyy, hh:mm"));

	UserProfile.createProfiles(resources);

	mailcap = new FullMailcapCommandMap();

	JFrame frame = new JFrame("Pooka");
	frame.setBackground(Color.lightGray);
	frame.getContentPane().setLayout(new BorderLayout());
	MainPanel panel = new MainPanel(frame);
	frame.getContentPane().add("North", panel.getMainToolbar());
	frame.getContentPane().add("Center", panel);
	frame.setJMenuBar(panel.getMainMenu());
	frame.pack();
	frame.setSize(Integer.parseInt(Pooka.getProperty("Pooka.hsize", "800")), Integer.parseInt(Pooka.getProperty("Pooka.vsize", "600")));
        frame.show();
	if (getProperty("Store", "").equals("")) {
	    NewAccountPooka nap = new NewAccountPooka(panel.getMessagePanel());
	    nap.start();
	}
    }

    static public String getProperty(String propName, String defVal) {
	return (resources.getProperty(propName, defVal));
    }

    static public String getProperty(String propName) {
	return (resources.getProperty(propName));
    }

    static public void setProperty(String propName, String propValue) {
	resources.setProperty(propName, propValue);
    }

    static public net.suberic.util.VariableBundle getResources() {
	return resources;
    }

    static public boolean isDebug() {
	if (resources.getProperty("Pooka.debug").equals("true"))
	    return true;
	else
	    return false;
    }

    static public java.text.SimpleDateFormat getDateFormatter() {
	return dateFormatter;
    }

    static public javax.activation.CommandMap getMailcap() {
	return mailcap;
    }
}



