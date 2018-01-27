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

import org.luwrain.core.*;
import org.luwrain.core.events.*;

final class ActionLists
{
    private final Strings strings;

    ActionLists(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	this.strings = strings;
    }

    Action[] getActions()
    {
	return new Action[]{
	    new Action("open-as", strings.actionOpenAs(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("save", strings.actionSave()),
		    	    new Action("save-as", strings.actionSaveAs(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
		    	    new Action("run", "Запустить как скрипт", new KeyboardEvent(KeyboardEvent.Special.F9)),
	};
    }
}
