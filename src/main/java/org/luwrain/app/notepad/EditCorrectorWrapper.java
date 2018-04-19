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

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class EditCorrectorWrapper implements MultilineEditCorrector
{
    static private final int ALIGNING_LINE_LEN = 60;

    private MultilineEditCorrector wrappedModel = null;

    private void alignParagraph(int pos, int lineIndex)
    {
	/*
	//Doing nothing on empty line
	if (getLine(lineIndex).trim().isEmpty())
	    return;
	//Searching paragraph bounds
	int paraBegin = lineIndex;
	int paraEnd = lineIndex;
	while (paraBegin > 0 && !getLine(paraBegin).trim().isEmpty())
	    	    --paraBegin;
	if (getLine(paraBegin).trim().isEmpty())
	    ++paraBegin;
		while (paraEnd < getLineCount() && !getLine(paraEnd).trim().isEmpty())
	    ++paraEnd;
		int startingLine = 0;
		for(startingLine = paraBegin;startingLine < paraEnd;++startingLine)
		    if (getLine(startingLine).length() > ALIGNING_MAX_LEN)
			break;
		//Stopping, if there are no long lines
		if (startingLine == paraEnd)
		    return;
	*/
    }

    void setWrappedCorrector(MultilineEditCorrector corrector)
    {
	NullCheck.notNull(corrector, "corrector");
	this.wrappedModel = corrector;
    }

    MultilineEditCorrector getWrappedCorrector()
    {
	return wrappedModel;
    }

    @Override public int getLineCount()
    {
	return wrappedModel.getLineCount();
    }

    @Override public String getLine(int index)
    {
	return wrappedModel.getLine(index);
    }

    @Override public int getHotPointX()
    {
	return wrappedModel.getHotPointX();
    }

    @Override public int getHotPointY()
    {
	return wrappedModel.getHotPointY();
    }

    @Override public String getTabSeq()
    {
	return wrappedModel.getTabSeq();
    }

    @Override public char deleteChar(int pos, int lineIndex)
    {
	return wrappedModel.deleteChar(pos, lineIndex);
    }

    @Override public boolean deleteRegion(int fromX, int fromY, int toX, int toY)
    {
	return wrappedModel.deleteRegion(fromX, fromY, toX, toY);
    }

    @Override public boolean insertRegion(int x, int y, String[] lines)
    {
	NullCheck.notNullItems(lines, "lines");
	return wrappedModel.insertRegion(x, y, lines);
    }

    @Override public void insertChars(int pos, int lineIndex, String str)
    {
	NullCheck.notNull(str, "str");
		    wrappedModel.insertChars(pos, lineIndex, str);
	if (str.equals(" "))
	    alignParagraph(pos, lineIndex);
    }

    @Override public void mergeLines(int firstLineIndex)
    {
	    wrappedModel.mergeLines(firstLineIndex);
    }

    @Override public String splitLines(int pos, int lineIndex)
    {
	return wrappedModel.splitLines(pos, lineIndex);
    }

    @Override public void doDirectAccessAction(DirectAccessAction action)
    {
	wrappedModel.doDirectAccessAction(action);
    }
}
