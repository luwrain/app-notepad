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
import org.luwrain.core.NullCheck;

class Base
{
    String[] read(String fileName, Charset encoding)
    {
	NullCheck.notNull(fileName, "fileName");
	NullCheck.notNull(encoding, "encoding");
	try {
	    Path path = Paths.get(fileName);
	    final byte[] bytes = Files.readAllBytes(path);
	    final CharBuffer charBuf = encoding.decode(ByteBuffer.wrap(bytes));
	    return new String(charBuf.array()).split("\n", -1);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    boolean save(String fileName, 
			String[] lines,
			Charset encoding)
    {
	if (fileName == null || fileName.isEmpty())
	    return false;
	try {
	    saveTextFile(fileName, lines, encoding);
	    return true;
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
		return false;
	    }
    }

    private void saveTextFile(String fileName,
			       String[] lines,
			       Charset encoding) throws IOException
    {
	Path path = Paths.get(fileName);
	try (BufferedWriter writer = Files.newBufferedWriter(path, encoding))
	{
	    for(int i = 0;i < lines.length;i++)
	    {
		writer.write(lines[i]);
		if (i + 1 < lines.length)
		    writer.newLine();
	    }
	}
    }
}
