/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

public interface Strings
{
    static final String NAME = "luwrain.notepad";

    String appName();
    String actionOpenAs();
    String actionSave();
    String actionSaveAs();
    String charsetPopupName();
    String charsetPopupPrefix();
    String compressing(String file);
    String done();
    String enteredPathMayNotBeDir(String fileName);
    String errorOpeningFile(String comment);
    String errorSavingFile(String comment);
    String fileIsSaved();
    String initialTitle();
    String invalidCharset();
    String narratingNoSupportedAudioFormats();
    String noTextToSynth();
    String noChannelToSynth();
    String noModificationsToSave();
    String propertiesCurrentLine();
    String propertiesFileName();
    String propertiesLinesTotal();
    String propertiesModified();
    String propertiesYes();
    String propertiesNo();
    String targetDirPopupName();
    String targetDirPopupPrefix();
    String saveChangesPopupName();
    String saveChangesPopupQuestion();
    String savePopupName();
    String savePopupPrefix();
    String settingsFormName();
    String settingsFormAligningLineLen();
    String settingsFormLameCommand();
}
