/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.notepad;

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

final class Conversations
{
    static final String charsets = "UTF-8:KOI8-R:windows-1251:IBM866:ISO-8859-5:";
    enum UnsavedChangesRes {CONTINUE_SAVE, CONTINUE_UNSAVED, CANCEL};

    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    //currentFile may be null
File save(File currentFile)
    {
	return Popups.path(luwrain, 
			   strings.savePopupName(),
			   strings.savePopupPrefix(),
			   currentFile != null?currentFile:luwrain.getFileProperty("luwrain.dir.userhome"),
			   /*			   luwrain.getFileProperty("luwrain.dir.userhome"),*/
			   (fileToCheck, announce)->{
			       if (fileToCheck.isDirectory())
			       {
				   if (announce)
				   luwrain.message(strings.enteredPathMayNotBeDir(fileToCheck.getAbsolutePath()), Luwrain.MessageType.ERROR);
				   return false;
			       }
			       return true;
			   });
    }

    String charset()
    {
	final String[] names = charsets.split(":", -1);
	final Object res = Popups.fixedList(luwrain, strings.charsetPopupPrefix(), names);
	if (res == null)
	    return null;
	return res.toString();
    }

    boolean rereadWithNewCharser(File file)
    {
	NullCheck.notNull(file, "file");
	return Popups.confirmDefaultYes(luwrain, "Новая кодировка", "Перечитать файл \"" + file.getAbsolutePath() + "\" с новой кодировкой?");
    }

    UnsavedChangesRes unsavedChanges()
    {
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), true, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return UnsavedChangesRes.CANCEL;
	return popup.result()?UnsavedChangesRes.CONTINUE_SAVE:UnsavedChangesRes.CONTINUE_UNSAVED;
    }

    File open()
    {
	return Popups.path(luwrain,
			   strings.openPopupName(), strings.openPopupPrefix(),
			   (file, announce)->{
			       if (file.exists() && file.isDirectory())
			       {
				   if (announce)
				       luwrain.message(strings.enteredPathMayNotBeDir(file.getAbsolutePath()), Luwrain.MessageType.ERROR);
				   return false;
			       }
			       return true;
			   });
    }

    File narratingDestDir()
    {
	return Popups.existingDir(luwrain, strings.narratingDestDirPopupName(), strings.narratingDestDirPopupPrefix());
    }
}
