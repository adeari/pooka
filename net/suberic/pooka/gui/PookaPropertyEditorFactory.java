package net.suberic.pooka.gui;

import net.suberic.util.gui.*;
import net.suberic.util.*;

public class PookaPropertyEditorFactory extends PropertyEditorFactory {
    /**
     * This creates a new PookaPropertyEditorFactory from the given
     * VariableBundle.
     */
    public PookaPropertyEditorFactory (VariableBundle bundle) {
	super(bundle);
    }


    /**
     * This returns a DefaultPropertyEditor for the property passed.
     * If there is a value set for property.propertyType, it will return
     * the proper editor for that property type.  If there is no such
     * property set, then this will return a BasicEditor.
     *
     * Overrides createEditor in PropertyEditorFactory.
     */
    public DefaultPropertyEditor createEditor(String property) {
	String test = getBundle().getProperty(property + ".propertyType", "");
	if (test.equals("Folder"))
	    return createFolderEditor(property);
	else
	    return super.createEditor(property);
    }

   /**
     * This returns a DefaultPropertyEditor for the property passed.
     * This method uses the typeTemplate parameter to determine what
     * type of property should be created.  Specifically, this method
     * looks for the property typeTemplate.propertyType, and, if it is
     * set, creates an editor appropriate for that type for the 
     * property.
     *
     * Overrides createEditor in PropertyEditorFactory.
     */
    public DefaultPropertyEditor createEditor(String property, String typeTemplate) {
	String test = getBundle().getProperty(typeTemplate + ".propertyType", "");
	if (test.equals("Folder"))
	    return createFolderEditor(property, typeTemplate);
	else
	    return super.createEditor(property, typeTemplate);
    }

    /**
     * This returns a new FolderEditor.
     */
    public DefaultPropertyEditor createFolderEditor(String property) {
	return new FolderSelectorPane(property, getBundle());
    }

    /**
     * This returns a new FolderEditor.
     */
    public DefaultPropertyEditor createFolderEditor(String property, String typeTemplate) {
	return new FolderSelectorPane(property, typeTemplate, getBundle());
    }
}
