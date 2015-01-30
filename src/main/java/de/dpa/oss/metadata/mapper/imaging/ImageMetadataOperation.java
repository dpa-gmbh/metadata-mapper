package de.dpa.oss.metadata.mapper.imaging;

import com.adobe.xmp.XMPException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolBackend;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;

/**
 * @author oliver langer
 */
public class ImageMetadataOperation
{
    private TimeZone timeZone;

    public ImageMetadataOperation(final TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    public void setTimeZone(final TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    public void writeMetadata(final InputStream inputStream, final ImageMetadata imageMetadata, final OutputStream imageOutput)
            throws XMPException
    {
        new ExifToolBackend(timeZone).writeMetadata(inputStream,imageMetadata,imageOutput);
    }

    public void removeAllMetadata(final FileInputStream imageInputStream, final ByteArrayOutputStream byteArrayOutputStream)
    {
        // TODO
    }
}
