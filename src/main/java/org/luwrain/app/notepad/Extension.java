

package org.luwrain.app.notepad;

import java.util.*;

import org.luwrain.core.Application;
import org.luwrain.core.Shortcut;
import org.luwrain.core.Command;
import org.luwrain.core.Luwrain;
import org.luwrain.core.Registry;

class Extension implements org.luwrain.core.Extension
{
    @Override public String init(String[] args, Registry registry)
    {
	return null;
    }

    @Override public Command[] getCommands()
    {
	Command[] res = new Command[1];
	res[0] = new Command(){
		@Override public String getName()
		{
		    return "notepad";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("notepad");
		}
	    };
	return res;
    }

    @Override public Shortcut[] getShortcuts()
    {
	Shortcut[] res = new Shortcut[1];
	res[0] = new Shortcut() {
		@Override public String getName()
		{
		    return "notepad";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length < 1)
		    {
			Application[] res = new Application[1];
			res[0] = new NotepadApp();
			return res;
		    }
		    Vector<Application> v = new Vector<Application>();
		    for(String s: args)
			if (s != null)
			    v.add(new NotepadApp(s));
		    if (v.isEmpty())
		    {
			Application[] res = new Application[1];
			res[0] = new NotepadApp();
			return res;
		    }
		    return v.toArray(new Application[v.size()]);
		}
	    };
	return res;
    }
}
