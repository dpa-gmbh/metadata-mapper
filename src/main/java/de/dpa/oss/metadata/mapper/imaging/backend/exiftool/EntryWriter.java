package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

/**
 * @author oliver langer
 */
public interface EntryWriter
{
    void write(final String key, final String value);
}
