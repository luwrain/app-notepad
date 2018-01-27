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
    String initialTitle();
    String errorOpeningFile(String comment);
    String errorSavingFile(String comment);
    String fileIsSaved();
    String savePopupName();
    String savePopupPrefix();
    String saveChangesPopupName();
    String saveChangesPopupQuestion();
    String noModificationsToSave();
    String charsetPopupName();
    String charsetPopupPrefix();
    String invalidCharset();
    String rereadAnotherCharsetPopupName();
    String rereadAnotherCharsetPopupQuestion();
    String actionSave();
    String actionSaveAs();
    String actionOpenAnotherCharset();
    String actionSaveAnotherCharset();
    String actionRemoveBackslashR();
    String actionAddBackslashR();
    String actionInfo();
    String infoAreaName();

    String propertiesFileName();
    String propertiesModified();
    String propertiesYes();
    String propertiesNo();
    String propertiesCurrentLine();
    String propertiesLinesTotal();
    String enteredPathMayNotBeDir(String fileName);

    String noTextToSynth();
    String noChannelToSynth();
    String targetDirPopupName();
    String targetDirPopupPrefix();
    String narratingNoSupportedAudioFormats();
    String done();
    String compressing(String file);
    String settingsFormName();
    String settingsFormLameCommand();
}
