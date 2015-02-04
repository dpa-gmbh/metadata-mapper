package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

import com.google.common.collect.ListMultimap;

import java.util.List;

/**
 * @author oliver langer
 */
public interface EntryWriter
{
    EntryWriter write(final String key, final String value);

    EntryWriter write(final String value);

    List<ListMultimap<String, String>> getKeyValueMaps();

    EntryWriter beginArray(String key);

    EntryWriter endArray();

    EntryWriter beginStruct(String key);

    EntryWriter endStruct();

    EntryWriter beginLangAlt(String key);

    EntryWriter endLangAlt();
}
