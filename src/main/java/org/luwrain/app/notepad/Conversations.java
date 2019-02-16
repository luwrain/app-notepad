
package org.luwrain.app.notepad;

import java.io.*;
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
	    luwrain.message(strings.invalidCharset(), Luwrain.MessageType.ERROR);
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

    void openAs()
    {
	final CustomFilePopup popup = new CustomFilePopup(luwrain, strings, null, "Открыть файл", "Имя файла:", luwrain.getFileProperty("luwrain.dir.userhome"));
	luwrain.popup(popup);
    }
}
