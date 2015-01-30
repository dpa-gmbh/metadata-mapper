package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import com.google.common.collect.ListMultimap;

import java.util.Arrays;
import java.util.List;

/**
 * @author oliver langer
 */
public class RootEntryWriter extends BaseEntryWriter implements EntryWriter
{

    final private ListMultimap<String, String> tagToValue;

    public RootEntryWriter(final ListMultimap<String, String> tagToValue)
    {
        this.tagToValue = tagToValue;
    }

    @Override public EntryWriter write(final String key, final String value)
    {
        tagToValue.put(key, value);
        return this;
    }

    @Override public List<ListMultimap<String, String>> getKeyValueMaps()
    {
        return Arrays.asList(tagToValue);
    }

    @Override public EntryWriter beginArray(final String key)
    {
        return new RootArrayEntryWriter(this, key);
    }

    @Override public EntryWriter beginStruct(final String key)
    {
        return new StructEntryWriter(this, key);
    }

    @Override public EntryWriter beginLangAlt(final String key)
    {
        return new LangAltEntryWriter(this,key);
    }
}

