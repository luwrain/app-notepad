

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

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
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

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
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
		    LinkedList<Application> v = new LinkedList<Application>();
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

    @Override public void i18nExtension(Luwrain luwrain, I18nExtension i18nExt)
    {
	i18nExt.addCommandTitle("en", "notepad", "Notepad");
	i18nExt.addCommandTitle("ru", "notepad", "Блокнот");
	i18nExt.addStrings("ru", NotepadApp.STRINGS_NAME, new org.luwrain.app.notepad.i18n.Ru());
    }
}
