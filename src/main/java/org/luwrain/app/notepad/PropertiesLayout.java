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
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

final class PropertiesLayout extends LayoutBase
{
    private final App app;
    private final SimpleArea propsArea;

    PropertiesLayout(App app, String[] lines, Runnable closing)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNullItems(lines, "lines");
	NullCheck.notNull(closing, "closing");
	this.app = app;
	this.propsArea = new SimpleArea(new DefaultControlContext(app.getLuwrain()), app.getStrings().propsAreaName()) {
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
	    };
	propsArea.update((text)->{
text.addLine("");
	for(String s: lines)
	    text.addLine(s);
	text.addLine("");
	    });
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(propsArea);
    }
}
