package de.dpa.oss.common;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

/**
 * YARU
 * @author oliver langer
 */
public class ResourceUtil
{
    public static String resourceAsString( final String resourcePath, Object caller ) throws IOException
    {
        return new String(ByteStreams.toByteArray( resourceAsStream( resourcePath,caller)));
    }

    public static InputStream resourceAsStream( final String resourcePath, Object caller )
    {
        return caller.getClass().getResourceAsStream(resourcePath);
    }
}
