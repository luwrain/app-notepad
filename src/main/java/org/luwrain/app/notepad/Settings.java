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

import org.luwrain.core.*;

interface Settings
{
    static final String PATH = "/org/luwrain/app/notepad";

    int getAligningLineLen(int defValue);
    void setAligningLineLen(int value);
    String getLameCommand(String defValue);
    void setLameCommand(String command);

    static Settings create(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	return 	RegistryProxy.create(registry, PATH, Settings.class);
    }
}
