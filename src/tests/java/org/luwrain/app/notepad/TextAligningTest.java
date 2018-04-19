
package org.luwrain.app.notepad;

import org.junit.*;

import org.luwrain.core.*;

public class TextAligningTest extends Assert
{
    @Test public void simple() throws Exception
    {
	final TextAligning t = new TextAligning(5);
	t.origLines = new String[]{"aaa"};
	t.align();
	assertTrue(t.res.size() == 1);
	assertTrue(t.res.get(0).equals("aaa"));
	assertTrue(t.hotPointX == -1);
	assertTrue(t.hotPointY == -1);
	for(int i = 0;i < 3;++i)
	{
	    t.hotPointY = 0;
	    t.hotPointX = i;
	    t.res.clear();
	    t.align();
	    assertTrue(t.res.size() == 1);
	    assertTrue(t.res.get(0).equals("aaa"));
	    assertTrue(t.hotPointX == i);
	    assertTrue(t.hotPointY == 0);
	}
    }
}
