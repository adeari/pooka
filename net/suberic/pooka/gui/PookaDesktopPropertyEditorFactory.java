package net.suberic.pooka.gui;

import net.suberic.util.gui.*;
import net.suberic.util.*;

public class PookaDesktopPropertyEditorFactory extends DesktopPropertyEditorFactory {
    /**
     * This creates a new PookaDesktopPropertyEditorFactory from the given
     * VariableBundle.
     */
    public PookaDesktopPropertyEditorFactory (VariableBundle bundle) {
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
      //System.err.println("creating editor for property " + property);
	String test = getBundle().getProperty(property + ".propertyType", "");
	if (test.equalsIgnoreCase("Folder"))
	    return createFolderEditor(property);
	else if (test.equalsIgnoreCase("Filter"))
	    return createFilterEditor(property);
	else if (test.equalsIgnoreCase("SearchTerm"))
	  return createSearchEditor(property);
	else if (test.equalsIgnoreCase("AddressList"))
	  return createAddressEditor(property);
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
      //System.err.println("creating editor for property " + property + ", template " + typeTemplate);
	String test = getBundle().getProperty(typeTemplate + ".propertyType", "");
	if (test.equals("Folder"))
	    return createFolderEditor(property, typeTemplate);
	else if (test.equalsIgnoreCase("Filter"))
	    return createFilterEditor(property, typeTemplate);
	else if (test.equalsIgnoreCase("SearchTerm"))
	    return createSearchEditor(property, typeTemplate);
	else if (test.equalsIgnoreCase("AddressList"))
	  return createAddressEditor(property, typeTemplate);
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


    /**
     * This returns a new FilterEditor.
     */
    public DefaultPropertyEditor createFilterEditor(String property) {
	return new net.suberic.pooka.gui.filter.FilterEditorPane(property, property, getBundle());
    }

    /**
     * This returns a new FilterEditor.
     */
    public DefaultPropertyEditor createFilterEditor(String property, String typeTemplate) {
	return new net.suberic.pooka.gui.filter.FilterEditorPane(property, typeTemplate, getBundle());
    }

    /**
     * This returns a new SearchTermEditor.
     */
    public DefaultPropertyEditor createSearchEditor(String property, String typeTemplate) {
	return new net.suberic.pooka.gui.search.SearchEditorPane(property, typeTemplate, getBundle());
    }


    /**
     * This returns a new SearchTermEditor.
     */
    public DefaultPropertyEditor createSearchEditor(String property) {
	return new net.suberic.pooka.gui.search.SearchEditorPane(property, property, getBundle());
    }

    /**
     * This returns a new AddressBookEditorPane.
     */
    public DefaultPropertyEditor createAddressEditor(String property, String typeTemplate) {
	return new net.suberic.pooka.gui.AddressBookEditorPane(this, property, typeTemplate, getBundle());
    }

    /**
     * This returns a new AddressBookEditorPane.
     */
    public DefaultPropertyEditor createAddressEditor(String property) {
	return new net.suberic.pooka.gui.AddressBookEditorPane(this, property, property, getBundle());
    }

}
