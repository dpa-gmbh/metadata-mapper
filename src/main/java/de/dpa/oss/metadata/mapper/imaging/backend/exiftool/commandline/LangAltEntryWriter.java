package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Arrays;
import java.util.List;

/**
 * @author oliver langer
 */
public class LangAltEntryWriter extends BaseEntryWriter implements EntryWriter
{
    private final BaseEntryWriter previousWriter;
    private String key;
    private final ListMultimap<String,String> langAltElements = ArrayListMultimap.create();

    public LangAltEntryWriter(final BaseEntryWriter previousWriter, final String key)
    {
        this.previousWriter = previousWriter;
        this.key = key;
    }

    @Override protected EntryWriter write(final String key, final EntryWriter entryWriter)
    {
        throw new UnsupportedOperationException( "not valid inside lang alt element");
    }

    @Override public EntryWriter write(final String langId, final String value)
    {
        if(Strings.isNullOrEmpty(langId))
        {
            langAltElements.put(key, value);
        }
        else
        {
            langAltElements.put(key+"-"+langId, value);
        }
        return this;
    }

    @Override public List<ListMultimap<String, String>> getKeyValueMaps()
    {
        return Arrays.asList( langAltElements);
    }

    @Override public EntryWriter endLangAlt()
    {
        previousWriter.write(key,this);
        return previousWriter;
    }
}
