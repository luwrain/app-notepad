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

import java.util.*;

import org.luwrain.core.*;

final class TextAligning
{
    final int maxLineLen;
    int origHotPointX = -1;
    int origHotPointY = -1;
    String[] origLines = new String[0];
    String[] result = new String[0];

    TextAligning(int maxLineLen)
    {
	if (maxLineLen < 0)
	    throw new IllegalArgumentException("maxLineLen (" + maxLineLen + ") may not be negative");
	this.maxLineLen = maxLineLen;
    }

    void align()
    {
	for(int lineIndex = 0;lineIndex < origLines.length;++lineIndex)
	{
	    final String line = origLines[lineIndex];
	    if (line.isEmpty())
		continue;
	    int pos = 0;
	    while (pos < line.length())
	    {
		final int wordBeginPos = pos;
		while (pos < line.length() && !Character.isSpaceChar(line.charAt(pos)))
		    ++pos;
		if (pos > wordBeginPos)
		{
		    //Handling the word
		    final int hotPointPos;
		    if (origHotPointY == lineIndex && origHotPointX >= wordBeginPos && origHotPointX < pos)
			hotPointPos = origHotPointX - wordBeginPos; else
			hotPointPos = -1;
		    onWord(line.substring(wordBeginPos, pos), hotPointPos);
		}
		final int spaceBeginPos = pos;
		while(pos < line.length() && Character.isSpaceChar(line.charAt(pos)))
		    ++pos;
		if (pos > spaceBeginPos)
		{
		    //Handling the space
		    final int hotPointPos;
		    if (origHotPointY == lineIndex && origHotPointX >= spaceBeginPos && origHotPointX < pos)
			hotPointPos = origHotPointX - spaceBeginPos; else
			hotPointPos = -1;
		    onSpace(hotPointPos);
		}
	    }
	}
    }

    private void onWord(String word, int hotPointPos)
    {
    }

    private void onSpace(int hotPointPos)
    {
    }
}
