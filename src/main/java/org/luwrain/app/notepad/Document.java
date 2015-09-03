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

import java.io.*;
import java.nio.charset.Charset;

class Document
{
    public File file;
    public boolean modified = false;
    public boolean defaultDoc = false;
    public Charset charset;

    public Document(File file,
		    Charset charset,
boolean defaultDoc)
    {
	this.file = file;
	this.modified = false;
	this.defaultDoc = defaultDoc;
	this.charset = charset;
	if (file == null)
	    throw new NullPointerException("file may not be null");
	if (charset == null)
	    throw new NullPointerException("charset may not be null");
    }
}
