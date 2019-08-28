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

import java.util.*;
import java.io.*;
import javax.sound.sampled.AudioFormat;

import org.luwrain.core.*;
import org.luwrain.speech.*;
import org.luwrain.util.*;

abstract class Narrating implements Runnable
{
    static private final String LOG_COMPONENT = Base.LOG_COMPONENT;

    private final Base base;
    private final File destDir;
    private final String[] text;
    private final Channel channel;
    private final String compressorCmd;

    private File currentFile;
    private OutputStream stream;
    private int fragmentNum = 1;
    private AudioFormat chosenFormat = null;
    private int lastPercents = 0;

    Narrating(Base base, String[] text, File destDir, String compressorCmd, Channel channel)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNullItems(text, "text");
	NullCheck.notNull(destDir, "destDir");
	NullCheck.notNull(compressorCmd, "compressorCmd");
	NullCheck.notNull(channel, "channel");
	this.base = base;
	this.text = text;
	this.destDir = destDir;
	this.compressorCmd = compressorCmd;
	this.channel = channel;
	final AudioFormat[] formats = channel.getSynthSupportedFormats();
	if (formats == null || formats.length == 0)
	    throw new RuntimeException("No supported audio formats");
	    this.chosenFormat = formats[0];
	    Log.debug(LOG_COMPONENT, "chosen format is " + chosenFormat.toString());
    }

    abstract protected void progressLine(String text, boolean doneMessage);

    @Override public void run()
    {
		try {
	Log.debug(LOG_COMPONENT, "starting narrating");
	    openStream();
	    for(String s: text)
		if (!s.isEmpty())
		onNewSent(s); else
		    silence(2000);
	    closeStream();
	    base.luwrain.playSound(Sounds.DONE);
	}
	catch(Exception e)
	{
	    base.luwrain.crash(e);
	}
    }

    private void onNewSent(String s) throws IOException
    {
	stream.flush();
	final Channel.SyncParams p = new Channel.SyncParams();
	p.setRate(0);
	p.setPitch(-50);
	Log.debug(LOG_COMPONENT, "Speaking \'" + s + "\'");
	final Channel.Result res = channel.synth(s, stream, chosenFormat, p, EnumSet.noneOf(Channel.Flags.class));
    }

    private void openStream() throws IOException
    {
	this.currentFile = File.createTempFile("lwrnarrating", ".dat");
	Log.debug(LOG_COMPONENT, "created the temporary file " + this.currentFile.getAbsolutePath());
	this.stream = new FileOutputStream(this.currentFile);
    }

    private void closeStream() throws IOException
    {
	Log.debug(LOG_COMPONENT, "closing stream");
	stream.flush();
	stream.close();
	stream = null;
	final File targetFile = new File(destDir, getNextFragmentFileName() + ".wav");
	final OutputStream targetStream = new FileOutputStream(targetFile);
	final InputStream is = new FileInputStream(currentFile);
	try {
	    Log.debug(LOG_COMPONENT, "creating " + targetFile.getAbsolutePath());
	    final byte[] header = SoundUtils.createWaveHeader(chosenFormat, (int)currentFile.length());
targetStream.write(header);
StreamUtils.copyAllBytes(is, targetStream);
targetStream.flush();
	}
	finally {
	    is.close();
	    targetStream.close();
	}
	progressLine(base.strings.compressing(targetFile.getName()), false);
	//	callCompressor(currentFile, targetFile);
		currentFile.delete();
	Log.debug(LOG_COMPONENT, "the temporary file deleted");
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

    private void silence(int delayMsec) throws IOException
    {
	final int numBytes = timeToBytes(delayMsec);
	final byte[] buf = new byte[numBytes];
	for(int i = 0;i < buf.length;++i)
	    buf[i] = 0;
	FileUtils.writeAllBytes(stream, buf);
    }

    private int timeToBytes(int msec)
    {
	float value = chosenFormat.getSampleRate() * chosenFormat.getSampleSizeInBits() * chosenFormat.getChannels();//bits in a second
	value /= 8;//bytes in a second
	value /= 1000;//bytes in msec
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
