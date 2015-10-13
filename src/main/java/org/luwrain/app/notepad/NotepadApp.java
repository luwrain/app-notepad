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
import java.io.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

class NotepadApp implements Application, Actions
{
static public final String STRINGS_NAME = "luwrain.notepad";
    static private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets(); 

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private EditArea area;
    private Document doc = null;

    private String arg = null;

    NotepadApp()
    {
	doc = null;
	arg = null;
    }

    public NotepadApp(String arg)
    {
	this.arg = arg;
	if (arg == null)
	    throw new NullPointerException("fileName may not be null"); 
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
	if (arg == null || arg.isEmpty())
	{
	    doc = new Document(new File(luwrain.launchContext().userHomeDirAsFile(), strings.newFileName()),
			       DEFAULT_CHARSET, true);
	    area.setName(doc.file.getName());
	    return;
	}
	final File f = new File(arg);
	doc = new Document(f.isAbsolute()?f:new File(luwrain.launchContext().userHomeDirAsFile(), f.getPath()),
			   DEFAULT_CHARSET, false);
	final String[] lines = base.read(doc.file.getAbsolutePath(), doc.charset);
	area.setName(doc.file.getName());
	if (lines != null)
	    area.setContent(lines); else
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
    }

    @Override public void removeBackslashR()
    {
	base.removeBackslashR(area);
	luwrain.onAreaNewContent(area);
	doc.modified = true;
    }

    @Override public void addBackslashR()
    {
	base.addBackslashR(area);
	luwrain.onAreaNewContent(area);
	doc.modified = true;
    }


    @Override public boolean anotherCharset()
    {
	if (doc == null)
	    return false;
	if (!checkIfUnsaved())
	    return true;
	if (doc.defaultDoc)
	    return false;
	final Charset charset = charsetPopup();
	if (charset == null)
	    return true;

	final YesNoPopup popup = new YesNoPopup(luwrain, strings.rereadAnotherCharsetPopupName(), strings.rereadAnotherCharsetPopupQuestion(), true);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	if (!popup.result())
	{
	    doc.modified = true;
	    doc.charset = charset;
	    return true;
	}
	final String[] lines = base.read(doc.file.getAbsolutePath(), charset);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	area.setContent(lines);
	doc.modified = false;
	doc.charset = charset;
	return true;
    }

    private Charset charsetPopup()
    {
	LinkedList<String> names = new LinkedList<String>();
	for(Map.Entry<String, Charset>  ent: AVAILABLE_CHARSETS.entrySet())
	    names.add(ent.getKey());
	EditListPopup popup = new EditListPopup(luwrain,
						new FixedListPopupModel(names.toArray(new String[names.size()])),
						strings.charsetPopupName(),
						strings.charsetPopupPrefix(),
						(doc != null && doc.charset != null)?doc.charset.displayName():"");
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

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public boolean save()
    {
	if (doc == null || doc.file == null)
	    return false;
	if (!doc.modified)
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (doc.defaultDoc)
	{
	    final String newFileName = askFileNameToSave();
	    if (newFileName == null || newFileName.isEmpty())
		return false;
	    final File f = new File(newFileName);
	    doc.file = f.isAbsolute()?f:new File(luwrain.launchContext().userHomeDirAsFile(), f.getPath());
	    doc.defaultDoc = false;
	}
	if (area.getContent() != null && base.save(doc.file.getAbsolutePath(), area.getContent(), doc.charset))
	{
	    doc.modified = false;
	    area.setName(doc.file.getName());
	    luwrain.message(strings.fileIsSaved(), Luwrain.MESSAGE_OK);
	    return true;
	}
	luwrain.message(strings.errorSavingFile(), Luwrain.MESSAGE_ERROR);
	return false;
    }

    @Override public void open()
    {
	if (doc == null || doc.file == null)
	    return;
	final File dir = doc.file.getParentFile();
	final File res = Popups.open(luwrain, dir, Popup.WEAK);
	if (res == null)
	    return;
	if (doc.modified || !doc.defaultDoc)
	{
	    luwrain.openFile(res.getAbsolutePath());
	    return;
	}
	final Document newDoc = new Document(res, DEFAULT_CHARSET, false);
	final String[] lines = base.read(res.getAbsolutePath(), newDoc.charset);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
	    return;
	}
	doc = newDoc;
	area.setContent(lines);
	    area.setName(res.getName());
    }

    @Override public void markAsModified()
    {
	if (doc == null)
	    return;
	doc.modified = true;
    }

    private void createArea()
    {
	final Actions a = this;
	area = new EditArea(new DefaultControlEnvironment(luwrain),"" ){
		private Actions actions = a;
		@Override public void onChange()
		{
		    actions.markAsModified();
		}
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (!event.isCommand() || event.isModified())
			return super.onKeyboardEvent(event);
		    switch(event.getCommand())
		    {
		    case KeyboardEvent.F7:
			actions.removeBackslashR();
			return true;
		    case KeyboardEvent.F8:
			actions.addBackslashR();
			return true;
		    case KeyboardEvent.F10:
			return actions.anotherCharset();
		    default:
			return super.onKeyboardEvent(event);
		    }
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    if (event == null)
			throw new NullPointerException("event may not be null");
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.close();
			return true;
		    case EnvironmentEvent.INTRODUCE:
			luwrain.playSound(Sounds.INTRO_REGULAR);
			luwrain.say(strings.introduction() + " " + getAreaName()); 
			return true;
		    case EnvironmentEvent.SAVE:
			actions.save();
			return true;
		    case EnvironmentEvent.OPEN:
			actions.open();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public void close()
    {
	if (!checkIfUnsaved())
	    return;
	doc.modified = false;
	luwrain.closeApp();
    }

    //Returns true if there are no more modification which the user would like to save;
    private boolean checkIfUnsaved()
    {
	if (doc == null)
	    return false;
	if (!doc.modified)
	    return true;
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), false);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return false;
	if ( popup.result() && !save())
	    return false;
	//	doc.modified = false;
	return true;
    }

    //null means user cancelled file name popup
    private String askFileNameToSave()
    {
	final File dir = luwrain.launchContext().userHomeDirAsFile();
	final File chosenFile = Popups.file(luwrain, strings.savePopupName(),
					    strings.savePopupPrefix(),
					    new File(dir, strings.newFileName()), FilePopup.ANY, Popup.WEAK);
	if (chosenFile == null)
	    return null;
	return chosenFile.getAbsolutePath();
    }
}
