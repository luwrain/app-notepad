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
import org.luwrain.template.*;

final class NarratingLayout extends LayoutBase implements Narrating.Listener
{
    private final App app;
    private final SimpleArea narratingArea;

    NarratingLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.narratingArea = new SimpleArea(new DefaultControlContext(app.getLuwrain()), app.getStrings().narratingAreaName()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };
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
			    //			    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingProgress(String.format("%.1f", value)) + "%");
			});
		}
    
		@Override public void done()
		{
		    app.getLuwrain().runUiSafely(()->{
			    //					    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingDone());
			    app.getLuwrain().message(app.getStrings().narratingDone(), Luwrain.MessageType.DONE);
			});
		}
    
		@Override public void cancelled()
		{
		    app.getLuwrain().runUiSafely(()->{
			    //					    destArea.setLine(destArea.getLineCount() - 2, base.strings.narratingCancelled());
			    app.getLuwrain().message(app.getStrings().narratingCancelled(), Luwrain.MessageType.DONE);
			});
		}

    AreaLayout getLayout()
    {
	return new AreaLayout(narratingArea);
    }

}

