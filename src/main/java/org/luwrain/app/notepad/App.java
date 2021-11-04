/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.speech.*;
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "notepad";
    static private final String DEFAULT_CHARSET = "UTF-8";
    static private final String NATURAL_MODE_CORRECTOR_HOOK = "luwrain.notepad.mode.natural";
    static private final String PROGRAMMING_MODE_CORRECTOR_HOOK = "luwrain.notepad.mode.programming";

    enum Mode { NONE, NATURAL, PROGRAMMING };

    File file = null;
    boolean modified = false;
    String charset = DEFAULT_CHARSET;
    String lineSeparator = System.lineSeparator();
    Mode mode = Mode.NONE;
    final EditUtils.ActiveCorrector corrector;
    boolean speakIndent = false;

    private FutureTask narratingTask = null; 
    private Narrating narrating = null;
    Settings sett = null;
    private final String arg;
    private Conversations conv = null;
    private Hooks hooks = null;
    private MainLayout mainLayout = null;
    private NarratingLayout narratingLayout = null;

    public App()
    {
	this(null);
    }

    public App(String arg)
    {
	super(Strings.NAME, Strings.class, "luwrain.notepad");
	this.arg = arg;
	this.corrector = new EditUtils.ActiveCorrector();
    }

    @Override protected AreaLayout onAppInit() throws IOException
    {
	this.sett = Settings.create(getLuwrain().getRegistry());
	this.conv = new Conversations(getLuwrain(), getStrings());
	this.hooks = new Hooks(this);
	this.mainLayout = new MainLayout(this);
	this.narratingLayout = new NarratingLayout(this, ()->{});
	setAppName(getStrings().appName());
	if (arg != null && !arg.isEmpty())
	{
	    this.file = new File(arg);
	    if (this.file.exists() && !this.file.isDirectory())
		mainLayout.setText(read());
	    this.modified = false;
	    setAppName(file.getName());
	}
	return mainLayout.getAreaLayout();
    }

    void openLayout(AreaLayout layout)
    {
	NullCheck.notNull(layout, "layout");
	getLayout().setBasicLayout(layout);
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
	    mainLayout.onNewFile();
	    setAppName(file.getName());
	}
	try {
	    save(mainLayout.getText());
	}
	catch(IOException e)
	{
	    getLuwrain().crash(e);
	    return false;
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

    boolean narrating(String[] text)
    {
	NullCheck.notNullItems(text, "text");
	if (isBusy())
	    return false;
	final NarratingText narratingText = new NarratingText();
	narratingText.split(text);
	if (narratingText.sents.isEmpty())
	{
	    getLuwrain().message(getStrings().noTextToSynth(), Luwrain.MessageType.ERROR);
	    return true;
	}
	final File destDir = conv.narratingDestDir();
	if (destDir == null)
	    return true;
	final Channel channel;
	try {
	    channel = getLuwrain().loadSpeechChannel(sett.getNarratingChannelName(""), sett.getNarratingChannelParams(""));
	}
	catch(Exception e)
	{
	    getLuwrain().crash(e);
	    return true;
	}
	if (channel == null)
	{
	    getLuwrain().message(getStrings().noChannelToSynth(sett.getNarratingChannelName("")), Luwrain.MessageType.ERROR);
	    return true;
	}
	Log.debug(LOG_COMPONENT, "narrating channel loaded: " + channel.getChannelName());
	final NarratingLayout layout = new NarratingLayout(this, ()->cancelNarrating());
	this.narrating = new Narrating(this, layout, narratingText.sents.toArray(new String[narratingText.sents.size()]),
				       destDir, new File(getLuwrain().getFileProperty("luwrain.dir.scripts"), "lwr-audio-compress").getAbsolutePath(), channel);
	this.narratingTask = new FutureTask<>(this.narrating, null);
	getLuwrain().executeBkg(this.narratingTask);
	getLayout().setBasicLayout(layout.getLayout());
	return true;
    }

    private void cancelNarrating()
    {
	if (isBusy() && narrating != null)
	{
	    narrating.interrupting = true;
	    return;
	}
	this.narrating = null;
	this.narratingTask = null;
	setAreaLayout(mainLayout);
	getLuwrain().announceActiveArea();
    }

    void finishedNarrating()
    {
	narrating = null;
	narratingTask = null;
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

    @Override public boolean isBusy()
    {
	return narratingTask != null && !narratingTask.isDone();
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    @Override public void closeApp()
    {
	if (isBusy())
	{
	    getLuwrain().message(getStrings().cancelNarratingBeforeClosing(), Luwrain.MessageType.ERROR);
	    return;
	}
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
