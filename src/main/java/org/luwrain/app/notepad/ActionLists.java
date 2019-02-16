
package org.luwrain.app.notepad;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

final class ActionLists
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;

    ActionLists(Luwrain luwrain, Strings strings, Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
    }

    Action[] getActions()
    {
	final List<Action> res = new LinkedList();
	res.add(new Action("open-as", strings.actionOpenAs(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	res.add(new Action("save", strings.actionSave()));
	res.add(new Action("save-as", strings.actionSaveAs(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	//	res.add(new Action("run", "Запустить как скрипт", new KeyboardEvent(KeyboardEvent.Special.F9)),

		if (base.spokenTextType != Luwrain.SpokenTextType.NONE)
		    res.add(new Action("spoken-text-none", strings.actionSpokenTextNone(), new KeyboardEvent(KeyboardEvent.Special.F1, EnumSet.of(KeyboardEvent.Modifiers.ALT))));

		
	if (base.spokenTextType != Luwrain.SpokenTextType.NATURAL)
	    res.add(new Action("spoken-text-natural", strings.actionSpokenTextNatural(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (base.spokenTextType != Luwrain.SpokenTextType.PROGRAMMING)
	    res.add(new Action("spoken-text-programming", strings.actionSpokenTextProgramming(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (!base.speakIndent)
	    res.add(new Action("indent", strings.actionIndents(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	if (base.speakIndent)
	    res.add(new Action("no-indent", strings.actionIndents(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))));
	return res.toArray(new Action[res.size()]);
    }
}
