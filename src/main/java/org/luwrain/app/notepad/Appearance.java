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
import org.luwrain.controls.*;

abstract class Appearance extends EditUtils.DefaultEditAreaAppearance
{
    Appearance(ControlContext context)
    {
	super(context);
    }

    abstract App.Mode getMode();

    @Override public void announceLine(int index, String line)
    {
	final App.Mode mode = getMode();
	if (mode == null)
	{
	    NavigationArea.defaultLineAnnouncement(context, index, line);
	    return;
	}
	switch(mode)
	{
	case NONE:
	    NavigationArea.defaultLineAnnouncement(context, index, context.getSpeakableText(line, Luwrain.SpeakableTextType.NONE));
	    break;
	case PROGRAMMING:
	    NavigationArea.defaultLineAnnouncement(context, index, context.getSpeakableText(line, Luwrain.SpeakableTextType.PROGRAMMING));
	    break;
	case NATURAL:
	    NavigationArea.defaultLineAnnouncement(context, index, context.getSpeakableText(line, Luwrain.SpeakableTextType.NATURAL));
	    break;
	default:
	    NavigationArea.defaultLineAnnouncement(context, index, line);
	}
    }
}
