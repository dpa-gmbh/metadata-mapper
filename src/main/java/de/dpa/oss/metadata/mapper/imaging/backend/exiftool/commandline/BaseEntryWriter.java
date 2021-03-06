package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.Map;

/**
 * @author oliver langer
 */
public abstract class BaseEntryWriter implements EntryWriter
{
    protected EntryWriter write(final String namespaceRef, final String key, final EntryWriter entryWriter)
    {
        if( entryWriter != null )
        {
            // another entry writer has been called
            for (ListMultimap<String, String> keyValueMap : entryWriter.getKeyValueMaps())
            {
                for (Map.Entry<String,String> keyValueentry : keyValueMap.entries())
                {
                    write(namespaceRef, keyValueentry.getKey(), keyValueentry.getValue());
                }
            }
        }

        return this;
    }

    @Override public EntryWriter write(final String value)
    {
        throw new UnsupportedOperationException();
    }

    @Override public EntryWriter beginArray(final String namespaceRef, final String key)
    {
        throw new UnsupportedOperationException();
    }

    @Override public List<ListMultimap<String, String>> getKeyValueMaps()
    {
        throw new UnsupportedOperationException();
    }

    @Override public EntryWriter endArray()
    {
        throw new UnsupportedOperationException();
    }

    @Override public EntryWriter beginStruct(final String namespaceRef, final String key)
    {
        throw new UnsupportedOperationException();
    }

    @Override public EntryWriter endStruct()
    {
        throw new UnsupportedOperationException();
    }

    @Override public EntryWriter beginLangAlt(final String namespaceRef, final String key)
    {
        throw new UnsupportedOperationException();
    }

    @Override public EntryWriter endLangAlt()
    {
        throw new UnsupportedOperationException();
    }

    protected String buildKey( final String namespaceRef, final String key )
    {
        if(Strings.isNullOrEmpty(namespaceRef))
        {
            return key;
        }
        else
        {
            return namespaceRef+":"+key;
        }
    }
}
