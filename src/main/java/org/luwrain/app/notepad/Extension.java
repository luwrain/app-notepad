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

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.cpanel.*;

public final class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    static private final Element controlPanelElement = new SimpleElement(StandardElements.APPLICATIONS, Extension.class.getName());

    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new Command(){
		@Override public String getName()
		{
		    return "notepad";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("notepad");
		}
	    }};
    }

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new ExtensionObject[]{
	    
	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "notepad";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    if (args.length == 0)
			return new Application[]{new App()};
		    final List<Application> v = new LinkedList();
		    for(String s: args)
			v.add(new App(s));
		    if (v.isEmpty())
			return new Application[]{new App()};
		    return v.toArray(new Application[v.size()]);
		}
	    },

	};
    }

    @Override public Factory[] getControlPanelFactories(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new Factory[]{
	    new Factory() {
		@Override public Element[] getElements()
		{
		    return new Element[]{controlPanelElement};
		}
		@Override public Element[] getOnDemandElements(Element parent)
		{
		    return new Element[0];
		}
		@Override public org.luwrain.cpanel.Section createSection(Element el)
		{
		    NullCheck.notNull(el, "el");
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (el.equals(controlPanelElement))
			return new SimpleSection(controlPanelElement, strings.settingsFormName(), (controlPanel)->SettingsForm.create(controlPanel));
		    return null;
		}
	    }
	};
    }
}
