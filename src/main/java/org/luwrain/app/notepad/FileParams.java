/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.util.*;

class FileParams
{
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    final File file;
    String lineSeparator = System.lineSeparator();
    Charset charset = DEFAULT_CHARSET;

    FileParams(File file)
    {
	NullCheck.notNull(file, "file");
	this.file = file;
    }

    String getName()
    {
	return file.getName();
    }

    String[] read() throws IOException
    {
	return FileUtils.readTextFileMultipleStrings(file, charset.toString(), lineSeparator);
    }

void save(String[] lines) throws IOException
    {
	NullCheck.notNullItems(lines, "lines");
	FileUtils.writeTextFileMultipleStrings(file, lines, charset.toString(), lineSeparator);
    }


}
