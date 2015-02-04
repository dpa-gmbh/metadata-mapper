package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

/**
 * @author oliver langer
 */
public class RootArrayEntryWriter extends BaseEntryWriter implements EntryWriter
{
    private RootEntryWriter rootEntryWriter;
    private String key;

    protected RootArrayEntryWriter(final RootEntryWriter rootEntryWriter, final String key)
    {
        this.rootEntryWriter = rootEntryWriter;
        this.key = key;
    }

    @Override public EntryWriter write(final String ignoreKey, final String value)
    {
        rootEntryWriter.write(key,value);
        return this;
    }

    @Override public EntryWriter write(final String value)
    {
        rootEntryWriter.write(key,value);
        return this;
    }

    @Override public EntryWriter endArray()
    {
        return rootEntryWriter;
    }

    @Override protected EntryWriter write(final String ignoreKey, final EntryWriter entryWriter)
    {
        return super.write(key, entryWriter);
    }

    @Override public EntryWriter beginArray(final String key)
    {
        return new ArrayEntryWriter(this, key);
    }

    @Override public EntryWriter beginStruct(final String key)
    {
        return new StructEntryWriter(this,key);
    }

    @Override public EntryWriter beginLangAlt(final String key)
    {
        return new LangAltEntryWriter(this,key);
    }

}
