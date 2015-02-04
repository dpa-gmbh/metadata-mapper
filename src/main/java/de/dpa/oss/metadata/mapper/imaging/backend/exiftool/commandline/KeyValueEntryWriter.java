package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

/**
 * @author oliver langer
 */
public interface KeyValueEntryWriter extends EntryWriter
{
    KeyValueEntryWriter write(final String key, final String value);

}
