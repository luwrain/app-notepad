/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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
import java.util.concurrent.*;

import org.apache.commons.io.*;

import org.luwrain.core.*;
import org.luwrain.speech.*;
import org.luwrain.controls.*;

final class Base
{
    enum Mode {
	NONE,
	NATURAL,
	PROGRAMMING
    };

    final Luwrain luwrain;
    final Strings strings;

    boolean modified = false;
    String charset = "UTF-8";
    FileParams file = null;
    Mode mode = Mode.NONE;
    boolean speakIndent = false;

//for narrating
    FutureTask narratingTask = null; 
    Narrating narrating = null;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }
}
