package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

/**
 * @author oliver langer
 */
public class ArrayEntryWriter implements EntryWriter
{
    final StringBuilder sb;

    public ArrayEntryWriter()
    {
        sb = new StringBuilder("[");
    }

    @Override public void write(final String key, final String value)
    {
        if (sb.length() > 1)
        {
            sb.append(',');
        }

        sb.append(value);
    }

    public String endArray()
    {
        sb.append(']');
        return sb.toString();
    }
}
