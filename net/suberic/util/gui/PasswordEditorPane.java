package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.FlowLayout;

public class PasswordEditorPane extends DefaultPropertyEditor {
    String property;
    String originalValue;
    String originalScrambledValue;
    JLabel label;
    JPasswordField inputField;
    VariableBundle sourceBundle;

    public PasswordEditorPane(String newProperty, VariableBundle bundle, boolean isEnabled) {
	configureEditor(newProperty, bundle, isEnabled);
    }

    public void configureEditor(PropertyEditorFactory factory, String newProperty, String templateType, VariableBundle bundle, boolean isEnabled) {
	property=newProperty;
	sourceBundle=bundle;
	originalScrambledValue = sourceBundle.getProperty(newProperty, "");
	if (!originalScrambledValue.equals(""))
	    originalValue = descrambleString(originalScrambledValue);
	else
	    originalValue = "";

	String defaultLabel;
	int dotIndex = property.lastIndexOf(".");
	if (dotIndex == -1) 
	    defaultLabel = new String(property);
	else
	    defaultLabel = property.substring(dotIndex+1);

	label = new JLabel(sourceBundle.getProperty(property + ".label", defaultLabel));
	inputField = new JPasswordField(originalValue);
	this.add(label);
	this.add(inputField);
	this.setEnabled(isEnabled);
    }

    public PasswordEditorPane(String newProperty, VariableBundle bundle) {
	this(newProperty, bundle, true);
    }

    public void setValue() {
	String value = new String(inputField.getPassword());
	if (isEnabled() && !(value.equals(originalValue)))
	    sourceBundle.setProperty(property, scrambleString(value));
    }

    public java.util.Properties getValue() {
	String value = new String(inputField.getPassword());
	java.util.Properties retProps = new java.util.Properties();
	if (value.equals(originalValue))
	    retProps.setProperty(property, originalScrambledValue);
	else
	    retProps.setProperty(property, scrambleString(value));
	return retProps;
    }

    public void resetDefaultValue() {
	inputField.setText(originalValue);
    }

    public void setEnabled(boolean newValue) {
	if (inputField != null) {
	    inputField.setEnabled(newValue);
	    enabled=newValue;
	}
    }

    private static char[] scrambleChars = new char[] {'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'G', 'g', 'H', 'h', 'I', 'i', 'J', 'j', 'K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'O', 'o', 'P', 'p', 'Q', 'q', 'R', 'r', 'S', 's', 'T', 't', 'U', 'u', 'V', 'v', 'W', 'w', 'X', 'x', 'Y', 'y', 'Z', 'z'};

    /**
     * This is a simple scrambler.
     */
    public static String scrambleString(String key) {
	int[] salt = new int[4];
	int keySize = key.length();
	long seed = System.currentTimeMillis();
	
	salt[0] = (int)((seed / 107) %  2704);
	salt[1] = (int)((seed / 19) % 2704);
	salt[2] = (int)((seed / 17) % 2704);
	salt[3] = (int)((seed / 91) % 2704);

	char [] scrambledString = new char[(keySize * 2) + 8];

	for (int i = 0; i < keySize; i++) {
	    int numValue = (int)(key.charAt(i));
	    numValue = (numValue + salt[i % 4]) % 2704;
	    scrambledString[i * 2] = scrambleChars[numValue / 52];
	    scrambledString[(i * 2) + 1] = scrambleChars[numValue % 52];
	}

	for (int i = 0; i  < 3; i++) {
	    int numValue = (salt[i] + salt[i + 1]) % 2704;
	    scrambledString[(keySize + i) * 2] = scrambleChars[numValue / 52];
	    scrambledString[((keySize + i) * 2) + 1] = scrambleChars[numValue % 52];
	}
	
	scrambledString[(keySize + 3) * 2] = scrambleChars[salt[3] / 52];
	scrambledString[((keySize + 3) * 2) + 1] = scrambleChars[salt[3] % 52];
	
	return new String(scrambledString);
    }

    /**
     * And this is a simple descrambler.
     */

    public static String descrambleString(String value) {
	int[] salt = new int[4];
	int scrambleSize = value.length();
	char[] key = new char[(scrambleSize - 8) / 2];
	salt[3] = (findCharValue(value.charAt(scrambleSize - 2)) * 52) + findCharValue(value.charAt(scrambleSize - 1));

	for (int i = 2; i >= 0; i--) {
	    salt[i] = (2704 - salt[i + 1] + (findCharValue(value.charAt(scrambleSize - ((4 - i) * 2) )) * 52) + findCharValue(value.charAt(scrambleSize - ((4 - i) * 2) + 1))) % 2704;
	}

	for (int i = 0; i < (scrambleSize - 8) / 2; i++) {
	    key[i] = (char)((2704 - salt[i % 4] + (findCharValue(value.charAt(i * 2)) * 52) + findCharValue(value.charAt((i * 2) + 1))) % 2704);
	}
		
	return new String(key);
    }

    /**
     * This very inefficiently finds a character value in the scrambleChars
     * array.
     */
    private static int findCharValue(char a) {
	for (int i = 0; i < scrambleChars.length; i++) 
	    if (a == scrambleChars[i])
		return i;

	return 0;
    }


}
