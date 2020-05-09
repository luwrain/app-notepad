/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.script.*;
import org.luwrain.speech.*;
import org.luwrain.template.*;

final class App extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "notepad";
    static private final String NATURAL_MODE_CORRECTOR_HOOK = "luwrain.notepad.mode.natural";
    static private final String PROGRAMMING_MODE_CORRECTOR_HOOK = "luwrain.notepad.mode.programming";

    enum Mode {
	NONE,
	NATURAL,
	PROGRAMMING
    };

    File file = null;
    boolean modified = false;
    String charset = "UTF-8";
    String lineSeparator = System.lineSeparator();
    Mode mode = Mode.NONE;
    boolean speakIndent = false;
    FutureTask narratingTask = null; 
    Narrating narrating = null;
    final EditUtils.ActiveCorrector corrector;

    Settings sett = null;
    private final String arg;
    private Conversations conv = null;
    private Hooks hooks = null;
    private MainLayout mainLayout = null;
    private NarratingLayout narratingLayout = null;

    App()
    {
	this(null);
    }

    App(String arg)
    {
	super(Strings.NAME, Strings.class);
	this.arg = arg;
	this.corrector = new EditUtils.ActiveCorrector();
    }

    @Override public boolean onAppInit() throws IOException
    {
	this.sett = Settings.create(getLuwrain().getRegistry());
	this.conv = new Conversations(getLuwrain(), getStrings());
	this.hooks = new Hooks(this);
	this.mainLayout = new MainLayout(this);
	this.narratingLayout = new NarratingLayout(this);
	setAppName(getStrings().appName());
	if (arg != null && !arg.isEmpty())
	{
	    this.file = new File(arg);
	    if (this.file.exists() && !this.file.isDirectory())
		mainLayout.setText(read());
	    this.modified = false;
	    setAppName(file.getName());
	}
	return true;
    }

    //Returns True if everything saved, false otherwise
    boolean onSave()
    {
	if (!modified)
	{
	    getLuwrain().message(getStrings().noModificationsToSave());
	    return true;
	}
	if (file == null)
	{
	    final File f = conv.save(null);
	    if (f == null)
		return false;
	    this.file = f;
	    mainLayout.onAreaNewName();
	    setAppName(file.getName());
	}
	try {
	    save(mainLayout.getLines());
	}
	catch(IOException e)
	{
	    getLuwrain().crash(e);
	    return true;
	}
	this.modified = false;
	getLuwrain().message(getStrings().fileIsSaved(), Luwrain.MessageType.OK);
	return true;
    }

    //Returns true, if there are no modifications a user might want to save
    boolean everythingSaved()
    {
	if (!modified)
	    return true;
	switch(conv.unsavedChanges())
	{
	case CONTINUE_SAVE:
	    return onSave();
	case CONTINUE_UNSAVED:
	    return true;
	case CANCEL:
	    return false;
	}
	return false;
    }

    void activateMode(Mode mode)
    {
	NullCheck.notNull(mode, "mode");
	switch(mode)
	{
	case NATURAL:
	    corrector.setActivatedCorrector(new DirectScriptMultilineEditCorrector(new DefaultControlContext(getLuwrain()), corrector.getDefaultCorrector(), NATURAL_MODE_CORRECTOR_HOOK));
	    break;
	case PROGRAMMING:
	    corrector.setActivatedCorrector(new DirectScriptMultilineEditCorrector(new DefaultControlContext(getLuwrain()), corrector.getDefaultCorrector(), PROGRAMMING_MODE_CORRECTOR_HOOK));
	    break;
	}
    }

    boolean onNarrating(SimpleArea destArea, String[] text)
    {
	NullCheck.notNull(destArea, "destArea");
	NullCheck.notNullItems(text, "text");
	if (this.narratingTask != null && !this.narratingTask.isDone())
	{
	    //	    luwrain.setActiveArea(destArea);
	    return false;
	}
	final NarratingText narratingText = new NarratingText();
	narratingText.split(text);
	if (narratingText.sents.isEmpty())
	{
	    getLuwrain().message(getStrings().noTextToSynth(), Luwrain.MessageType.ERROR);
	    return false;
	}
	final File destDir = conv.narratingDestDir();
	if (destDir == null)
	    return false;
	final Channel channel;
	try {
	    channel = getLuwrain().loadSpeechChannel(sett.getNarratingChannelName(""), sett.getNarratingChannelParams(""));
	}
	catch(Exception e)
	{
	    getLuwrain().message(getStrings().errorLoadingSpeechChannel(e.getMessage()), Luwrain.MessageType.ERROR);
	    e.printStackTrace();
	    return false;
	}
	if (channel == null)
	{
	    getLuwrain().message(getStrings().noChannelToSynth(sett.getNarratingChannelName("")), Luwrain.MessageType.ERROR);
	    return false;
	}
	Log.debug(LOG_COMPONENT, "narrating channel loaded: " + channel.getChannelName());
	//	destArea.clear();
	//	destArea.addLine(base.strings.narratingProgress("0.0%"));
	destArea.addLine("");
	this.narrating = new Narrating(this,
				       narratingText.sents.toArray(new String[narratingText.sents.size()]),
				       destDir, 
				       new File(getLuwrain().getFileProperty("luwrain.dir.scripts"), "lwr-audio-compress").getAbsolutePath(), channel){
		@Override protected void writeMessage(String text)
		{
		    NullCheck.notNull(text, "text");
		    getLuwrain().runUiSafely(()->{
			    destArea.insertLine(destArea.getLineCount() - 2, text);
			});
		}
		@Override protected void progressUpdate(int sentsProcessed, int sentsTotal)
		{
		    final float value = ((float)sentsProcessed * 100) / sentsTotal;
		    getLuwrain().runUiSafely(()->{
			    //			    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingProgress(String.format("%.1f", value)) + "%");
			});
		}
		@Override protected void done()
		{
		    getLuwrain().runUiSafely(()->{
			    //					    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingDone());
			    getLuwrain().message(getStrings().narratingDone(), Luwrain.MessageType.DONE);
			});
		}
		@Override protected void cancelled()
		{
		    getLuwrain().runUiSafely(()->{
			    //					    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingCancelled());
			    getLuwrain().message(getStrings().narratingCancelled(), Luwrain.MessageType.DONE);
			});
		}
	    };
	this.narratingTask = new FutureTask(this.narrating, null);
	getLuwrain().executeBkg(this.narratingTask);
	return true;
    }

        String[] read() throws IOException
    {
	final String text = org.luwrain.util.FileUtils.readTextFileSingleString(file, charset);
	return org.luwrain.util.FileUtils.universalLineSplitting(text);
    }

    void save(String[] lines) throws IOException
    {
	NullCheck.notNullItems(lines, "lines");
	org.luwrain.util.FileUtils.writeTextFileMultipleStrings(file, lines, charset, lineSeparator);
    }

            @Override public boolean onInputEvent(Area area, KeyboardEvent event)
    {
	NullCheck.notNull(area, "area");
	if (super.onInputEvent(area, event))
	    return true;
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case ESCAPE:
		closeApp();
		return true;
	    }
	return false;
    }

        Conversations getConv()
    {
	return this.conv;
    }

    Hooks getHooks()
    {
	return this.hooks;
    }

    Settings getSett()
    {
	return this.sett;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

    @Override public void closeApp()
    {
	if (!everythingSaved())
	    return;
	super.closeApp();
    }

    @Override public void setAppName(String newName)
    {
	NullCheck.notEmpty(newName, "newName");
	super.setAppName(newName);
    }
}
