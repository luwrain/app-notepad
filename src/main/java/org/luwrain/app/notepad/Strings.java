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

public interface Strings
{
    static final String NAME = "luwrain.notepad";

    String actionCharset();
    String actionIndents();
    String actionNarrating();
    String actionNoIndents();
    String actionOpen();
    String actionReplace();
    String actionSave();
    String actionSaveAs();
    String appName();
    String charsetPopupName();
    String charsetPopupPrefix();
    String enteredPathMayNotBeDir(String fileName);
    String errorLoadingSpeechChannel(String message);
    String errorOpeningFile(String comment);
    String errorSavingFile(String comment);
    String fileIsSaved();
    String initialTitle();
    String modeNatural();
    String modeNone();
    String modeProgramming();
    String narratingAreaName();
    String narratingCancelled();
    String narratingDestDirPopupName();
    String narratingDestDirPopupPrefix();
    String narratingDone();
    String narratingNoSupportedAudioFormats();
    String narratingFileWritten(String file);
    String narratingProgress(String status);
    String noChannelToSynth(String channelName);
    String noModificationsToSave();
    String noTextToSynth();
    String openPopupName();
    String openPopupPrefix();
    String propsAreaName();
    String saveChangesPopupName();
    String saveChangesPopupQuestion();
    String savePopupName();
        String savePopupPrefix();
    String settingsFormFileLenMayNotBeNegative();
    String settingsFormFileLenIsNotInteger();
    String settingsFormLameCommand();
    String settingsFormName();
    String settingsFormNarratedFileLen();
    String settingsFormNarratingChannelName();
    String settingsFormNarratingChannelParams();
    String settingsFormNarratingPauseDuration();
    String settingsFormNarratingSpeechPitch();
    String settingsFormNarratingSpeechRate();
}
