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
							action("open", app.getStrings().actionOpen(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT)), MainLayout.this::actOpen),
														action("save-as", app.getStrings().actionSaveAs(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT)), MainLayout.this::actSaveAs),
														action("charset", app.getStrings().actionCharset(), new KeyboardEvent(KeyboardEvent.Special.F9), MainLayout .this::actCharset)
							);
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
			if (event.getType() == EnvironmentEvent.Type.REGULAR)
			    switch(event.getCode())
			    {
			    case SAVE:
				app.onSave();
				return true;
			    }
					    if (app.onSystemEvent(this, event, actions))
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
		params.appearance = new Appearance(params.context);
		params.appearance = new EditUtils.DefaultEditAreaAppearance(params.context);
	params.changeListener = ()->{app.modified = true;};
	params.editFactory = (p, c)->{
	    final MultilineEdit.Params pp = new MultilineEdit.Params();
	    pp.context = p.context;
	    	    	    app.corrector.setDefaultCorrector(c);
	    pp.model = app.corrector;
	    pp.regionPoint = p.regionPoint;
	    pp.appearance = p.appearance;
	    return new MultilineEdit(pp);
	};
	return params;
    }
}
