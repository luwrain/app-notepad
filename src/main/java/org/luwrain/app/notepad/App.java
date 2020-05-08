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
import java.util.concurrent.atomic.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.script.*;

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

    final EditUtils.ActiveCorrector corrector;

    
//for narrating
    FutureTask narratingTask = null; 
    Narrating narrating = null;



    
    private final String arg;

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

    @Override public boolean onAppInit()
    {
	if (arg != null && !arg.isEmpty())
	{
	    base.file = new File(arg);
	    try {
		if (base.file.exists() && !base.file.isDirectory())
		    editArea.setLines(base.read());
		base.modified = false;
	    }
	    catch(IOException e)
	    {
		luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)));
	    }
	}
	return new InitResult();
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

    //Returns true, if there are no more modification which the user might want to save
    private boolean everythingSaved()
    {
	if (!base.modified)
	    return true;
	switch(actions.conv.unsavedChanges())
	{
	case CONTINUE_SAVE:
	    return actions.onSave(editArea);
	case CONTINUE_UNSAVED:
	    return true;
	case CANCEL:
	    return false;
	}
	return false;
    }

    @Override public void closeApp()
    {
	if (!everythingSaved())
	    return;
	luwrain.closeApp();
    }

        void activateMode(Mode mode)
    {
	NullCheck.notNull(mode, "mode");
	switch(mode)
	{
	case NATURAL:
	    corrector.setActivatedCorrector(new DirectScriptMultilineEditCorrector(new DefaultControlContext(luwrain), corrector.getDefaultCorrector(), NATURAL_MODE_CORRECTOR_HOOK));
	    break;
	case PROGRAMMING:
	    corrector.setActivatedCorrector(new DirectScriptMultilineEditCorrector(new DefaultControlContext(luwrain), corrector.getDefaultCorrector(), PROGRAMMING_MODE_CORRECTOR_HOOK));
	    break;
	}
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

        boolean onNarrating(SimpleArea destArea, String[] text)
    {
	NullCheck.notNull(destArea, "destArea");
	NullCheck.notNullItems(text, "text");
	if (base.narratingTask != null && !base.narratingTask.isDone())
	{
	    luwrain.setActiveArea(destArea);
	    return false;
	}

	final NarratingText narratingText = new NarratingText();
	narratingText.split(text);
	if (narratingText.sents.isEmpty())
	{
	    luwrain.message(strings.noTextToSynth(), Luwrain.MessageType.ERROR);
	    return false;
	}
	final File destDir = conv.narratingDestDir();
	if (destDir == null)
	    return false;
	final Channel channel;
	try {
	    channel = luwrain.loadSpeechChannel(base.sett.getNarratingChannelName(""), base.sett.getNarratingChannelParams(""));
	}
	catch(Exception e)
	{
	    luwrain.message(strings.errorLoadingSpeechChannel(e.getMessage()), Luwrain.MessageType.ERROR);
	    e.printStackTrace();
	    return false;
	}
	if (channel == null)
	{
	    luwrain.message(strings.noChannelToSynth(base.sett.getNarratingChannelName("")), Luwrain.MessageType.ERROR);
	    return false;
	}
	Log.debug(LOG_COMPONENT, "narrating channel loaded: " + channel.getChannelName());
	destArea.clear();
	destArea.addLine(base.strings.narratingProgress("0.0%"));
	destArea.addLine("");
	base.narrating = new Narrating(base,
				       narratingText.sents.toArray(new String[narratingText.sents.size()]),
				       destDir, 
				       new File(luwrain.getFileProperty("luwrain.dir.scripts"), "lwr-audio-compress").getAbsolutePath(), channel){
		@Override protected void writeMessage(String text)
		{
		    NullCheck.notNull(text, "text");
		    luwrain.runUiSafely(()->{
			    destArea.insertLine(destArea.getLineCount() - 2, text);
			});
		}
		@Override protected void progressUpdate(int sentsProcessed, int sentsTotal)
		{
		    final float value = ((float)sentsProcessed * 100) / sentsTotal;
		    luwrain.runUiSafely(()->{
			    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingProgress(String.format("%.1f", value)) + "%");
			});
		}
		@Override protected void done()
		{
		    		    luwrain.runUiSafely(()->{
					    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingDone());
					    		    luwrain.message(strings.narratingDone(), Luwrain.MessageType.DONE);
			});
		}
				@Override protected void cancelled()
		{
		    		    luwrain.runUiSafely(()->{
					    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingCancelled());
					    		    luwrain.message(strings.narratingCancelled(), Luwrain.MessageType.DONE);
			});
		}
	    };
	base.narratingTask = new FutureTask(base.narrating, null);
	luwrain.executeBkg(base.narratingTask);
	return true;
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
