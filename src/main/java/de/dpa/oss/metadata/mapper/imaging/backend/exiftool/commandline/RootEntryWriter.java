package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Arrays;
import java.util.List;

/**
 * @author oliver langer
 */
public class RootEntryWriter extends BaseEntryWriter implements EntryWriter
{

    final private ListMultimap<String, String> tagToValue;

    public RootEntryWriter()
    {
        tagToValue = ArrayListMultimap.create();
    }

    protected RootEntryWriter(final ListMultimap<String, String> tagToValue)
    {
        this.tagToValue = tagToValue;
    }

    @Override public EntryWriter write(final String namespaceRef, final String key, final String value)
    {
        tagToValue.put(buildKey(namespaceRef,key), value);
        return this;
    }

    @Override public List<ListMultimap<String, String>> getKeyValueMaps()
    {
        return Arrays.asList(tagToValue);
    }

    @Override public EntryWriter beginArray(final String namespaceRef, final String key)
    {
        return new RootArrayEntryWriter(this, namespaceRef, key);
    }

    @Override public EntryWriter beginStruct(final String namespaceRef, final String key)
    {
        return new StructEntryWriter(this, namespaceRef, key);
    }

    @Override public EntryWriter beginLangAlt(final String namespaceRef, final String key)
    {
        return new LangAltEntryWriter(this, namespaceRef, key);
    }

    public ListMultimap<String,String> getTagToValues()
    {
        return tagToValue;
    }
}

