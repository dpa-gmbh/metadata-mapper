package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Arrays;
import java.util.List;

/**
 * @author oliver langer
 */
public class StructEntryWriter extends BaseEntryWriter implements EntryWriter
{
    private BaseEntryWriter callingEntryWriter;
    private String namespaceRef;
    private String key;
    private final StringBuilder sb;

    public StructEntryWriter(final BaseEntryWriter callingEntryWriter, final String namespaceRef, final String key)
    {
        this.callingEntryWriter = callingEntryWriter;
        this.namespaceRef = namespaceRef;
        this.key = key;
        this.sb = new StringBuilder("{");
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
        separateEntriesIfNecessary();
        sb.append(key).append('=').append(value);
        return this;
    }

    @Override public List<ListMultimap<String, String>> getKeyValueMaps()
    {
        ListMultimap<String, String> toReturn = ArrayListMultimap.create();
        toReturn.put( key, sb.toString() + "}" );

        return Arrays.asList(toReturn);
    }

    public EntryWriter endStruct()
    {
        callingEntryWriter.write(namespaceRef, key, this );
        return callingEntryWriter;
    }

    @Override public EntryWriter beginArray(final String namespaceRef, final String key)
    {
        /**
         * Do not use namespace prefix inside arrays:
         */
        return new ArrayEntryWriter(this, null, key);
    }

    @Override public EntryWriter beginStruct(final String namespaceRef, final String key)
    {
        return new StructEntryWriter(this, null, key);
    }

    @Override public EntryWriter beginLangAlt(final String namespaceRef, final String key)
    {
        /**
         * Do not use namespace prefix inside arrays:
         */
        return new LangAltEntryWriter(this, null, key);
    }
}
