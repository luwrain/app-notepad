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

import java.io.*;
import javax.sound.sampled.AudioFormat;

import org.luwrain.core.*;
import org.luwrain.speech.*;

abstract class Narrating implements Runnable
{
    private final Strings strings;
    private final File destDir;
    private final String text;
    private final Channel channel;
    private final String compressorCmd;

    private File currentFile;
    private OutputStream stream;
    private int fragmentNum = 1;
    private AudioFormat chosenFormat = null;
    private int lastPercents = 0;

    Narrating(Strings strings, String text, File destDir, String compressorCmd, Channel channel)
    {
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(text, "text");
	NullCheck.notNull(destDir, "destDir");
	NullCheck.notNull(compressorCmd, "compressorCmd");
	NullCheck.notNull(channel, "channel");
	this.strings = strings;
	this.text = text;
	this.destDir = destDir;
	this.compressorCmd = compressorCmd;
	this.channel = channel;
    }

    abstract protected void progressLine(String text, boolean doneMessage);

    @Override public void run()
    {
	try {
	    final AudioFormat[] formats = channel.getSynthSupportedFormats();
	    if (formats == null || formats.length < 0)
	    {
		progressLine(strings.narratingNoSupportedAudioFormats(), false);
		return;
	    }
	    chosenFormat = formats[0];
	    openStream();
	    splitText();
	    closeStream();
	    progressLine(strings.done(), true);
	}
	catch(Exception e)
	{
	    progressLine(e.getClass() + ":" + e.getMessage(), false);
	}
    }

    private void splitText() throws IOException
    {
StringBuilder b = new StringBuilder();
	for(int i = 0;i < text.length();++i)
	{
	    final int percents = (i * 100) / text.length();
	    if (percents > lastPercents)
	    {
		progressLine("" + percents + "%", false);
		lastPercents = percents;
	    }
	    final char c = text.charAt(i);
	    final char cc = (i + 1 < text.length())?text.charAt(i + 1):'\0';
	    if (c == '\n' && cc == '#')
	    {
		int k = i + 1;
		while(k < text.length() && text.charAt(k) != '\n')
		    ++k;
		final String s = new String(b);
		if (k >= text.length())//If the line with hash command is the last one, skipping it
		    break;
		if (k > i + 1 && onHashCmd(s, text.substring(i + 1, k)))
		{
		    b = new StringBuilder();
		    i = k;
		    continue;
		}
	    }
	    if (Character.isISOControl(c))
	    {
		b.append(" ");
		continue;
	    }
	    if (c == '.' || c == '!' || c == '?')
	    {
		b.append(c);
		final String s = new String(b);
		b = new StringBuilder();
		if (s.length() > 1)
		    onNewPortion(s, true);
		continue;
	    }
	    b.append(c);
	}
	final String s = new String(b);
	if (!s.isEmpty())
	    onNewPortion(s, true);
    }

    private void onNewPortion(String s, boolean commit) throws IOException
    {
	//	channel.synth(s, 0, 0, chosenFormat, stream);
	if (commit)
	    checkSize();
    }

    private void openStream() throws IOException
    {
	currentFile = File.createTempFile("lwrnarrating", ".dat");
	stream = new FileOutputStream(currentFile);
    }

    private void closeStream() throws IOException
    {
	stream.flush();
	stream.close();
	stream = null;
	final String fileName = getNextFragmentFileName() + ".wav";
	final File targetFile = new File(destDir, fileName);
	progressLine(strings.compressing(targetFile.getName()), false);
	callCompressor(currentFile, targetFile);
	currentFile.delete();
	currentFile = null;
    }

    private void saveWavFile()
    {

    }

    private void checkSize() throws IOException
    {
	stream.flush();
	if (currentFile.length() > timeToBytes(300000))//5 min
	{
	    closeStream();
	    openStream();
	}
    }

    private void callCompressor(File inputFile, File outputFile)
    {
	try {
	    final Process p = new ProcessBuilder(compressorCmd, inputFile.getAbsolutePath(), outputFile.getAbsolutePath()).start();
	    p.waitFor();
	}
	catch(IOException e)
	{
	    progressLine(e.getClass().getName() + ":" + e.getMessage(), false);
	}
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	}
    }

    private void silence(int delay) throws IOException
    {
	final int numBytes = timeToBytes(delay);
	final byte[] buf = new byte[numBytes];
	for(int i = 0;i < buf.length;++i)
	    buf[i] = 0;
	stream.write(buf);
    }

    private boolean onHashCmd(String uncommittedText, String cmd) throws IOException
    {
	if (cmd.length() < 2)
	    return false;
	final String body = cmd.substring(1);
try {
	    final int delay = Integer.parseInt(body);
	    if (delay > 100 && delay < 100000)
	    {
		onNewPortion(uncommittedText, false);
		silence(delay);
	    return true;
	    } else
		return false;
}
	    catch (NumberFormatException e)
	    { return false; }
    }

    private int timeToBytes(int msec)
    {
	float value = chosenFormat.getSampleRate() * chosenFormat.getSampleSizeInBits() * chosenFormat.getChannels();//bits in a second
	value /= 8;//bytes in a second
	value /= 1000;//bytes in millisecond
	return (int)(value * msec);
    }

    private String getNextFragmentFileName()
    {
	String fileName = "" + fragmentNum;
	++fragmentNum;
	while(fileName.length() < 3)
	    fileName = "0" + fileName;
	return fileName;

    }
}
