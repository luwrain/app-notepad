/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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

import static org.luwrain.popups.Popups.*;

final class Conversations
{
    static final String
	charsets = "UTF-8:KOI8-R:windows-1251:IBM866:ISO-8859-5";

    enum UnsavedChangesRes {CONTINUE_SAVE, CONTINUE_UNSAVED, CANCEL};

    private final Luwrain luwrain;
    private final Strings strings;

    private final Set<String>
	replaceExpHistory = new HashSet<>(),
	replaceWithHistory = new HashSet<>();

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    //currentFile may be null
    File save(File currentFile)
    {
	return path(luwrain, 
		    strings.savePopupName(), strings.savePopupPrefix(),
		    currentFile, //It's OK, if this value is null
		    (fileToCheck, announce)->{
			if (fileToCheck.exists() && fileToCheck.isDirectory())
			{
			    if (announce)
				luwrain.message(strings.enteredPathMayNotBeDir(fileToCheck.getAbsolutePath()), Luwrain.MessageType.ERROR);
			    return false;
			}
			return true;
		    });
    }

    File open()
    {
	return existingFile(luwrain, strings.openPopupName());
    }

    File narratingDestDir()
    {
	return existingDir(luwrain, strings.narratingDestDirPopupPrefix());
    }

    String charset()
    {
	final String[] names = charsets.split(":", -1);
	final Object res = fixedList(luwrain, strings.charsetPopupPrefix(), names);
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

    String replaceExp()
    {
	return Popups.editWithHistory(luwrain, strings.replacePopupName(), strings.replaceExpPopupPrefix(), "", replaceExpHistory);
    }

        String replaceWith()
    {
	return Popups.editWithHistory(luwrain, strings.replacePopupName(), strings.replaceWithPopupPrefix(), "", replaceWithHistory);
    }

    String correctionSuggestion(String[] options)
    {
final Object res = Popups.fixedList(luwrain, strings.correctionSuggestionsPopupName(), options);
return res != null?res.toString():null;
    }

}
