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
    int hotPointX = -1;
    int hotPointY = -1;
    final LinkedList<String> res = new LinkedList();

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
		    final boolean withHotPoint = origHotPointY == lineIndex && origHotPointX >= spaceBeginPos && origHotPointX < pos;
		    onSpace(withHotPoint);
		}
	    }
	}
    }

    private void onWord(String word, int hotPointPos)
    {
	//System.out.println("kaka " + word + " " + hotPointPos);
	NullCheck.notEmpty(word, "word");
	if (hotPointPos >= word.length())
	    throw new IllegalArgumentException("hotPointPos (" + hotPointPos + ") may not be greater than " + word.length());
	if (res.isEmpty())
	{
	    res.add(word);
	    if (hotPointPos >= 0)
	    {
		hotPointX = hotPointPos;
		hotPointY = 0;
	    }
	    return;
	}
	final int prevLen = res.getLast().length();
	if (prevLen + word.length() <= maxLineLen)
	{
	    res.add(res.pollLast() + word);
	    if (hotPointPos >= 0)
	    {
		hotPointX = prevLen + hotPointPos;
		hotPointY = res.size() - 1;
	    }
	    return;
	}
	//On new line the word must be added anyway regardless its length
	fixEndingSpace();
	res.add(word);
	if (hotPointPos >= 0)
	{
	    hotPointX = hotPointPos;
	    hotPointY = res.size() - 1;
	}
    }

    private void onSpace(boolean withHotPoint)
    {
	//	System.out.println("kaka space " + withHotPoint);
	if (res.isEmpty())
	{
	    //Adding space only if it is with hot point
	    if (!withHotPoint)
		return;
	    res.add(" ");
	    hotPointX = 0;
	    hotPointY = 0;
	    return;
	}
	if (res.getLast().length() + 1 <= maxLineLen)
	{
	    res.add(res.pollLast() + " ");
	    if (withHotPoint)
	    {
		hotPointX = res.getLast().length() - 1;
		hotPointY = res.size() - 1;
	    }
	    return;
	}
	//res is not empty and we can not append space to the last line, doing this only for hot point
	if (!withHotPoint)
	    return;
	fixEndingSpace();
	res.add(" ");
	hotPointX = 0;
	hotPointY = res.size() - 1;
    }

    private void fixEndingSpace()
    {
	if (res.isEmpty() || res.getLast().isEmpty())
	    return;
	final String line = res.getLast();
	if (!Character.isSpaceChar(line.charAt(line.length() - 1)))
	    return;
	//	System.out.println("hotPointX=" + hotPointX);
	//	System.out.println("hotPointY=" + hotPointY);
	if (hotPointX == line .length() - 1 && hotPointY == res.size() - 1)
	    return;
	res.set(res.size() - 1, line.substring(0, line.length() - 1));
    }
}
