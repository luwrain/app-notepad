
package org.luwrain.app.notepad;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Appearance extends EditUtils2.DefaultEditAreaAppearance
{
    Appearance(ControlContext context)
    {
	super(context);
    }
    
    @Override public void announceLine(int index, String line)
    {
    }
}
