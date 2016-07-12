/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import org.luwrain.core.*;
import org.luwrain.controls.*;

class Base
{
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private Luwrain luwrain;
    private Strings strings;

    boolean modified = false;
    Path path = null;

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	return true;
    }

    void prepareDocument(String arg, EditArea area)
    {
	NullCheck.notNull(area, "area");
	area.setName(strings.initialTitle());
	if (arg == null || arg.isEmpty())
	    return;
	path = Paths.get(arg);
	if (path == null)
	    return;
	final String[] lines = read(path, DEFAULT_CHARSET);
	area.setName(path.getFileName().toString());
	if (lines != null)
	    area.setLines(lines); else
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
    }

    //For EnvironmentEvent.OPEN:
    boolean open(String fileName, EditArea area)
    {
	NullCheck.notNull(fileName, "fileName");
	NullCheck.notNull(area, "area");
	if (modified || path != null)
	    return false;
	final Path newPath = Paths.get(fileName);
	if (newPath == null || Files.isDirectory(newPath))
	    return false;
	final String[] lines = read(newPath, DEFAULT_CHARSET);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	path = newPath;
	area.setLines(lines);
	area.setName(path.getFileName().toString());
	return true;
    }

    void fillProperties(SimpleArea area, EditArea editArea)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(editArea, "editArea");
	area.beginLinesTrans();
	area.clear();
	area.addLine(strings.propertiesFileName() + " " + (path != null?path.toString():""));
	area.addLine(strings.propertiesModified() + " " + (modified?strings.propertiesYes():strings.propertiesNo()));
	area.addLine(strings.propertiesCurrentLine() + " " + (editArea.getHotPointY() + 1));
	area.addLine(strings.propertiesLinesTotal() + " " + editArea.getLines().length);


	area.addLine("");
	area.endLinesTrans();
	area.reset(false);

    }

    String[] read(Path path, Charset encoding)
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNull(encoding, "encoding");
	try {
	    final byte[] bytes = Files.readAllBytes(path);
	    final CharBuffer charBuf = encoding.decode(ByteBuffer.wrap(bytes));
	    return new String(charBuf.array(), charBuf.arrayOffset(), charBuf.length()).split("\n", -1);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    boolean save(String fileName, String[] lines,
		 Charset charset)
    {
	NullCheck.notNull(fileName, "fileName");
	NullCheck.notNull(lines, "lines");
	NullCheck.notNull(charset, "charset");
	try {
	    final Path path = Paths.get(fileName);
	    final StringBuilder b = new StringBuilder();
	    if (lines.length > 0)
	    {
		b.append(lines[0]);
		for(int i = 1;i < lines.length;++i)
		    b.append("\n" + lines[i]);
	    }
	    final ByteBuffer buf = charset.encode(CharBuffer.wrap(b.toString()));
	    final FileChannel chan = new FileOutputStream(fileName).getChannel();
	    chan.write(buf);
	    chan.close();
	    return true;
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
		return false;
	    }
    }

    void removeBackslashR(EditArea area)
    {
	area.beginLinesTrans();
	for(int i = 0;i < area.getLineCount();++i)
	{
	    final String line = area.getLine(i);
	    if (line == null || line.isEmpty())
		continue;
	    final StringBuilder b = new StringBuilder();
	    for(int k = 0;k < line.length();++k)
		if (line.charAt(k) != '\r')
		    b.append(line.charAt(k));
	    area.setLine(i, b.toString());
	}
	area.endLinesTrans();
    }

    void addBackslashR(EditArea area)
    {
	area.beginLinesTrans();
	for(int i = 0;i < area.getLineCount();++i)
	{
	    final String line = area.getLine(i);
	    if (line == null)
		continue;
	    area.setLine(i, line + '\r');
	}
	area.endLinesTrans();
    }
}
