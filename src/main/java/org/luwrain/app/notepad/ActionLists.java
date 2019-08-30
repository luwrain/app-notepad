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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.script.*;
import org.luwrain.script.hooks.*;

final class ActionLists
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;
    private final Action[] hookActions;

    ActionLists(Luwrain luwrain, Strings strings, Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
	final CollectorHook hook = new CollectorHook(luwrain);
	final Object[] res;
	try {
	    res = hook.runForArrays("luwrain.notepad.actions", new Object[0]);
	}
	catch(RuntimeException e)
	{
	    luwrain.crash(e);
	    this.hookActions = new Action[0];
	    return;
	}
	final List<Action> actions = new LinkedList();
	for(Object o: res)
	{
	    final Action a = ScriptUtils.getAction(o);
	    if (a != null)
		actions.add(a);
	}
	this.hookActions = actions.toArray(new Action[actions.size()]);
    }

    Action[] getActions()
    {
	final List<Action> res = new LinkedList();
	res.add(new Action("open", strings.actionOpen(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	res.add(new Action("save", strings.actionSave(), new KeyboardEvent(KeyboardEvent.Special.F2)));
	res.add(new Action("save-as", strings.actionSaveAs(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	res.add(new Action("charset", strings.actionCharset(), new KeyboardEvent(KeyboardEvent.Special.F9)));
	res.add(new Action("narrating", strings.actionNarrating(), new KeyboardEvent(KeyboardEvent.Special.F10)));
	if (base.mode != Base.Mode.NONE)
	    res.add(new Action("mode-none", strings.modeNone(), new KeyboardEvent(KeyboardEvent.Special.F1, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (base.mode != Base.Mode.NATURAL)
	    res.add(new Action("mode-natural", strings.modeNatural(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (base.mode != Base.Mode.PROGRAMMING)
	    res.add(new Action("mode-programming", strings.modeProgramming(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (!base.speakIndent)
	    res.add(new Action("indent", strings.actionIndents(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (base.speakIndent)
	    res.add(new Action("no-indent", strings.actionIndents(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	for(Action a: hookActions)
	    res.add(a);
	return res.toArray(new Action[res.size()]);
    }
}
