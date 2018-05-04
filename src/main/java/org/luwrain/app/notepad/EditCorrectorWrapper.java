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

    private MultilineEditCorrector wrappedCorrector = null;

    private void alignParagraph(int pos, int lineIndex)
    {
	doDirectAccessAction((lines,hotPoint)->{
		//Doing nothing on empty line
		if (lines.getLine(lineIndex).trim().isEmpty())
		    return;
		//Searching paragraph bounds
		int paraBegin = lineIndex;
		int paraEnd = lineIndex;
		while (paraBegin > 0 && !lines.getLine(paraBegin).trim().isEmpty())
	    	    --paraBegin;
		if (lines.getLine(paraBegin).trim().isEmpty())
		    ++paraBegin;
		while (paraEnd < lines.getLineCount() && !lines.getLine(paraEnd).trim().isEmpty())
		    ++paraEnd;
		//Looking for the first line where it's necessary to do correction from
		int startingLine = 0;
		for(startingLine = paraBegin;startingLine < paraEnd;++startingLine)
		    if (lines.getLine(startingLine).length() > ALIGNING_LINE_LEN)
			break;
		//Stopping, if there are no long lines at all
		if (startingLine == paraEnd)
		    return;
		doAligning(lines, hotPoint, startingLine, paraEnd);
	    });
    }

    //The fragment of lines between lineFrom and lineTo may not contain empty strings
    private void doAligning(MutableLines lines, HotPointControl hotPoint, int lineFrom, int lineTo)
    {
	Log.debug("proba", "doAligning(" + lineFrom + "," + lineTo + ")");
	NullCheck.notNull(lines, "lines");
	NullCheck.notNull(hotPoint, "hotPoint");
	if (lineFrom < 0 || lineFrom >= lines.getLineCount())
	    throw new IllegalArgumentException("lineFrom (" + lineFrom + ") must be less than " + lines.getLineCount() + " and non-negative");
	if (lineTo < 0 || lineTo > lines.getLineCount())
	    throw new IllegalArgumentException("lineTo (" + lineTo + ") must be less than " + lines.getLineCount() + " and non-negative");
	if (lineFrom >= lineTo)
	    throw new IllegalArgumentException("lineFrom (" + lineFrom + ") must be less than lineTo (" + lineTo + ")");
	final int hotPointX = hotPoint.getHotPointX();
	final int hotPointY = hotPoint.getHotPointY();
	if (hotPointY < lineFrom || hotPointY >= lineTo)
	{
	    final TextAligning t = new TextAligning(ALIGNING_LINE_LEN);
	    t.origLines = new String[lineTo - lineFrom];
	    for(int i = lineFrom;i < lineTo;++i)
		t.origLines[i - lineFrom] = lines.getLine(i);
	    t.align();
	    //FIXME:do it more effectively
	    for(int i = lineFrom;i < lineTo;++i)
		lines.removeLine(lineFrom);
	    int k = 0;
	    for(String s: t.res)
		lines.insertLine(lineFrom + (k++), s);
	} else
	{
	    final TextAligning t = new TextAligning(ALIGNING_LINE_LEN);
	    t.origLines = new String[lineTo - lineFrom];
	    for(int i = lineFrom;i < lineTo;++i)
		t.origLines[i - lineFrom] = lines.getLine(i);
	    t.origHotPointX = hotPointX;
	    t.origHotPointY = hotPointY - lineFrom;
	    	    Log.debug("proba", "before " + t.origHotPointX + " " + t.origHotPointY);
		    //Taking care of positioning of the hot point outsite of line bounds, may be on one character on the right
	    final boolean hotPointXShifted;
	    if (t.origHotPointX >= t.origLines[t.origHotPointY].length())
	    {
		if (t.origLines[t.origHotPointY].isEmpty())
		    throw new IllegalArgumentException("lines array contains the empty string at position " + (t.origHotPointY + lineFrom));
		--t.origHotPointX;
		hotPointXShifted = true; 
	    } else
		hotPointXShifted = false;
	    //	    Log.debug("shifted=" + hotPointXShifted);
	    t.align();
	    Log.debug("proba", "after " + t.hotPointX + " " + t.hotPointY);
	    //FIXME:do it more effectively
	    for(int i = lineFrom;i < lineTo;++i)
		lines.removeLine(lineFrom);
	    int k = 0;
	    for(String s: t.res)
		lines.insertLine(lineFrom + (k++), s);
	    if (t.hotPointX >= 0 && t.hotPointY >= 0)
	    {
			    hotPoint.setHotPointY(t.hotPointY + lineFrom);
		hotPoint.setHotPointX(t.hotPointX + (hotPointXShifted?1:0));
	    }
	}
    }

    void setWrappedCorrector(MultilineEditCorrector corrector)
    {
	NullCheck.notNull(corrector, "corrector");
	this.wrappedCorrector = corrector;
    }

    MultilineEditCorrector getWrappedCorrector()
    {
	return wrappedCorrector;
    }

    @Override public int getLineCount()
    {
	return wrappedCorrector.getLineCount();
    }

    @Override public String getLine(int index)
    {
	return wrappedCorrector.getLine(index);
    }

    @Override public int getHotPointX()
    {
	return wrappedCorrector.getHotPointX();
    }

    @Override public int getHotPointY()
    {
	return wrappedCorrector.getHotPointY();
    }

    @Override public String getTabSeq()
    {
	return wrappedCorrector.getTabSeq();
    }

    @Override public char deleteChar(int pos, int lineIndex)
    {
	return wrappedCorrector.deleteChar(pos, lineIndex);
    }

    @Override public boolean deleteRegion(int fromX, int fromY, int toX, int toY)
    {
	return wrappedCorrector.deleteRegion(fromX, fromY, toX, toY);
    }

    @Override public boolean insertRegion(int x, int y, String[] lines)
    {
	NullCheck.notNullItems(lines, "lines");
	return wrappedCorrector.insertRegion(x, y, lines);
    }

    @Override public void insertChars(int pos, int lineIndex, String str)
    {
	NullCheck.notNull(str, "str");
		    wrappedCorrector.insertChars(pos, lineIndex, str);
	if (str.equals(" "))
	    alignParagraph(pos, lineIndex);
    }

    @Override public void mergeLines(int firstLineIndex)
    {
	    wrappedCorrector.mergeLines(firstLineIndex);
    }

    @Override public String splitLines(int pos, int lineIndex)
    {
	return wrappedCorrector.splitLines(pos, lineIndex);
    }

    @Override public void doDirectAccessAction(DirectAccessAction action)
    {
	wrappedCorrector.doDirectAccessAction(action);
    }
}
