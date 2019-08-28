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
    boolean interrupting = false;

    private final long maxFragmentBytes;
    private File currentFile = null;
    private OutputStream stream = null;
    private int fragmentNum = 1;
    private AudioFormat chosenFormat = null;

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
	if (base.sett.getNarratedFileLen(0) > 0)
	    this.maxFragmentBytes = timeToBytes(base.sett.getNarratedFileLen(0) * 1000); else
	    this.maxFragmentBytes = 0;
	Log.debug(LOG_COMPONENT, "max length of a fragment in bytes is " + String.valueOf(maxFragmentBytes));
    }

    abstract protected void writeMessage(String text);
    abstract protected void progressUpdate(int sentsProcessed, int sentsTotal);
    abstract protected void done();
    abstract protected void cancelled();

    @Override public void run()
    {
	try {
	    try {
		Log.debug(LOG_COMPONENT, "starting narrating");
		openStream();
		for(int i = 0;i < text.length;i++)
		{
		    if (interrupting)
			return;
		    final String s = text[i];
		    if (!s.isEmpty())
			onNewSent(s); else
			silence(base.sett.getNarratingPauseDuration(500));
		    progressUpdate(i, text.length);
		}
	    }
	    finally {
		closeStream();
		if (interrupting)
		    cancelled();
	    }
	    done();
	}
	catch(Exception e)
	{
	    base.luwrain.crash(e);
	}
    }

    private void onNewSent(String s) throws IOException
    {
	if (maxFragmentBytes > 0)
	{
	    stream.flush();
	    if (currentFile.length() > maxFragmentBytes)
	    {
		closeStream();
		openStream();
	    }
	}
	final Channel.SyncParams p = new Channel.SyncParams();
	p.setRate(base.sett.getNarratingSpeechRate(0));
	p.setPitch(base.sett.getNarratingSpeechPitch(0));
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
	if (this.stream == null || this.currentFile == null)
	{
	    Log.debug(LOG_COMPONENT, "nothing to close for narrating");
	    return;
	}
	Log.debug(LOG_COMPONENT, "closing stream");
	this.stream.flush();
	this.stream.close();
	this.stream = null;
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
	writeMessage(base.strings.narratingFileWritten(targetFile.getAbsolutePath()));
	//	callCompressor(currentFile, targetFile);
	this.currentFile.delete();
	this.currentFile = null;
	Log.debug(LOG_COMPONENT, "the temporary file deleted");
    }

    private void silence(int delayMsec) throws IOException
    {
	if (delayMsec <= 0)
	    return;
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
