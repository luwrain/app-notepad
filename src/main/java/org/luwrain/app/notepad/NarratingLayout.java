/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.app.base.*;

final class NarratingLayout extends LayoutBase implements Narrating.Listener
{
    private final App app;
    private final SimpleArea narratingArea;

    NarratingLayout(App app, Runnable closing)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.narratingArea = new SimpleArea(new DefaultControlContext(app.getLuwrain()), app.getStrings().narratingAreaName()){
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event, closing))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public void announceLine(int index, String line)
		{
		    NullCheck.notNull(line, "line");
		    app.getLuwrain().setEventResponse(DefaultEventResponse.text(app.getLuwrain().getSpeakableText(line, Luwrain.SpeakableTextType.PROGRAMMING)));
		}
	    };
	narratingArea.addLine(app.getStrings().narratingProgress("0.0%"));
	narratingArea.addLine("");
    }

    @Override public void writeMessage(String text)
    {
	NullCheck.notNull(text, "text");
	app.getLuwrain().runUiSafely(()->{
		narratingArea.insertLine(narratingArea.getLineCount() - 2, text);
	    });
    }

    @Override public void progressUpdate(int sentsProcessed, int sentsTotal)
    {
	final float value = ((float)sentsProcessed * 100) / sentsTotal;
	app.getLuwrain().runUiSafely(()->{
		narratingArea.setLine(narratingArea.getLineCount() - 2, app.getStrings().narratingProgress(String.format("%.1f", value) + "%"));
	    });
    }

    @Override public void done()
    {
	app.getLuwrain().runUiSafely(()->{
		app.finishedNarrating();
		app.getLuwrain().onAreaNewBackgroundSound(narratingArea);
		narratingArea.setLine(narratingArea.getLineCount() - 2, app.getStrings().narratingDone());
		app.getLuwrain().message(app.getStrings().narratingDone(), Luwrain.MessageType.DONE);
	    });
    }

    @Override public void cancelled()
    {
	app.getLuwrain().runUiSafely(()->{
		app.finishedNarrating();
		app.getLuwrain().onAreaNewBackgroundSound(narratingArea);
		narratingArea.setLine(narratingArea.getLineCount() - 2, app.getStrings().narratingCancelled());
		app.getLuwrain().message(app.getStrings().narratingCancelled(), Luwrain.MessageType.DONE);
	    });
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(narratingArea);
    }
}
