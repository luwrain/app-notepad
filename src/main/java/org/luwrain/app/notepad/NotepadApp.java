/*
   Copyright 2012-2014 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.notepad;


import java.io.*;
import java.nio.charset.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

public class NotepadApp implements Application, Actions
{
    static private final Charset ENCODING = StandardCharsets.UTF_8;

    private Luwrain luwrain;
    private Base base = new Base();
    private Strings strings;
    private EditArea area;
    private String fileName = "";
    private boolean modified = false; 

    public NotepadApp()
    {
    }

    public NotepadApp(String arg)
    {
	this.fileName = arg;
    }

    public boolean onLaunch(Luwrain luwrain)
    {
	Object o = Langs.requestStringConstructor("notepad");
	if (o == null)
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	createArea();
	if (fileName != null && !fileName.isEmpty())
	{
	    String[] lines = base.read(fileName, ENCODING);

	    File f = new File(fileName);
	    area.setName(f.getName());
	    if (lines == null)
		luwrain.message(strings.errorOpeningFile()); else 
		area.setContent(lines);
	} else
	    area.setName(strings.newFileName());
	return true;
    }

    public boolean save()
    {
	if (!modified)
	{
	    luwrain.message(strings.noModificationsToSave());
	    return true;
	}
	if (fileName == null || fileName.isEmpty())
	{
	    String newFileName = askFileNameToSave();
	    if (newFileName == null || newFileName.isEmpty())
		return false;
	    fileName = newFileName;
	}
	if (area.getContent() != null)
	    if (base.save(fileName, area.getContent(), ENCODING))
	    {
		modified = false;
		luwrain.message(strings.fileIsSaved());
		return true;
	    }
	luwrain.message(strings.errorSavingFile());
	return false;
    }

    public void open()
    {
	if (!checkIfUnsaved())
	    return;
	File dir = null;
	if (fileName == null || fileName.isEmpty())
	{
	    dir = luwrain.launchContext().userHomeDirAsFile();
	} else
	{
	    File f = new File(fileName);
	    dir = f.getParentFile();
	}
	File chosenFile = luwrain.openPopup(null, null, dir);
	if (chosenFile == null)
	    return;
	String[] lines = base.read(chosenFile.getAbsolutePath(), ENCODING);
	if (lines == null)
	{
	    luwrain.message(strings.errorOpeningFile());
	    return;
	}
	area.setContent(lines);
	    fileName = chosenFile.getAbsolutePath();
	    area.setName(chosenFile.getName());
    }

    public void markAsModified()
    {
	modified = true;
    }

    private void createArea()
    {
	final Actions a = this;
	area = new EditArea(new DefaultControlEnvironment(luwrain), fileName){
		private Actions actions = a;
		public void onChange()
		{
		    actions.markAsModified();
		}
		public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.close();
			return true;
		    case EnvironmentEvent.INTRODUCE:
			luwrain.say(strings.introduction() + " " + getName()); 
			return true;
		    case EnvironmentEvent.SAVE:
			actions.save();
			return true;
		    case EnvironmentEvent.OPEN:
			actions.open();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
    }

    public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    public void close()
    {
	if (!checkIfUnsaved())
	    return;
	luwrain.closeApp();
    }

    //Returns true if there are no more modification user wants to save;
    private boolean checkIfUnsaved()
    {
	if (!modified)
	    return true;
	YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), false);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return false;
	if ( popup.getResult() && !save())
	    return false;
	modified = false;
	return true;
    }

    //null means user cancelled file name popup
    private String askFileNameToSave()
    {
	final File dir = luwrain.launchContext().userHomeDirAsFile();
	final File chosenFile = luwrain.openPopup(strings.savePopupName(),
						  strings.savePopupPrefix(),
						  new File(dir, strings.newFileName()));
	if (chosenFile == null)
	    return null;
	//FIXME:Is a valid file;
	return chosenFile.getAbsolutePath();
    }
}
