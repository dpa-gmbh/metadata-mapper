package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

/**
 * @author oliver langer
 */
public class StructEntryWriter implements EntryWriter
{
    private final StringBuilder sb;

    private StructEntryWriter()
    {
        this.sb = new StringBuilder("{");
    }

    public static StructEntryWriter beginStruct()
    {
        return new StructEntryWriter();
    }

    @Override public void write(final String key, final String value)
    {
        if( sb.length()>1)
        {
            sb.append(',');
        }
        sb.append( key ).append('=').append(value);
    }

    public String endStruct()
    {
        return sb.append('}').toString();
    }
}
