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

final class MainLayout extends LayoutBase
{
    private final App app;
    private final EditArea editArea;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
			       this.editArea = new EditArea(createEditParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		    		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public String getAreaName()
		{
		    if (base.file == null)
			return strings.initialTitle();
		    return base.file.getName();
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };

    private boolean runActionHooks(EnvironmentEvent event, AbstractRegionPoint regionPoint)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(regionPoint, "regionPoint");
	if (!(event instanceof ActionEvent))
	    return false;
	final ActionEvent actionEvent = (ActionEvent)event;
	final MultilineEdit.Model model = editArea.getEdit().getMultilineEditModel();
	if (model == null || !(model instanceof MultilineEditCorrector))
	    return false;
	final MultilineEditCorrector corrector = (MultilineEditCorrector)model;
	final AtomicBoolean res = new AtomicBoolean(false);
	corrector.doEditAction((lines, hotPoint)->{
		try {
		    res.set(luwrain.xRunHooks("luwrain.notepad.action", new Object[]{
				actionEvent.getActionName(),
				org.luwrain.script.TextScriptUtils.createTextEditHookObject(editArea, lines, hotPoint, regionPoint)
			    }, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY));
		}
		catch(RuntimeException e)
		{
		    luwrain.crash(e);
		}
	    });
	return res.get();
    }

        void onOpen(EditArea editArea)
    {
	NullCheck.notNull(editArea, "editArea");
	final File file = conv.open();
	if (file == null)
	    return;
	base.file = file;
	luwrain.onAreaNewName(editArea);
	try {
	    editArea.getContent().setLines(base.read());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return;
	}
	editArea.reset(false);
	luwrain.onAreaNewContent(editArea);
	luwrain.onAreaNewHotPoint(editArea);
	base.modified = false;
    }

    //Returns True if everything saved, false otherwise
    boolean onSave(EditArea area)
    {
	NullCheck.notNull(area, "area");
	if (!base.modified)
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (base.file == null)
	{
	    final File f = conv.save(base.file );
	    if (f == null)
		return false;
	    base.file = f;
	    luwrain.onAreaNewName(area);
	}
	try {
	    base.save(area.getLines());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return true;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MessageType.OK);
	return true;
    }

    void onSaveAs(EditArea area)
    {
	NullCheck.notNull(area, "area");
	final File f = conv.save(base.file);
	if (f == null)
	    return;
	base.file = f;
	luwrain.onAreaNewName(area);
	try {
	    base.save(area.getLines());
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return;
	}
	base.modified = false;
	luwrain.message(strings.fileIsSaved(), Luwrain.MessageType.OK);
    }

    void onCharset(EditArea editArea)
    {
	NullCheck.notNull(editArea, "editArea");
	if (base.modified)
	{
	    switch(conv.unsavedChanges())
	    {
	    case CONTINUE_SAVE:
		if (!onSave(editArea))
		    return;
	    case CANCEL:
		return;
	    }
	}
	final String res = conv.charset();
	if (res == null)
	    return;
	base.charset = res;
	if (base.file != null && //!base.modified &&
	    conv.rereadWithNewCharser(base.file))
	{
	    try {
		editArea.getContent().setLines(base.read());
	    }
	    catch(IOException e)
	    {
		luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
		return;
	    }
	    luwrain.onAreaNewContent(editArea);
	}
    }



AreaLayout getLayout()
    {
	return new AreaLayout(editArea);
    }

    EditArea.Params createEditParams()
    {
		final EditArea.Params params = new EditArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.name = "";
		params.appearance = new Appearance(params.context);
		params.appearance = new EditUtils.DefaultEditAreaAppearance(params.context);
	params.changeListener = ()->{modified = true;};
	params.editFactory = (p, c)->{
	    final MultilineEdit.Params pp = new MultilineEdit.Params();
	    pp.context = p.context;
	    	    	    Base.this.corrector.setDefaultCorrector(c);
	    pp.model = Base.this.corrector;
	    pp.regionPoint = p.regionPoint;
	    pp.appearance = p.appearance;
	    return new MultilineEdit(pp);
	};
	return params;
    }

}
