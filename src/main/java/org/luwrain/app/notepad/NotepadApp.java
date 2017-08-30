/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.notepad;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

class NotepadApp implements Application
{
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets(); 

    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private Conversations conversations = null;

    private EditArea editArea = null;
    private AreaLayoutHelper layout = null;

    private final String arg;

    NotepadApp()
    {
	arg = null;
    }

    public NotepadApp(String arg)
    {
	NullCheck.notNull(arg, "arg");
	this.arg = arg;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actions = new Actions(luwrain, strings);
	this.conversations = new Conversations(luwrain, strings);
	createAreas();
	this.layout = new AreaLayoutHelper(()->luwrain.onNewAreaLayout(), editArea);
	base.prepareDocument(arg, editArea);
	return new InitResult();
    }

    private void createAreas()
    {
	editArea = new EditArea(new DefaultControlEnvironment(luwrain),"",
				new String[0], ()->{base.modified = true;}){

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case SAVE:
			return actions.onSave(base, editArea);
		    case PROPERTIES:
			return showProps();
		    case OPEN:
			if (!(event instanceof OpenEvent))
			    return false;
			return actions.onOpenEvent(base, ((OpenEvent)event).path(), this);
		    case ACTION:
			return onEditAction(event);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getEditAreaActions();
		}
	    };

    }

    private boolean onEditAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	/*
			if (ActionEvent.isAction(event, "save"))
			    return save();
	*/
			if (ActionEvent.isAction(event, "remove-backslash-r"))
			    return actions.removeBackslashR(base, editArea);
			if (ActionEvent.isAction(event, "add-backslash-r"))
			    return actions.addBackslashR(base, editArea);
			return false;
    }

    private boolean showProps()
    {
	final SimpleArea propsArea = new SimpleArea(new DefaultControlEnvironment(luwrain), "Информация") {
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
	propsArea.beginLinesTrans();
	propsArea.addLine(strings.propertiesFileName() + " " + (base.file != null?base.file.file.getAbsolutePath():""));
	propsArea.addLine(strings.propertiesModified() + " " + (base.modified?strings.propertiesYes():strings.propertiesNo()));
	propsArea.addLine(strings.propertiesCurrentLine() + " " + (editArea.getHotPointY() + 1));
	propsArea.addLine(strings.propertiesLinesTotal() + " " + editArea.getLines().length);
	propsArea.addLine("");
	propsArea.endLinesTrans();
	layout.openTempArea(propsArea);
	return true;
    }

    //Returns true if there are no more modification which the user would like to save;
    private boolean checkIfUnsaved()
    {
	if (!base.modified)
	    return true;
	//Popups.confirmDefaultNo() isn't applicable here
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), true, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return false;
	if (! popup.result())
	    return true;
	return actions.save(base, editArea);
    }

    @Override public void closeApp()
    {
	if (!checkIfUnsaved())
	    return;
	luwrain.closeApp();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
    }

    @Override public String getAppName()
    {
	return base.file == null?strings.appName():base.file.getName();
    }
}
