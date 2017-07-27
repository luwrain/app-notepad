/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.popups.*;

class CustomFilePopup extends FilePopup
{
    interface Actions
    {
	boolean onAction(EnvironmentEvent actionEvent);
	Action[] getActions();
    }

    private final Actions actions;

    CustomFilePopup(Luwrain luwrain, Strings strings, Actions actions,
		    String name, String prefix, File startingValue)
    {
	super(luwrain, name, prefix, 
	      createAcceptance(luwrain, strings),
	      startingValue, luwrain.getFileProperty("luwrain.dir.userhome"),
	      Popups.loadFilePopupFlags(luwrain), Popups.DEFAULT_POPUP_FLAGS);
	NullCheck.notNull(actions, "actions");
	this.actions = actions;
    }

    @Override public Action[] getAreaActions()
    {
	final List<Action> res = new LinkedList<Action>();
	for(Action a: actions.getActions())
	    res.add(a);
	final Action[] superActions = super.getAreaActions();
	if (superActions != null)
	    for(Action a: superActions)
	    res.add(a);
	return res.toArray(new Action[res.size()]);
    }

    static FilePopup.Acceptance createAcceptance(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	return (fileToCheck, announce)->{
	    if (fileToCheck.isDirectory())
		return false;
	    return true;
	};
    }
}
