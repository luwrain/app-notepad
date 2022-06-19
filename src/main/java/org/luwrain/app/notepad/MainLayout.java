/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.app.base.*;
import org.luwrain.nlp.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    final EditArea editArea;

    final EditSpellChecking spellChecking;
    final EditArea.ChangeListener modificationMarkListener;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	this.spellChecking =new EditSpellChecking(getLuwrain());
	this.modificationMarkListener = (area, lines, hotPoint)->{app.modified = true;};
	this.editArea = new EditArea(editParams((params)->{
		    params.name = "";
		    params.appearance = new Appearance(params.context){
			    @Override App.Mode getMode() { return app.mode; }
			    @Override public EditArea getEditArea() { return editArea; };
			};
		    params.changeListeners = Arrays.asList(modificationMarkListener);
		    params.editFactory = (p)->{
			app.corrector.setDefaultCorrector((MultilineEditCorrector)p.model);
			p.model = app.corrector;
			return new MultilineEdit(p);
		    };
		})){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case SAVE:
			    app.onSave();
			    return true;
			case PROPERTIES:
			    return showProperties();
			case IDLE:
			    return onIdle();
			}
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    if (query.getQueryCode() == AreaQuery.CURRENT_DIR && query instanceof CurrentDirQuery)
			return onDirectoryQuery((CurrentDirQuery)query);
		    return super.onAreaQuery(query);
		}
		@Override public String getAreaName()
		{
		    if (app.file == null)
			return app.getStrings().initialTitle();
		    return app.file.getName();
		}
	    };
	setAreaLayout(editArea, actions(
					action("replace", app.getStrings().actionReplace(), new InputEvent(InputEvent.Special.F5), this::actReplace),
										action("spell-right", app.getStrings().actionSpellRight(), new InputEvent(InputEvent.Special.ARROW_RIGHT, EnumSet.of(InputEvent.Modifiers.SHIFT)), this::actFindSpellRight),
					action("charset", app.getStrings().actionCharset(), new InputEvent(InputEvent.Special.F9), MainLayout .this::actCharset),
					action("narrating", app.getStrings().actionNarrating(), new InputEvent(InputEvent.Special.F10), MainLayout.this::actNarrating),
					action("open", app.getStrings().actionOpen(), new InputEvent(InputEvent.Special.F3, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actOpen),
					action("save-as", app.getStrings().actionSaveAs(), new InputEvent(InputEvent.Special.F2, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actSaveAs),
					action("mode-none", app.getStrings().modeNone(), new InputEvent(InputEvent.Special.F1, EnumSet.of(InputEvent.Modifiers.ALT)), MainLayout.this::actModeNone),
					action("mode-natural", app.getStrings().modeNatural(), new InputEvent(InputEvent.Special.F2, EnumSet.of(InputEvent.Modifiers.ALT)), MainLayout.this::actModeNatural),
					action("mode-programming", app.getStrings().modeProgramming(), new InputEvent(InputEvent.Special.F3, EnumSet.of(InputEvent.Modifiers.ALT)), MainLayout.this::actModeProgramming)
					));
    }

    private boolean actReplace()
    {
	final String oldValue = app.getConv().replaceExp();
	if (oldValue == null || oldValue.isEmpty())
	    return true;
	final String newValue = app.getConv().replaceWith();
	if (newValue == null)
	    return true;
	editArea.update((lines, hotPoint)->{
		for(int i = 0;i < lines.getLineCount();i++)
		    lines.setLine(i, lines.getLine(i).replaceAll(oldValue, newValue));
		return true;
	    });
	return true;
    }

    private boolean actFindSpellRight()
    {
	final AtomicBoolean moved = new AtomicBoolean(false);
	editArea.update((lines, hotPoint)->{
		final int index = hotPoint.getHotPointY();
		final int len = lines.getLine(index).length();
		for(int i = 0;i < len;i++)
		{
		    final LineMarks lineMarks = lines.getLineMarks(index);
		    if (lineMarks == null)
			continue;
		    final LineMarks.Mark[] marks = lineMarks.findAtPos(i);
		    if (marks == null || marks.length == 0)
			continue;
		    for(LineMarks.Mark m: marks)
			if (m.getMarkObject() != null && m.getMarkObject() instanceof SpellProblem)
		    {
			hotPoint.setHotPointX(i);
			moved.set(true);
			return false;
		    }
		}
		if (!moved.get())
		return false;
		getControlContext().onAreaNewHotPoint(editArea);
		return true;
	    });

	return true;
    }

    private boolean onIdle()
    {
	final MarkedLines lines = editArea.getContent();
	final int
	x = editArea.getHotPointX(),
	y = editArea.getHotPointY();
	if (y >= lines.getLineCount())
	    return true;
	final LineMarks marks = lines.getLineMarks(y);
	if (marks == null)
	    return  true;
	final LineMarks.Mark[] atPoint = marks.findAtPos(x);
	if (atPoint == null || atPoint.length == 0)
	    return true;
	for(LineMarks.Mark m: atPoint)
	{
	    if (m.getMarkObject() == null || !(m.getMarkObject() instanceof SpellProblem))
		continue;
	    final SpellProblem p = (SpellProblem)m.getMarkObject();
	    app.message(p.getComment(), Luwrain.MessageType.ANNOUNCEMENT);
	    return true;
	}
	return true;
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
	    app.crash(e);
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
	    app.save(editArea.getText());
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
	return app.narrating(editArea.getText());
    }

    private boolean showProperties()
    {
	final String[] lines = app.getHooks().runPropertiesHook(editArea);
	if (lines.length == 0)
	    return false;
	final PropertiesLayout propertiesLayout = new PropertiesLayout(app, lines, ()->{
		app.setAreaLayout(this);
		app.getLuwrain().announceActiveArea();
	    });
	app.openLayout(propertiesLayout.getLayout());
	app.getLuwrain().announceActiveArea();
	return true;
    }

    private boolean actModeNone()
    {
	app.mode = App.Mode.NONE;
	editArea.setChangeListeners(Arrays.asList(modificationMarkListener));
	spellChecking.eraseSpellingMarks(editArea);
	app.message(app.getStrings().modeNone(), Luwrain.MessageType.OK);
	return true;
    }

    private boolean actModeNatural()
    {
	app.mode = App.Mode.NATURAL;
	editArea.setChangeListeners(Arrays.asList(modificationMarkListener, spellChecking));
	spellChecking.initialChecking(editArea);
	app.message(app.getStrings().modeNatural(), Luwrain.MessageType.OK);
	return true;
    }

    private boolean actModeProgramming()
    {
	app.mode = App.Mode.PROGRAMMING;
	spellChecking.eraseSpellingMarks(editArea); 
	editArea.setChangeListeners(Arrays.asList(modificationMarkListener));
	app.message(app.getStrings().modeProgramming(), Luwrain.MessageType.OK);
	return true;
    }

    void setText(String[] text)
    {
	editArea.update((lines, hotPoint)->{
		lines.setLines(text);
		return false;//Means no need to call listeners etc
	    });
	editArea.refresh();
    }

        void onNewFile()
    {
	app.getLuwrain().onAreaNewName(editArea);
    }
}
