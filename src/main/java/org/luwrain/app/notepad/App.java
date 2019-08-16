/*
c   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

final class App implements Application
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;

    private EditArea editArea = null;
    private AreaLayoutHelper layout = null;

    private final String arg;

    App()
    {
	arg = null;
    }

    App(String arg)
    {
	NullCheck.notNull(arg, "arg");
	this.arg = arg;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actions = new Actions(luwrain, strings, base);
	this.actionLists = new ActionLists(luwrain, strings, base);
	createArea();
	this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
	    }, editArea);
	if (arg != null && !arg.isEmpty())
	{
	    base.file = new File(arg);
	    try {
		final String[] lines = base.read();
		editArea.setLines(lines);
	    }
	    catch(IOException e)
	    {
		luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    }
	}
	return new InitResult();
    }

    private void createArea()
    {
	this.editArea = new EditArea(base.createEditParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    closeApp();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case SAVE:
			return actions.onSave( editArea);
		    case PROPERTIES:
			return showProps();
		    case ACTION:
			if (runActionHooks(event, regionPoint))
			    return true;
			if (ActionEvent.isAction(event, "save"))
			    return actions.onSave(this);
			if (ActionEvent.isAction(event, "save-as"))
			{
			    actions.onSaveAs(this);
			    return true;
			}
			if (ActionEvent.isAction(event, "open-as"))
			    return actions.onOpenAs();
			if (ActionEvent.isAction(event, "charset"))
			    return actions.onCharset();
			if (ActionEvent.isAction(event, "mode-none"))
			{
			    base.activateMode(Base.Mode.NONE);
			    luwrain.message(strings.modeNone(), Luwrain.MessageType.OK);
			    return true;
			}
			if (ActionEvent.isAction(event, "mode-natural"))
			{
			    base.activateMode(Base.Mode.NATURAL);
			    luwrain.message(strings.modeNatural(), Luwrain.MessageType.OK);
			    return true;
			}
			if (ActionEvent.isAction(event, "mode-programming"))
			{
			    base.activateMode(Base.Mode.PROGRAMMING);
			    luwrain.message(strings.modeProgramming(), Luwrain.MessageType.OK);
			    return true;
			}
			return false;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    if (base.file == null)
			return strings.initialTitle();
		    return base.file.getName();
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getActions();
		}
	    };
    }

    private boolean runActionHooks(EnvironmentEvent event, AbstractRegionPoint regionPoint)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(regionPoint, "regionPoint");
	if (!(event instanceof ActionEvent))
	    return false;
	final ActionEvent actionEvent = (ActionEvent)event;
	final MultilineEdit2.Model model = editArea.getEdit().getMultilineEditModel();
	if (model == null || !(model instanceof MultilineEditCorrector2))
	    return false;
	final MultilineEditCorrector2 corrector = (MultilineEditCorrector2)model;
	final AtomicBoolean res = new AtomicBoolean(false);
	corrector.doEditAction((lines, hotPoint)->{
		try {
		    res.set(luwrain.xRunHooks("luwrain.notepad.action", new Object[]{
				actionEvent.getActionName(),
				EditUtils.createHookObject(editArea, lines, hotPoint, regionPoint)
			    }, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY));
		}
		catch(RuntimeException e)
		{
		    luwrain.crash(e);
		}
	    });
	return res.get();
    }

    private boolean showProps()
    {
	final SimpleArea propsArea = new SimpleArea(new DefaultControlContext(luwrain), strings.propsAreaName()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	final EmptyHookObject hookObj = new EmptyHookObject(){
		@Override public Object getMember(String name)
		{
		    NullCheck.notEmpty(name, "name");
		    switch(name)
		    {
		    case "lines":
			return ScriptUtils.createReadOnlyArray(editArea.getLines());
		    case "fileName":
			if (base.file == null)
			    return "";
						return base.file.getAbsolutePath();
		    case "charset":
			return base.charset;
		    default:
			return super.getMember(name);
		    }
		}
	    };
	final List<String> res = new LinkedList();
	try {
	    final Object o = new org.luwrain.script.hooks.ProviderHook(luwrain).run("luwrain.notepad.properties.basic", new Object[]{hookObj});
	    if (o != null)
	    {
		final List r = ScriptUtils.getArray(o);
		for(Object i: r)
		{
		    final String s = ScriptUtils.getStringValue(i);
		    if (s != null && !s.trim().isEmpty())
			res.add(s);
		}
	    }
	}
	catch(RuntimeException e)
	{
	    luwrain.crash(e);
	    return true;
	}
	propsArea.beginLinesTrans();
	propsArea.addLine("");
	for(String s: res)
	    propsArea.addLine(s);
	propsArea.addLine("");
	propsArea.endLinesTrans();
	layout.openTempArea(propsArea);
	return true;
    }

    //Returns true, if there are no more modification which the user would like to save
    private boolean noUnsavedData()
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
	if (!noUnsavedData())
	    return;
	luwrain.closeApp();
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
