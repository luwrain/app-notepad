
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
