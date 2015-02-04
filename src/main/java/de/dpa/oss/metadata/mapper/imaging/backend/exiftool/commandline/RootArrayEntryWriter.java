package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

/**
 * @author oliver langer
 */
public class RootArrayEntryWriter extends BaseEntryWriter implements EntryWriter
{
    private RootEntryWriter rootEntryWriter;
    private String namespaceRef;
    private String key;

    protected RootArrayEntryWriter(final RootEntryWriter rootEntryWriter, final String namespaceRef, final String key)
    {
        this.rootEntryWriter = rootEntryWriter;
        this.namespaceRef = namespaceRef;
        this.key = key;
    }

    @Override public EntryWriter write(final String namespaceRef, final String ignoreKey, final String value)
    {
        rootEntryWriter.write(namespaceRef, key,value);
        return this;
    }

    @Override public EntryWriter write(final String value)
    {
        rootEntryWriter.write(namespaceRef, key,value);
        return this;
    }

    @Override public EntryWriter endArray()
    {
        return rootEntryWriter;
    }

    @Override protected EntryWriter write(final String namespaceRef, final String ignoreKey, final EntryWriter entryWriter)
    {
        return super.write(namespaceRef, key, entryWriter);
    }

    @Override public EntryWriter beginArray(final String namespaceRef, final String key)
    {
        return new ArrayEntryWriter(this, namespaceRef, key);
    }

    @Override public EntryWriter beginStruct(final String namespaceRef, final String key)
    {
        return new StructEntryWriter(this, namespaceRef, key);
    }

    @Override public EntryWriter beginLangAlt(final String namespaceRef, final String key)
    {
        return new LangAltEntryWriter(this, namespaceRef, key);
    }

}
