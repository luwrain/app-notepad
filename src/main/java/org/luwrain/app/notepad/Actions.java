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

import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

class Actions
{
    private final Luwrain luwrain;
    private final Strings strings;

    Actions(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    Action[] getEditAreaActions()
    {
	return new Action[]{
	    new Action("save", strings.actionSave()),
	    new Action("open-another-charset", strings.actionOpenAnotherCharset()),
	    new Action("save-another-charset", strings.actionSaveAnotherCharset()),
	    new Action("remove-backslash-r", strings.actionRemoveBackslashR()),
		       new Action("add-backslash-r", strings.actionAddBackslashR()),
	    new Action("info", strings.actionInfo()),
	};
    }

    boolean removeBackslashR(Base base, EditArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	area.beginLinesTrans();
	for(int i = 0;i < area.getLineCount();++i)
	{
	    final String line = area.getLine(i);
	    NullCheck.notNull(line, "line");
	    if (line.isEmpty())
		continue;
	    final StringBuilder b = new StringBuilder();
	    for(int k = 0;k < line.length();++k)
		if (line.charAt(k) != '\r')
		    b.append(line.charAt(k));
	    area.setLine(i, b.toString());
	}
	area.endLinesTrans();
	luwrain.onAreaNewContent(area);
	base.markAsModified();
	return true;
    }

    boolean addBackslashR(Base base, EditArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	area.beginLinesTrans();
	for(int i = 0;i < area.getLineCount();++i)
	{
	    final String line = area.getLine(i);
	    NullCheck.notNull(line, "line");
	    area.setLine(i, line + '\r');
	}
	area.endLinesTrans();
	luwrain.onAreaNewContent(area);
	base.markAsModified();
	return true;
    }

    /**
     * Ensures there is no any unsaved data, so it is safe to proceed.
     *
     * \return True if everything saved, false otherwise
     */
    boolean save(Base base, EditArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	if (!base.isModified())
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (base.file == null)
	{
	    base.file = savePopup(base);
	    if (base.file == null)
		return false;
	}
	try {
	    base.save(base.file.toPath(), area.getLines(), base.DEFAULT_CHARSET);
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorSavingFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	base.markNoModifications();
	area.setName(base.file.getName());
	luwrain.message(strings.fileIsSaved(), Luwrain.MESSAGE_OK);
	return true;
    }

    boolean onSave(Base base, EditArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	save(base, area);
	return true;
    }

    boolean onOpenEvent(Base base, String fileName, EditArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(fileName, "fileName");
	NullCheck.notNull(area, "area");
	if (fileName.isEmpty())
	    return false;
	if (base.isModified() || base.file != null)
	    return false;
	final Path newPath = Paths.get(fileName);
	if (Files.isDirectory(newPath))
	    return false;
	final String[] lines;
	try {
	    lines = base.read(newPath, Base.DEFAULT_CHARSET);
	}
	catch(IOException e)
	{
	    luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	base.file = newPath.toFile();
	area.setLines(lines);
	area.setName(base.file.getName());
	base.markNoModifications();
	return true;
    }


    private File savePopup(Base base)
    {
	NullCheck.notNull(base, "base");
	return Popups.path(luwrain, 
			   strings.savePopupName(), strings.savePopupPrefix(),
			   base.file != null?base.file:luwrain.getFileProperty("luwrain.dir.userhome"), luwrain.getFileProperty("luwrain.dir.userhome"),
			   (fileToCheck, announce)->{
			       if (fileToCheck.isDirectory())
			       {
				   if (announce)
				   luwrain.message(strings.enteredPathMayNotBeDir(fileToCheck.getAbsolutePath()), Luwrain.MESSAGE_ERROR);
				   return false;
			       }
			       return true;
			   });
    }
}
