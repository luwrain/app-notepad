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
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.script.*;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private final EditArea editArea;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.editArea = new EditArea(createEditParams()) {
		private final Actions actions = actions(
							action("open", app.getStrings().actionOpen(), new InputEvent(InputEvent.Special.F3, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actOpen),
							action("save-as", app.getStrings().actionSaveAs(), new InputEvent(InputEvent.Special.F2, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actSaveAs),
							action("charset", app.getStrings().actionCharset(), new InputEvent(InputEvent.Special.F9), MainLayout .this::actCharset),
							action("narrating", app.getStrings().actionNarrating(), new InputEvent(InputEvent.Special.F10), MainLayout.this::actNarrating),
							action("mode-none", app.getStrings().modeNone(), new InputEvent(InputEvent.Special.F1, EnumSet.of(InputEvent.Modifiers.ALT)), MainLayout.this::actModeNone),
							action("mode-natural", app.getStrings().modeNatural(), new InputEvent(InputEvent.Special.F2, EnumSet.of(InputEvent.Modifiers.ALT)), MainLayout.this::actModeNatural),
							action("mode-programming", app.getStrings().modeProgramming(), new InputEvent(InputEvent.Special.F3, EnumSet.of(InputEvent.Modifiers.ALT)), MainLayout.this::actModeProgramming)
							);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
			if (event.getType() == SystemEvent.Type.REGULAR)
			    switch(event.getCode())
			    {
			    case SAVE:
				app.onSave();
								return true;
			    case PROPERTIES:
				return showProperties();
			    }
					    if (app.onSystemEvent(this, event, actions))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (query.getQueryCode() == AreaQuery.CURRENT_DIR && query instanceof CurrentDirQuery)
			return onDirectoryQuery((CurrentDirQuery)query);
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public String getAreaName()
		{
		    if (app.file == null)
			return app.getStrings().initialTitle();
		    return app.file.getName();
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
	    };
    }

    private boolean onDirectoryQuery(CurrentDirQuery query)
    {
	NullCheck.notNull(query, "query");
	if (app.file == null)
	    return false;
	final File f = app.file.getParentFile();
	if (f == null)
	    return false;
	query.answer(f.getAbsolutePath());
	return true;
    }

    private boolean actOpen()
    {
	if (!app.everythingSaved())
	    return true;
		final File file = app.getConv().open();
	if (file == null)
	    return true;
	//To restore on failed reading
	final File origFile = app.file;
	app.file = file;
	try {
	    setText(app.read());
	}
	catch(IOException e)
	{
	    app.file = origFile;
	    app.getLuwrain().crash(e);
	    return true;
	}
	app.setAppName(app.file.getName());
	editArea.reset(false);
	onNewFile();
	app.modified = false;
	return true;
    }

    private boolean actSaveAs()
    {
	final File f = app.getConv().save(app.file);
	if (f == null)
	    return true;
	app.file = f;
	onNewFile();
	try {
	    app.save(getText());
	}
	catch(IOException e)
	{
	    app.getLuwrain().crash(e);
	    return true;
	}
	app.modified = false;
	app.getLuwrain().message(app.getStrings().fileIsSaved(), Luwrain.MessageType.OK);
	return true;
    }

    private boolean actCharset()
    {
		if (!app.everythingSaved())
	    return true;
	final String res = app.getConv().charset();
	if (res == null)
	    return true;
	app.charset = res;
	if (app.file != null && app.getConv().rereadWithNewCharser(app.file))
	{
	    try {
		setText(app.read());
	    }
	    catch(IOException e)
	    {
		app.getLuwrain().crash(e);
		return true;
	    }
	}
    return true;
    }

    private boolean actNarrating()
    {
	return app.narrating(editArea.getLines());
    }

    private boolean showProperties()
    {
	final String[] lines = app.getHooks().runPropertiesHook(editArea);
	if (lines.length == 0)
	    return false;
	final PropertiesLayout propertiesLayout = new PropertiesLayout(app, lines, ()->{
		app.openLayout(getLayout());
		app.getLuwrain().announceActiveArea();
	    });
	app.openLayout(propertiesLayout.getLayout());
	app.getLuwrain().announceActiveArea();
	return true;
    }

    private boolean actModeNone()
    {
	app.mode = App.Mode.NONE;
	app.getLuwrain().message(app.getStrings().modeNone(), Luwrain.MessageType.OK);
	return true;
    }

    private boolean actModeNatural()
    {
	app.mode = App.Mode.NATURAL;
		app.getLuwrain().message(app.getStrings().modeNatural(), Luwrain.MessageType.OK);
	return true;
    }

    private boolean actModeProgramming()
    {
	app.mode = App.Mode.PROGRAMMING;
		app.getLuwrain().message(app.getStrings().modeProgramming(), Luwrain.MessageType.OK);
	return true;
    }

        String[] getText()
	      {
		  return editArea.getLines();
	      }

    void setText(String[] text)
    {
	NullCheck.notNullItems(text, "text");
	editArea.getContent().setLines(text);
	app.getLuwrain().onAreaNewContent(editArea);
    }

        void onNewFile()
    {
	app.getLuwrain().onAreaNewName(editArea);
    }

AreaLayout getLayout()
    {
	return new AreaLayout(editArea);
    }

    EditArea.Params createEditParams()
    {
		final EditArea.Params params = new EditArea.Params();
		params.context = new DefaultControlContext(app.getLuwrain());
	params.name = "";
	params.appearance = new Appearance(params.context){
		@Override App.Mode getMode()
		{
		    return app.mode;
		}
	    };
	params.changeListener = ()->{app.modified = true;};
	params.editFactory = (p)->{
	    app.corrector.setDefaultCorrector((MultilineEditCorrector)p.model);
	    p.model = app.corrector;
	    return new MultilineEdit(p);
	};
	return params;
    }
}
