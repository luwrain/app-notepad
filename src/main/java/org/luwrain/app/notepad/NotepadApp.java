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
static private final String STRINGS_NAME = "luwrain.notepad";
    static private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets(); 

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private EditArea area;
    //    private Document doc = null;
    private boolean modified = false;
    private Path path = null;

    private String arg = null;

    NotepadApp()
    {
    }

    public NotepadApp(String arg)
    {
	this.arg = arg;
	NullCheck.notNull(arg, "arg");
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	createArea();
	prepareDocument();
	return true;
    }

    private void prepareDocument()
    {
	area.setName(strings.initialTitle());
	if (arg == null || arg.isEmpty())
	    return;
	path = Paths.get(arg);
	if (path == null)
	    return;
	final String[] lines = base.read(path.toString(), DEFAULT_CHARSET);
	area.setName(path.getFileName().toString());
	if (lines != null)
	    area.setLines(lines); else
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
    }

    @Override public void removeBackslashR()
    {
	base.removeBackslashR(area);
	luwrain.onAreaNewContent(area);
	modified = true;
    }

    @Override public void addBackslashR()
    {
	base.addBackslashR(area);
	luwrain.onAreaNewContent(area);
	modified = true;
    }

    //Returns false if there are still unsaved changes
    @Override public boolean save()
    {
	if (!modified)
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (path == null)
	{
	    path = savePopup();
	    if (path == null)
		return false;
	}
	if (!base.save(path.toString(), area.getLines(), DEFAULT_CHARSET))
	{
	    luwrain.message(strings.errorSavingFile(), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	modified = false;
	area.setName(path.getFileName().toString());
	luwrain.message(strings.fileIsSaved(), Luwrain.MESSAGE_OK);
	return true;
    }

    @Override public void saveAnotherCharset()
    {
	final Charset charset = charsetPopup();
	if (charset == null)
	    return;
	final Path p = savePopup();
	if (p == null)
	    return;
	if (!base.save(p.toString(), area.getLines(), charset))
	{
	    luwrain.message(strings.errorSavingFile(), Luwrain.MESSAGE_ERROR);
	    return;
	}
	modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MESSAGE_OK);
    }

    @Override public boolean open(String fileName)
    {
	if (modified || path != null)
	    return false;
	final String[] lines = base.read(fileName, DEFAULT_CHARSET);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	area.setLines(lines);
	area.setName(path.getFileName().toString());
	return true;
    }

    @Override public void openAnotherCharset()
    {
	if (!checkIfUnsaved())
	    return;
	final Charset charset = charsetPopup();
	if (charset == null)
	    return;
	final Path home = luwrain.launchContext().userHomeDirAsPath();
	final Path p = Popups.open(luwrain, path != null?path:home, home);
	if (p == null)
	    return;
	final String[] lines = base.read(p.toString(), charset);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
	    return;
	}
	path = p;
	area.setLines(lines);
	area.setName(path.getFileName().toString());
    }

    @Override public void markAsModified()
    {
	modified = true;
    }

    private void createArea()
    {
	final Actions actions = this;
	area = new EditArea(new DefaultControlEnvironment(luwrain),"",
			    new String[0], ()->actions.markAsModified()){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
		    switch(event.getSpecial())
		    {
		    case F7:
			actions.removeBackslashR();
			return true;
		    case F8:
			actions.addBackslashR();
			return true;
			/*
		    case KeyboardEvent.F10:
			return actions.anotherCharset();
			*/
		    }
			return super.onKeyboardEvent(event);
		    		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			actions.closeApp();
			return true;
			/*
		    case EnvironmentEvent.INTRODUCE:
			luwrain.silence();
			luwrain.playSound(Sounds.INTRO_REGULAR);
			luwrain.say(strings.introduction() + " " + getAreaName()); 
			return true;
			*/
		    case SAVE:
			actions.save();
			return true;
		    case OPEN:
			if (!(event instanceof OpenEvent))
			    return false;
			return actions.open(((OpenEvent)event).path());
		    case ACTION:
			if (ActionEvent.isAction(event, "save"))
			{
			    actions.save();
			    return true;
			}
			if (ActionEvent.isAction(event, "open-another-charset"))
			{
			    actions.openAnotherCharset();
			    return true;
			}
			if (ActionEvent.isAction(event, "save-another-charset"))
			{
			    actions.saveAnotherCharset();
			    return true;
			}
			if (ActionEvent.isAction(event, "remove-backslash-r"))
			{
			    actions.removeBackslashR();
			    return true;
			}
			if (ActionEvent.isAction(event, "add-backslash-r"))
			{
			    actions.addBackslashR();
			    return true;
			}
			return false;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[]{
			new Action("save", strings.actionTitle("save")),
			new Action("open-another-charset", strings.actionTitle("open-another-charset")),
			new Action("save-another-charset", strings.actionTitle("save-another-charset")),
			new Action("remove-backslash-r", strings.actionTitle("remove-backslash-r")),
			new Action("add-backslash-r", strings.actionTitle("add-backslash-r")),
		    };
		}
	    };
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public void closeApp()
    {
	if (!checkIfUnsaved())
	    return;
	modified = false;
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    //Returns true if there are no more modification which the user would like to save;
    private boolean checkIfUnsaved()
    {
	if (!modified)
	    return true;
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), false);
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
	final Path dir = luwrain.launchContext().userHomeDirAsPath();
return Popups.chooseFile(luwrain, 
strings.savePopupName(), strings.savePopupPrefix(),
			 path != null?path:dir, dir,
			 DefaultFileAcceptance.Type.ANY);
    }

    private Charset charsetPopup()
    {
	final LinkedList<String> names = new LinkedList<String>();
	for(Map.Entry<String, Charset>  ent: AVAILABLE_CHARSETS.entrySet())
	    names.add(ent.getKey());
	final EditListPopup popup = new EditListPopup(luwrain,
						new FixedEditListPopupModel(names.toArray(new String[names.size()])),
						strings.charsetPopupName(), strings.charsetPopupPrefix(),
						      "");
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
}
