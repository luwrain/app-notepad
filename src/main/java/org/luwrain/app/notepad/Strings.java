/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
    String errorOpeningFile();
    String errorSavingFile();
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
    String actionTitle(String name);
    String infoAreaName();

    String propertiesFileName();
    String propertiesModified();
    String propertiesYes();
    String propertiesNo();
    String propertiesCurrentLine();
    String propertiesLinesTotal();
    String enteredPathMayNotBeDir(String fileName);
}
