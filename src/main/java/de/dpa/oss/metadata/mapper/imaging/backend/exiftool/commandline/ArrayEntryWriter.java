package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Arrays;
import java.util.List;

/**
 * @author oliver langer
 */
public class ArrayEntryWriter extends BaseEntryWriter implements EntryWriter
{
    final StringBuilder sb;
    private String namespaceRef;
    final String arrayKey;
    final BaseEntryWriter previousEntryWriter;

    public ArrayEntryWriter(final BaseEntryWriter previousEntryWriter, final String namespaceRef, final String arrayKey)
    {
        this.previousEntryWriter = previousEntryWriter;
        this.namespaceRef = namespaceRef;
        this.arrayKey = arrayKey;
        sb = new StringBuilder("[");
    }

    private void separateEntriesIfNecessary()
    {
        if( sb.length()>1)
        {
            sb.append(',');
        }
    }

    @Override public EntryWriter write(final String namespaceRef, final String key, final String value)
    {
        return write(value);
    }

    @Override public EntryWriter write(final String value)
    {
        separateEntriesIfNecessary();
        sb.append(value);
        return this;
    }

    public EntryWriter endArray()
    {
        previousEntryWriter.write(namespaceRef, arrayKey,this);
        return previousEntryWriter;
    }

    @Override public List<ListMultimap<String, String>> getKeyValueMaps()
    {
        ListMultimap<String, String> toReturn = ArrayListMultimap.create();
        toReturn.put(arrayKey,sb.toString()+"]");
        return Arrays.asList(toReturn);

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
