/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

    Actions(Base base)
    {
	NullCheck.notNull(base, "base");
	this.luwrain = base.luwrain;
	this.strings = base.strings;
	this.base = base;
	this.conv = new Conversations(luwrain, strings);
    }

    boolean onOpenAs()
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
	    final File f = conv.save(base.file );
	    if (f == null)
		return false;
	    base.file = f;
	    luwrain.onAreaNewName(area);
	}
	try {
	    base.save(area.getLines());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return true;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MessageType.OK);
	return true;
    }

void onSaveAs(EditArea area)
    {
	NullCheck.notNull(area, "area");
	final File f = conv.save(base.file);
	if (f == null)
	    return;
	base.file = f;
	luwrain.onAreaNewName(area);
	try {
	    base.save(area.getLines());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MessageType.OK);
    }

    boolean onCharset()
    {
final String res = conv.charset();
if (res == null)
    return true;
base.charset = res;
	return true;
    }

    boolean onNarrating(SimpleArea destArea, String[] text)
    {
	NullCheck.notNull(destArea, "destArea");
	NullCheck.notNullItems(text, "text");
		if (base.narratingTask != null && !base.narratingTask.isDone())
	    return false;

	final File destDir = conv.narratingDestDir();
	if (destDir == null)
	    return true;
	/*
	if (text.trim().isEmpty())
	{
	    luwrain.message(strings.noTextToSynth(), Luwrain.MessageType.ERROR);
	    return true;
	}
	*/
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
	base.narrating = new Narrating(strings, text, destDir, 
				       new File(luwrain.getFileProperty("luwrain.dir.scripts"), "lwr-audio-compress").getAbsolutePath(), channel){
		@Override protected void progressLine(String text, boolean doneMessage)
		{
		    luwrain.runUiSafely(()->{
			    //FIXME:
			    //			    destArea.addProgressLine(text)
			    			    });
		    if (doneMessage)
			luwrain.runUiSafely(()->luwrain.message(text, Luwrain.MessageType.DONE));
		}
	    };
	base.narratingTask = new FutureTask(base.narrating, null);
	luwrain.executeBkg(base.narratingTask);
	return true;
    }
}
