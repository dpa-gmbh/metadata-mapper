package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import com.google.common.collect.ListMultimap;

import java.util.List;

/**
 * @author oliver langer
 */
public interface KeyValueEntryWriter extends EntryWriter
{
    KeyValueEntryWriter write(final String key, final String value);

}
