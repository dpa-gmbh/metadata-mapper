package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import java.io.IOException;

/**
 * @author oliver langer
 */
public class ExifToolIntegrationException extends Exception
{
    public ExifToolIntegrationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ExifToolIntegrationException(final Throwable throwable)
    {
        super(throwable);
    }
}
