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

    private boolean modified = false;
    File file = null;

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
	file = new File(arg);
	if (file == null)
	    return;
	final String[] lines;
	try {
	    lines = read(file.toPath(), DEFAULT_CHARSET);
	}
	catch(IOException e)
	{
	    area.setName(file.getName());
	    luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MESSAGE_ERROR);
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
	area.addLine(strings.propertiesFileName() + " " + (file != null?file.getAbsolutePath():""));
	area.addLine(strings.propertiesModified() + " " + (modified?strings.propertiesYes():strings.propertiesNo()));
	area.addLine(strings.propertiesCurrentLine() + " " + (editArea.getHotPointY() + 1));
	area.addLine(strings.propertiesLinesTotal() + " " + editArea.getLines().length);
	area.addLine("");
	area.endLinesTrans();
	area.reset(false);
    }

    void markNoModifications()
    {
	modified = false;
    }

    void markAsModified()
    {
	modified = true;
    }

    boolean isModified()
    {
 return modified;
 }

    static String[] read2(FileParams fileParams) throws IOException
    {
	NullCheck.notNull(fileParams, "fileParams");
	final InputStream is = new FileInputStream(fileParams.file);
	try {
	    final byte[] bytes = IOUtils.toByteArray(is);
	    if (bytes.length == 0)
		return new String[0];
	    final String text = new String(bytes, fileParams.charset);
	    if (text.isEmpty())
		return new String[0];
	    return text.split(fileParams.lineSeparator, -1);
	}
	finally {
	    is.close();
	}
	}



    static String[] read(Path path, Charset encoding) throws IOException
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNull(encoding, "encoding");
	    final byte[] bytes = Files.readAllBytes(path);
	    final CharBuffer charBuf = encoding.decode(ByteBuffer.wrap(bytes));
	    return new String(charBuf.array(), charBuf.arrayOffset(), charBuf.length()).split("\n", -1);
    }

    static void save(Path path, String[] lines, Charset charset) throws IOException
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNullItems(lines, "lines");
	NullCheck.notNull(charset, "charset");
	final StringBuilder b = new StringBuilder();
	if (lines.length > 0)
	{
	    b.append(lines[0]);
	    for(int i = 1;i < lines.length;++i)
		b.append("\n" + lines[i]);
	}
	final ByteBuffer buf = charset.encode(CharBuffer.wrap(b.toString()));
	final FileChannel chan = new FileOutputStream(path.toString()).getChannel();
	try {
	chan.write(buf);
	}
	finally {
	chan.close();
	}
    }
}
