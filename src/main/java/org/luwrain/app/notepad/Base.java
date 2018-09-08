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

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

import org.apache.commons.io.*;

import org.luwrain.core.*;
import org.luwrain.speech.*;
import org.luwrain.controls.*;

final class Base
{
    static final int DEFAULT_ALIGNING_LINE_LEN = 60;

    private final Luwrain luwrain;
    private final Strings strings;

    final EditCorrectorWrapper editCorrectorWrapper = new EditCorrectorWrapper();

    boolean modified = false;
    FileParams file = null;
    Luwrain.SpokenTextType spokenTextType = Luwrain.SpokenTextType.NONE;
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
