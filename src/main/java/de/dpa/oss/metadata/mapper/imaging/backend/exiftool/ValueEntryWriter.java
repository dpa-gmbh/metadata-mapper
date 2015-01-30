package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

/**
 * @author oliver langer
 */
public interface ValueEntryWriter extends EntryWriter
{
    ValueEntryWriter write(final String value);
}
