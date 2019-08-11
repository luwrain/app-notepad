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
    static private final String NATURAL_MODE_CORRECTOR_HOOK = "luwrain.notepad.mode.natural";
        static private final String PROGRAMMING_MODE_CORRECTOR_HOOK = "luwrain.notepad.mode.programming";
    
    enum Mode {
	NONE,
	NATURAL,
	PROGRAMMING
    };

    final Luwrain luwrain;
    final Strings strings;

        File file = null;
    boolean modified = false;
    String charset = "UTF-8";
    String lineSeparator = System.lineSeparator();
    Mode mode = Mode.NONE;
    boolean speakIndent = false;

    final EditUtils2.ActiveCorrector corrector;

//for narrating
    FutureTask narratingTask = null; 
    Narrating narrating = null;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.corrector = new EditUtils2.ActiveCorrector();
    }

    void activateMode(Mode mode)
    {
	NullCheck.notNull(mode, "mode");
	switch(mode)
	{
	case NATURAL:
	    corrector.setActivatedCorrector(new DirectScriptMultilineEditCorrector(new DefaultControlContext(luwrain), corrector.getWrappedCorrector(), NATURAL_MODE_CORRECTOR_HOOK));
	    break;
	case PROGRAMMING:
	    corrector.setActivatedCorrector(new DirectScriptMultilineEditCorrector(new DefaultControlContext(luwrain), corrector.getWrappedCorrector(), PROGRAMMING_MODE_CORRECTOR_HOOK));
	    break;
	}
    }

    EditArea2.Params createEditParams()
    {
		final EditArea2.Params params = new EditArea2.Params();
	params.context = new DefaultControlContext(luwrain);
	params.name = "";
	params.appearance = new Appearance(params.context);
	params.changeListener = ()->{modified = true;};
	params.editFactory = (p, c)->{
	    final MultilineEdit2.Params pp = new MultilineEdit2.Params();
	    pp.context = p.context;
	    	    	    corrector.setWrappedCorrector(c);
	    pp.model = corrector;
	    pp.regionPoint = p.regionPoint;
	    pp.appearance = p.appearance;
	    return new MultilineEdit2(pp);
	};
	return params;
    }

        String[] read() throws IOException
    {
	final String text = org.luwrain.util.FileUtils.readTextFileSingleString(file, charset);
	return org.luwrain.util.FileUtils.universalLineSplitting(text);
    }

    void save(String[] lines) throws IOException
    {
	NullCheck.notNullItems(lines, "lines");
	org.luwrain.util.FileUtils.writeTextFileMultipleStrings(file, lines, charset, lineSeparator);
    }

    
}
