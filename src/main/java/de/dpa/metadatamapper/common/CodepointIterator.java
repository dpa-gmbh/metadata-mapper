package de.dpa.metadatamapper.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author oliver langer
 */
public class CodepointIterator implements Iterator<Integer>
{
    private int nextPos;
    private final int stringLength;
    private String srcString;

    public static CodepointIterator iterate( final String srcStr )
    {
        if( srcStr == null  || srcStr.length() == 0 )
        {
            return new CodepointIterator( srcStr, Integer.MAX_VALUE );
        }
        else
        {
            return new CodepointIterator(srcStr,0);
        }
        
    }

    public CodepointIterator( final String srcString, int startPos )
    {
        this.srcString = srcString;
        this.stringLength = (srcString != null) ? srcString.length() : 0;
        this.nextPos = startPos;
    }

    public Integer next()
    {
        if( !hasNext() )
        {
            throw new NoSuchElementException("String of char length=" + stringLength + " processed");
        }
        
        int codepoint = srcString.codePointAt(nextPos);
        
        nextPos += Character.charCount(codepoint);
        
        return codepoint;
    }
    
    @Override public boolean hasNext()
    {
        return nextPos < stringLength;
    }

    @Override public void remove()
    {
        throw new UnsupportedOperationException( "what??");
    }
}
