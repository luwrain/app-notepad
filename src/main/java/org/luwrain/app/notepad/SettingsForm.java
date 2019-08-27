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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.cpanel.*;

final class SettingsForm extends FormArea implements SectionArea
{
    private ControlPanel controlPanel;
    private Luwrain luwrain;
    private Settings sett;
    private Strings strings;

    SettingsForm(ControlPanel controlPanel, Strings strings)
    {
	super(new DefaultControlContext(controlPanel.getCoreInterface()), strings.settingsFormName());
	this.controlPanel = controlPanel;
	this.luwrain = controlPanel.getCoreInterface();;
	this.strings = strings;
	this.sett = Settings.create(luwrain.getRegistry());
	fillForm();
    }

    private void fillForm()
    {
		addEdit("narrating-channel-name", strings.settingsFormNarratingChannelName(), sett.getNarratingChannelName(""));
				addEdit("narrating-channel-params", strings.settingsFormNarratingChannelParams(), sett.getNarratingChannelParams(""));
    }

        @Override public boolean saveSectionData()
    {
	sett.setNarratingChannelName(getEnteredText("narrating-channel-name"));
		sett.setNarratingChannelParams(getEnteredText("narrating-channel-params"));
	return true;
    }

    @Override public boolean onInputEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (controlPanel.onInputEvent(event))
	    return true;
	return super.onInputEvent(event);
    }

    @Override public boolean onSystemEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (controlPanel.onSystemEvent(event))
	    return true;
	return super.onSystemEvent(event);
    }


    static SettingsForm create(ControlPanel controlPanel)
    {
	NullCheck.notNull(controlPanel, "controlPanel");
	final Strings strings = (Strings)controlPanel.getCoreInterface().i18n().getStrings(Strings.NAME);
	return new SettingsForm(controlPanel, strings);
    }
}
