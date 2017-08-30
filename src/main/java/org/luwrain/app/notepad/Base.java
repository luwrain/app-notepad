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

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.channels.*;

import org.apache.commons.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class Base
{
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Luwrain luwrain;
    private final Strings strings;

boolean modified = false;
    FileParams file = null;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    void prepareDocument(String arg, EditArea area)
    {
	NullCheck.notNull(area, "area");
	area.setName(strings.initialTitle());
	if (arg == null || arg.isEmpty())
	    return;
	file = new FileParams(new File(arg));
	final String[] lines;
	try {
	    lines = file.read();
	}
	catch(IOException e)
	{
	    area.setName(file.getName());
	    luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return;
	}
	area.setLines(lines);
	area.setName(file.getName());
    }

    void fillProperties(SimpleArea area, EditArea editArea)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(editArea, "editArea");
	area.beginLinesTrans();
	area.clear();
	area.addLine(strings.propertiesFileName() + " " + (file != null?file.file.getAbsolutePath():""));
	area.addLine(strings.propertiesModified() + " " + (modified?strings.propertiesYes():strings.propertiesNo()));
	area.addLine(strings.propertiesCurrentLine() + " " + (editArea.getHotPointY() + 1));
	area.addLine(strings.propertiesLinesTotal() + " " + editArea.getLines().length);
	area.addLine("");
	area.endLinesTrans();
	area.reset(false);
    }
}
