
package org.luwrain.app.notepad;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

public class NarratorApp implements Application
{
    static private final String STRINGS_NAME = "luwrain.narrator";

    private Base base = null;
    private Luwrain luwrain;
    private Strings strings;
    private EditArea editArea;
    private ProgressArea progressArea;
    private AreaLayoutSwitch areaLayoutSwitch;

    private String initialText = null;

    public NarratorApp()
    {
    }

    public NarratorApp(String text)
    {
	NullCheck.notNull(text, "text");
	this.initialText = text;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, STRINGS_NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	areaLayoutSwitch = new AreaLayoutSwitch(luwrain);
	createAreas();
	return new InitResult();
    }

void start()
    {
	//	if (!base.start(progressArea, editArea.getWholeText()))
	//	return;//FIXME:Some error message
	areaLayoutSwitch.show(1);
    }

    private void createAreas()
    {
	//	final Actions a = this;
	final Strings s = strings;

	editArea = new EditArea(new DefaultControlEnvironment(luwrain), strings.appName(), 
				initialText != null?initialText.split("\n", -1):new String[0], null){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
closeApp();
			return true;
		    case OK:
start();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	progressArea = new ProgressArea(new DefaultControlEnvironment(luwrain), strings.appName()){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		private void onProgressLine(String line)
		{
		    if (line == null)
			return;
		    addLine(line);
		}
	    };

	areaLayoutSwitch.add(new AreaLayout(editArea));
	areaLayoutSwitch.add(new AreaLayout(progressArea));
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return areaLayoutSwitch.getCurrentLayout(); 
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}
