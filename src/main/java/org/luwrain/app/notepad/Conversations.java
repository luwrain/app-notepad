/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import java.util.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

class Conversations
{
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets(); 

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

    Charset charsetPopup()
    {
	final LinkedList<String> names = new LinkedList<String>();
	for(Map.Entry<String, Charset>  ent: AVAILABLE_CHARSETS.entrySet())
	    names.add(ent.getKey());
	final EditListPopup popup = new EditListPopup(luwrain,
						      new EditListPopupUtils.FixedModel(names.toArray(new String[names.size()])),
						      strings.charsetPopupName(), strings.charsetPopupPrefix(),
						      "", Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	final String text = popup.text().trim();
	if (text == null || text.isEmpty() || !AVAILABLE_CHARSETS.containsKey(text))
	{
	    luwrain.message(strings.invalidCharset(), Luwrain.MESSAGE_ERROR);
	    return null;
	}
	return AVAILABLE_CHARSETS.get(text);
    }

    UnsavedChangesRes unsavedChanges()
    {
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.saveChangesPopupName(), strings.saveChangesPopupQuestion(), true, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return UnsavedChangesRes.CANCEL;
	return popup.result()?UnsavedChangesRes.CONTINUE_SAVE:UnsavedChangesRes.CONTINUE_UNSAVED;
    }
}
