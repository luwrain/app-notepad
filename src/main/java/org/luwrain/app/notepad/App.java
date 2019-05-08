
package org.luwrain.app.notepad;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

class App implements Application
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;

    private EditArea editArea = null;
    private AreaLayoutHelper layout = null;

    private final String arg;

    App()
    {
	arg = null;
    }

    App(String arg)
    {
	NullCheck.notNull(arg, "arg");
	this.arg = arg;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actions = new Actions(luwrain, strings, base);
	this.actionLists = new ActionLists(luwrain, strings, base);
	createArea();
	this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
	    }, editArea);
	if (arg != null && !arg.isEmpty())
	{
	    base.file = new FileParams(new File(arg));
	    try {
		final String[] lines = base.file.read();
		editArea.setLines(lines);
	    }
	    catch(IOException e)
	    {
		luwrain.message(strings.errorOpeningFile(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    }
	}
	return new InitResult();
    }

    private void createArea()
    {
	final EditArea.Params params = new EditArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.name = "";
	/*
	params.correctorFactory = (corrector)->{
	    NullCheck.notNull(corrector, "corrector");
	    base.editCorrectorWrapper.setWrappedCorrector(corrector);
	    return base.editCorrectorWrapper;
	};
	*/
	params.changeListener = ()->{base.modified = true;};
	
	
	editArea = new EditArea(params) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case ESCAPE:
			closeApp();
			return true;
		    }
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case SAVE:
			return actions.onSave( editArea);
		    case PROPERTIES:
			return showProps();
		    case ACTION:
			if (ActionEvent.isAction(event, "save"))
			    return actions.onSave(this);
			if (ActionEvent.isAction(event, "save-as"))
			{
			    actions.onSaveAs(this);
			    return true;
			    			}
			if (ActionEvent.isAction(event, "open-as"))
			    return actions.openAs();
			/*
						if (ActionEvent.isAction(event, "run"))
			    return actions.run(editArea);
			*/

												if (ActionEvent.isAction(event, "spoken-text-none"))
												{
												    luwrain.speak("none");
									    return true;
												}

																								if (ActionEvent.isAction(event, "spoken-text-natural"))
												{
												    luwrain.speak("natural");
									    return true;
												}

																																																if (ActionEvent.isAction(event, "spoken-text-programming"))
												{
												    luwrain.speak("programming");
									    return true;
												}


																								
						return false;

						
								    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    if (base.file == null)
			return strings.initialTitle();
		    return base.file.getName();
		}
		@Override public void announceLine(int index, String line)
		{
		    NullCheck.notNull(line, "line");
		    if (line.trim().isEmpty())
		    {
			luwrain.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
			return;
			}
		    luwrain.setEventResponse(DefaultEventResponse.text(luwrain.getSpokenText(line, base.spokenTextType)));
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getActions();
		}
	    };
    }

    private boolean showProps()
    {
	final SimpleArea propsArea = new SimpleArea(new DefaultControlContext(luwrain), "Информация") {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	propsArea.beginLinesTrans();
	propsArea.addLine(strings.propertiesFileName() + " " + (base.file != null?base.file.file.getAbsolutePath():""));
	propsArea.addLine(strings.propertiesModified() + " " + (base.modified?strings.propertiesYes():strings.propertiesNo()));
	propsArea.addLine(strings.propertiesCurrentLine() + " " + (editArea.getHotPointY() + 1));
	propsArea.addLine(strings.propertiesLinesTotal() + " " + editArea.getLines().length);
	propsArea.addLine("");
	propsArea.endLinesTrans();
	layout.openTempArea(propsArea);
	return true;
    }

    //Returns true, if there are no more modification which the user would like to save
    private boolean noUnsavedData()
    {
	if (!base.modified)
	    return true;
	switch(actions.conv.unsavedChanges())
	{
	case CONTINUE_SAVE:
	    return actions.onSave(editArea);
	case CONTINUE_UNSAVED:
	    return true;
	case CANCEL:
	    return false;
	}
	return false;
    }

    @Override public void closeApp()
    {
	if (!noUnsavedData())
	    return;
	luwrain.closeApp();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
    }

    @Override public String getAppName()
    {
	return base.file == null?strings.appName():base.file.getName();
    }
}
