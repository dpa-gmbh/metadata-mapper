package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline;

/**
 * @author oliver langer
 */
public interface ValueEntryWriter extends EntryWriter
{
    ValueEntryWriter write(final String value);
}
