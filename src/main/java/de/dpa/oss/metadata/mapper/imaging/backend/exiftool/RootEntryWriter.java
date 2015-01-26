package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import java.util.Map;

/**
 * @author oliver langer
 */
public class RootEntryWriter implements EntryWriter
{

    final private Map<String,String> tagToValue;

    public RootEntryWriter(final Map<String, String> tagToValue)
    {
        this.tagToValue = tagToValue;
    }

    @Override public void write(final String key, final String value )
    {
        tagToValue.put( key, value );
    }



}
