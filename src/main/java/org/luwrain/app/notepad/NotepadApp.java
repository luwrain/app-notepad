/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
//import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

class NotepadApp implements Application, Actions
{
    static private final int NORMAL_LAYOUT_INDEX = 0;
    static private final int PROPERTIES_LAYOUT_INDEX = 1;

    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets(); 

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private EditArea editArea;
    private SimpleArea propertiesArea;
    private AreaLayoutSwitch layouts;

    private String arg = null;

    NotepadApp()
    {
	arg = null;
    }

    public NotepadApp(String arg)
    {
	NullCheck.notNull(arg, "arg");
	this.arg = arg;
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(editArea));
	layouts.add(new AreaLayout(propertiesArea));
	base.prepareDocument(arg, editArea);
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	editArea = new EditArea(new DefaultControlEnvironment(luwrain),"",
			    new String[0], ()->markAsModified()){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case SAVE:
			save();
			return true;
		    case PROPERTIES:
			return onShowProperties();
		    case OPEN:
			if (!(event instanceof OpenEvent))
			    return false;
			return base.open(((OpenEvent)event).path(), this);
		    case ACTION:
			return onEditAction(event);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return getEditAreaActions();
		}
	    };

	propertiesArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    return onCloseProperties();
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
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
    }

    private Action[] getEditAreaActions()
    {
		    return new Action[]{
			new Action("save", strings.actionTitle("save")),
			new Action("open-another-charset", strings.actionTitle("open-another-charset")),
			new Action("save-another-charset", strings.actionTitle("save-another-charset")),
			new Action("remove-backslash-r", strings.actionTitle("remove-backslash-r")),
			new Action("add-backslash-r", strings.actionTitle("add-backslash-r")),
			new Action("info", strings.actionTitle("info")),
		    };
    }

    private boolean onEditAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
			if (ActionEvent.isAction(event, "save"))
			    return save();
			if (ActionEvent.isAction(event, "open-another-charset"))
			    return openAnotherCharset();
			if (ActionEvent.isAction(event, "save-another-charset"))
			    return saveAnotherCharset();
			if (ActionEvent.isAction(event, "remove-backslash-r"))
			    return removeBackslashR();
			if (ActionEvent.isAction(event, "add-backslash-r"))
			    return addBackslashR();
			if (ActionEvent.isAction(event, "info"))
			    return info();
			return false;
    }

    private boolean onShowProperties()
    {
	base.fillProperties(propertiesArea, editArea);
	layouts.show(PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    private boolean onCloseProperties()
    {
	layouts.show(NORMAL_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }


    private void prepareDocument()
    {
	editArea.setName(strings.initialTitle());
	if (arg == null || arg.isEmpty())
	    return;
	base.path = Paths.get(arg);
	if (base.path == null)
	    return;
	final String[] lines = base.read(base.path, base.DEFAULT_CHARSET);
	editArea.setName(base.path.getFileName().toString());
	if (lines != null)
	    editArea.setLines(lines); else
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
    }

private boolean removeBackslashR()
    {
	base.removeBackslashR(editArea);
	luwrain.onAreaNewContent(editArea);
	base.modified = true;
	return true;
    }

    private boolean addBackslashR()
    {
	base.addBackslashR(editArea);
	luwrain.onAreaNewContent(editArea);
	base.modified = true;
	return true;
    }

    //Returns false if there are still unsaved changes
    private boolean save()
    {
	if (!base.modified)
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (base.path == null)
	{
	    base.path = savePopup();
	    if (base.path == null)
		return false;
	}
	if (!base.save(base.path.toString(), editArea.getLines(), base.DEFAULT_CHARSET))
	{
	    luwrain.message(strings.errorSavingFile(), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	base.modified = false;
	editArea.setName(base.path.getFileName().toString());
	luwrain.message(strings.fileIsSaved(), Luwrain.MESSAGE_OK);
	return true;
    }

    private boolean saveAnotherCharset()
    {
	final Charset charset = charsetPopup();
	if (charset == null)
	    return true;
	final Path p = savePopup();
	if (p == null)
	    return true;
	if (!base.save(p.toString(), editArea.getLines(), charset))
	{
	    luwrain.message(strings.errorSavingFile(), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MESSAGE_OK);
	return true;
    }

    private boolean openAnotherCharset()
    {
	if (!checkIfUnsaved())
	    return true;
	final Charset charset = charsetPopup();
	if (charset == null)
	    return true;
	final Path home = luwrain.getPathProperty("luwrain.dir.userhome");
	final Path p = null;//Popups.open(luwrain, path != null?path:home, home);
	if (p == null)
	    return true;
	final String[] lines = base.read(p, charset);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	base.path = p;
	editArea.setLines(lines);
	editArea.setName(base.path.getFileName().toString());
	return true;
    }

    private void markAsModified()
    {
	base.modified = true;
    }

    private boolean info()
    {
	return false;
    }

    //Returns true if there are no more modification which the user would like to save;
    private boolean checkIfUnsaved()
    {
	if (!base.modified)
	    return true;
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), false, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return false;
	if ( popup.result() && !save())
	    return false;
	return true;
    }

    //null means user cancelled file name popup
    private Path savePopup()
    {
	final Path homeDir = luwrain.getPathProperty("luwrain.dir.userhome");
return Popups.path(luwrain, 
strings.savePopupName(), strings.savePopupPrefix(),
			 base.path != null?base.path:homeDir, homeDir,
		   (path)->{return true;},
		   Popups.loadFilePopupFlags(luwrain), Popups.DEFAULT_POPUP_FLAGS);
    }

    private Charset charsetPopup()
    {
	final LinkedList<String> names = new LinkedList<String>();
	for(Map.Entry<String, Charset>  ent: AVAILABLE_CHARSETS.entrySet())
	    names.add(ent.getKey());
	final EditListPopup popup = new EditListPopup(luwrain,
						new EditListPopupUtils.FixedModel(names.toArray(new String[names.size()])),
						strings.charsetPopupName(), strings.charsetPopupPrefix(),
						      "", Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return null;
	final String text = popup.text().trim();
	if (text == null || text.isEmpty() || !AVAILABLE_CHARSETS.containsKey(text))
	{
	    luwrain.message(strings.invalidCharset(), Luwrain.MESSAGE_ERROR);
	    return null;
	}
	return AVAILABLE_CHARSETS.get(text);
    }

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }

    @Override public void closeApp()
    {
	if (!checkIfUnsaved())
	    return;
	base.modified = false;
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return base.path == null?strings.appName():base.path.getFileName().toString();
    }
}
