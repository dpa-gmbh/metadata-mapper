package de.dpa.oss.common;

import java.nio.charset.Charset;

/**
 * @author oliver langer
 */
public class EncodedString
{
    private final String utf16String;
    private final Charset charset;

    public EncodedString(String utf16String, Charset charset)
    {
        this.utf16String = utf16String;
        this.charset = charset;
    }

    public int getLength()
    {
        return (utf16String != null) ? utf16String.length() : 0;

    }

    public byte[] getBytes()
    {
        return utf16String.getBytes(charset);
    }

    public String getUtf16String()
    {
        return utf16String;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof EncodedString))
        {
            return false;
        }

        final EncodedString that = (EncodedString) o;

        if (charset != null ? !charset.equals(that.charset) : that.charset != null)
        {
            return false;
        }
        if (utf16String != null ? !utf16String.equals(that.utf16String) : that.utf16String != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = utf16String != null ? utf16String.hashCode() : 0;
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        return result;
    }
}
