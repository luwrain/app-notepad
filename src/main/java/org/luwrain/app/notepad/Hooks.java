
package org.luwrain.app.notepad;

import java.util.concurrent.atomic.*;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.script.*;

final class Hooks
{
    static private final String PROPERTIES_HOOK = "luwrain.notepad.properties";

    private final App app;

    Hooks(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    boolean runActionHooks(EnvironmentEvent event, EditArea editArea)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(editArea, "editArea");
	if (!(event instanceof ActionEvent))
	    return false;
	final ActionEvent actionEvent = (ActionEvent)event;
	final MultilineEditCorrector corrector = (MultilineEditCorrector)editArea.getEdit().getMultilineEditModel();
	final AtomicBoolean res = new AtomicBoolean(false);
	corrector.doEditAction((lines, hotPoint)->{
		try {
		    res.set(app.getLuwrain().xRunHooks("luwrain.notepad.action", new Object[]{
				actionEvent.getActionName(),
				org.luwrain.script.TextScriptUtils.createTextEditHookObject(editArea, lines, hotPoint, editArea.getRegionPoint())
			    }, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY));
		}
		catch(RuntimeException e)
		{
		    app.getLuwrain().crash(e);
		}
	    });
	return res.get();
    }

    String[] runPropertiesHook(EditArea editArea)
    {
	NullCheck.notNull(editArea, "editArea");
		final EmptyHookObject hookObj = new EmptyHookObject(){
		@Override public Object getMember(String name)
		{
		    NullCheck.notEmpty(name, "name");
		    switch(name)
		    {
		    case "lines":
			return ScriptUtils.createReadOnlyArray(editArea.getLines());
		    case "fileName":
			if (app.file == null)
			    return "";
			return app.file.getAbsolutePath();
		    case "charset":
			return app.charset;
		    case "hotPoint":
			return new HotPointHookObject(editArea);
		    default:
			return super.getMember(name);
		    }
		}
	    };
	final List<String> res = new LinkedList();
	try {
	    final Object o = new org.luwrain.script.hooks.ProviderHook(app.getLuwrain()).run(PROPERTIES_HOOK, new Object[]{hookObj});
	    if (o != null)
	    {
		final List r = ScriptUtils.getArray(o);
		for(Object i: r)
		{
		    final String s = ScriptUtils.getStringValue(i);
		    if (s != null && !s.trim().isEmpty())
			res.add(s);
		}
	    }
	}
	catch(RuntimeException e)
	{
	    app.getLuwrain().crash(e);
	    return new String[0];
	}
	return res.toArray(new String[res.size()]);
    }
}
