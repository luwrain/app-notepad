/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.util.concurrent.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.speech.*;

final class Actions
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;
    final Conversations conv;

    Actions(Luwrain luwrain, Strings strings, Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
	this.conv = new Conversations(luwrain, strings);
    }

    boolean openAs()
    {
	conv.openAs();
	return true;
    }

    //Returns True if everything saved, false otherwise
    boolean onSave(EditArea area)
    {
	NullCheck.notNull(area, "area");
	if (!base.modified)
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (base.file == null)
	{
	    final File f = conv.save(base.file != null?base.file.file:null);
	    if (f == null)
		return false;
	    base.file = new FileParams(f);
	    luwrain.onAreaNewName(area);
	}
	try {
	    base.file.save(area.getLines());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return false;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MessageType.OK);
	return true;
    }

void onSaveAs(EditArea area)
    {
	NullCheck.notNull(area, "area");
	final File f = conv.save(base.file != null?base.file.file:null);
	if (f == null)
	    return;
	base.file = new FileParams(f);
	luwrain.onAreaNewName(area);
	try {
	    base.file.save(area.getLines());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MessageType.OK);
    }

    /*
    boolean onOpenEvent(Base base, String fileName, EditArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(fileName, "fileName");
	NullCheck.notNull(area, "area");
	if (fileName.isEmpty())
	    return false;
	if (base.modified || base.file != null)
	    return false;
final File f = new File(fileName);
if (f.isDirectory())
	    return false;
final FileParams fp = new FileParams(f);
	final String[] lines;
	try {
	    lines = fp.read();
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return true;
	}
	base.file = fp;
	area.setLines(lines);
	area.setAreaName(base.file.getName());
	base.modified = false;;
	return true;
    }
    */

    boolean startNarrating(ProgressArea destArea, String text)
    {
	NullCheck.notNull(destArea, "destArea");
	NullCheck.notNull(text, "text");
	if (base.narratingTask != null && !base.narratingTask.isDone())
	    return false;
	if (text.trim().isEmpty())
	{
	    luwrain.message(strings.noTextToSynth(), Luwrain.MessageType.ERROR);
	    return true;
	}
	final Channel channel;
	try {
channel = luwrain.loadSpeechChannel("", "");
	}
	catch(org.luwrain.speech.SpeechException e)
	{
	    luwrain.message(strings.errorLoadingSpeechChannel(e.getMessage()), Luwrain.MessageType.ERROR);
	    return true;
	}
	
	if (channel == null)
	{
	    luwrain.message(strings.noChannelToSynth(), Luwrain.MessageType.ERROR);
	    return true;
	}
	//	final File homeDir = luwrain.getFileProperty("luwrain.dir.userhome");
	final File res = Popups.path(luwrain, 
				     strings.targetDirPopupName(), strings.targetDirPopupPrefix(), luwrain.getFileProperty("luwrain.dir.userhome"),
				     (fileToCheck, announce)->{return true;});
	if (res == null)
	    return true;
	base.narrating = new Narrating(strings, text, res, 
				       new File(luwrain.getFileProperty("luwrain.dir.scripts"), "lwr-audio-compress").getAbsolutePath(), channel){
		@Override protected void progressLine(String text, boolean doneMessage)
		{
		    luwrain.runUiSafely(()->destArea.addProgressLine(text));
		    if (doneMessage)
			luwrain.runUiSafely(()->luwrain.message(text, Luwrain.MessageType.DONE));
		}
	    };
	base.narratingTask = new FutureTask(base.narrating, null);
	luwrain.executeBkg(base.narratingTask);
	return true;
    }
}
