
package org.luwrain.app.notepad;

import java.util.concurrent.atomic.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

final class Hooks
{
    private final App app;

    Hooks(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    boolean runActionHooks(EnvironmentEvent event, EditArea editArea, MultilineEdit.Model model, AbstractRegionPoint regionPoint)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(editArea, "editArea");
	NullCheck.notNull(model, "model");
	NullCheck.notNull(regionPoint, "regionPoint");
	if (!(event instanceof ActionEvent))
	    return false;
	final ActionEvent actionEvent = (ActionEvent)event;
	final MultilineEditCorrector corrector = (MultilineEditCorrector)model;
	final AtomicBoolean res = new AtomicBoolean(false);
	corrector.doEditAction((lines, hotPoint)->{
		try {
		    res.set(app.getLuwrain().xRunHooks("luwrain.notepad.action", new Object[]{
				actionEvent.getActionName(),
				org.luwrain.script.TextScriptUtils.createTextEditHookObject(editArea, lines, hotPoint, regionPoint)
			    }, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY));
		}
		catch(RuntimeException e)
		{
		    app.getLuwrain().crash(e);
		}
	    });
	return res.get();
    }
}
