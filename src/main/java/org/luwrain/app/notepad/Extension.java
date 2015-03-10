

package org.luwrain.app.notepad;

import java.util.*;

import org.luwrain.core.Application;
import org.luwrain.core.Shortcut;
import org.luwrain.core.Command;
import org.luwrain.core.CommandEnvironment;
import org.luwrain.core.Worker;
import org.luwrain.core.SharedObject;
import org.luwrain.core.I18nExtension;
import org.luwrain.core.Luwrain;
import org.luwrain.core.Registry;

public class Extension implements org.luwrain.core.Extension
{
    @Override public String init(String[] args, Registry registry)
    {
	return null;
    }

    @Override public Command[] getCommands(CommandEnvironment env)
    {
	Command[] res = new Command[1];
	res[0] = new Command(){
		@Override public String getName()
		{
		    return "notepad";
		}
		@Override public void onCommand(CommandEnvironment env)
		{
		    env.launchApp("notepad");
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

    @Override public Worker[] getWorkers()
    {
	return new Worker[0];
    }

    @Override public SharedObject[] getSharedObjects()
    {
	return new SharedObject[0];
    }

    @Override public void i18nExtension(I18nExtension i18nExt)
    {
	i18nExt.addCommandTitle("en", "notepad", "Notepad");
	i18nExt.addCommandTitle("ru", "notepad", "Блокнот");
	i18nExt.addStrings("ru", NotepadApp.STRINGS_NAME, new org.luwrain.app.notepad.i18n.Ru());
    }

    @Override public org.luwrain.mainmenu.Item[] getMainMenuItems(CommandEnvironment env)
    {
	return new org.luwrain.mainmenu.Item[0];
    }

    @Override public void close()
    {
	//Nothing here;
    }
}
