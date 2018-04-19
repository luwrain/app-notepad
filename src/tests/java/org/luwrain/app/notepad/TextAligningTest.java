
package org.luwrain.app.notepad;

import org.junit.*;

import org.luwrain.core.*;

public class TextAligningTest extends Assert
{
    @Test public void simple() throws Exception
    {
	final TextAligning t = new TextAligning(5);
	assertTrue(t.origHotPointX == -1);
	assertTrue(t.origHotPointY == -1);
	t.origLines = new String[]{"aaa"};
	t.align();
	assertTrue(t.res.size() == 1);
	assertTrue(t.res.get(0).equals("aaa"));
	assertTrue(t.hotPointX == -1);
	assertTrue(t.hotPointY == -1);
	for(int i = 0;i < 3;++i)
	{
	    t.origHotPointY = 0;
	    t.origHotPointX = i;
	    t.res.clear();
	    t.hotPointX = -1;
	    t.hotPointY = -1;
	    t.align();
	    assertTrue(t.res.size() == 1);
	    assertTrue(t.res.get(0).equals("aaa"));
	    assertTrue(t.hotPointX == i);
	    assertTrue(t.hotPointY == 0);
	}
    }

        @Test public void coupleWords() throws Exception
    {
	final TextAligning t = new TextAligning(5);
	t.origLines = new String[]{"aaa bbb"};
	t.align();
	assertTrue(t.res.size() == 2);
	assertTrue(t.res.get(0).equals("aaa"));
		assertTrue(t.res.get(1).equals("bbb"));
			assertTrue(t.hotPointX == -1);
	assertTrue(t.hotPointY == -1);
	for(int i = 0;i < 7;++i)
	{
	    System.out.println("here i=" + i);
	    t.origHotPointY = 0;
	    t.origHotPointX = i;
	    t.res.clear();
	    t.hotPointX = -1;
	    t.hotPointY = -1;
	    t.align();
	    assertTrue(t.res.size() == 2);
	    if (i != 3)
		assertTrue(t.res.get(0).equals("aaa")); else
			    	    assertTrue(t.res.get(0).equals("aaa "));
	    	    	    	    assertTrue(t.res.get(1).equals("bbb"));
		    if (i < 4)
		    {
	    assertTrue(t.hotPointX == i);
	    assertTrue(t.hotPointY == 0);
		    } else
		    {
			//			System.out.println("x=" + t.hotPointX);
			//						System.out.println("y=" + t.hotPointY);
						assertTrue(t.hotPointX == i - 4);
															assertTrue(t.hotPointY == 1);
    		    }
	}
    }
}
