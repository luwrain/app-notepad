/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

class Narrating implements Runnable
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    interface Listener
    {
	void writeMessage(String text);
	void progressUpdate(int sentsProcessed, int sentsTotal);
	void done();
	void cancelled();
    }

    private final App app;
    private final Listener listener;
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

    Narrating(App app, Listener listener, String[] text, File destDir, String compressorCmd, Channel channel)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(listener, "listener");
	NullCheck.notNullItems(text, "text");
	NullCheck.notNull(destDir, "destDir");
	NullCheck.notNull(compressorCmd, "compressorCmd");
	NullCheck.notNull(channel, "channel");
	this.app = app;
	this .listener = listener;
	this.text = text;
	this.destDir = destDir;
	this.compressorCmd = compressorCmd;
	this.channel = channel;
	final AudioFormat[] formats = channel.getSynthSupportedFormats();
	if (formats == null || formats.length == 0)
	    throw new RuntimeException("No supported audio formats");
	this.chosenFormat = formats[0];
	Log.debug(LOG_COMPONENT, "chosen format is " + chosenFormat.toString());
	if (app.sett.getNarratedFileLen(0) > 0)
	    this.maxFragmentBytes = timeToBytes(app.sett.getNarratedFileLen(0) * 1000); else
	    this.maxFragmentBytes = 0;
	Log.debug(LOG_COMPONENT, "max length of a fragment in bytes is " + String.valueOf(maxFragmentBytes));
    }

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
			onNewSent(app.getLuwrain().getSpeakableText(s, Luwrain.SpeakableTextType.NATURAL)); else
			silence(app.sett.getNarratingPauseDuration(500));
		    listener.progressUpdate(i, text.length);
		}
	    }
	    finally {
		closeStream();
		if (interrupting)
		    listener.cancelled();
	    }
	    listener.done();
	}
	catch(Exception e)
	{
	    app.getLuwrain().crash(e);
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
	p.setRate(app.sett.getNarratingSpeechRate(0));
	p.setPitch(app.sett.getNarratingSpeechPitch(0));
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
	listener.writeMessage(app.getStrings().narratingFileWritten(targetFile.getAbsolutePath()));
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
	StreamUtils.writeAllBytes(stream, buf);
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
