
package org.luwrain.app.notepad;

import java.io.*;
import java.nio.charset.Charset;

class Document
{
    public File file;
    public boolean modified = false;
    public boolean defaultDoc = false;
    public Charset charset;

    public Document(File file,
		    Charset charset,
boolean defaultDoc)
    {
	this.file = file;
	this.modified = false;
	this.defaultDoc = defaultDoc;
	this.charset = charset;
	if (file == null)
	    throw new NullPointerException("file may not be null");
	if (charset == null)
	    throw new NullPointerException("charset may not be null");
    }
}
