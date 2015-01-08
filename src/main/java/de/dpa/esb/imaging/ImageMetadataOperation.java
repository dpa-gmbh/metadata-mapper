package de.dpa.esb.imaging;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.google.common.io.ByteStreams;
import de.dpa.esb.imaging.common.ImageMetadataWriter;
import de.dpa.esb.imaging.xmp.metadata.XMPMetadata;
import de.dpa.esb.imaging.xmp.parser.XMPMetadataFactory;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.bytesource.ByteSourceArray;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.jpeg.iptc.JpegIptcRewriter;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.util.IoUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * @author oliver langer
 */
public class ImageMetadataOperation
{
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private final TimeZone timezone;

    public static Logger logger = Logger.getLogger(ImageMetadataOperation.class);

    public ImageMetadataOperation()
    {
        timezone = UTC;
    }

    public ImageMetadataOperation(final TimeZone timezone)
    {
        this.timezone = timezone;
    }

    protected void removeIPTCMetadata(final InputStream inputStream, final OutputStream outputStream)
            throws ImageWriteException, ImageReadException, IOException
    {
        new JpegIptcRewriter().removeIPTC(inputStream, outputStream);
    }

    protected void removeExifMetadata(final InputStream inputStream, final OutputStream outputStream)
            throws ImageWriteException, ImageReadException, IOException
    {
        new ExifRewriter().removeExifMetadata(inputStream, outputStream);
    }

    protected void removeXMPMetadata(final InputStream inputStream, final OutputStream outputStream) throws IOException, ImageReadException
    {
        new JpegXmpRewriter().removeXmpXml(inputStream, outputStream);
    }

    public de.dpa.esb.imaging.common.ImageMetadata readMetaData(final InputStream imageData) throws Exception
    {

        byte[] imageBuffer = ByteStreams.toByteArray(imageData);
        imageData.close();

        de.dpa.esb.imaging.common.ImageMetadata toReturn = new de.dpa.esb.imaging.common.ImageMetadata();
        try
        {
            readIPTCAndExifMetadata(imageBuffer, toReturn);
        }
        catch (Throwable t)
        {
            logger.warn("Error while reading IPTC and/or EXIF image meta data: " + t);
        }

        try
        {
            readXMPMetadata(imageBuffer, toReturn);
        }
        catch (Throwable t)
        {
            logger.warn("Error while reading XMP image meta data: " + t);
        }

        return toReturn;
    }

    private void readXMPMetadata(final byte[] imageData, final de.dpa.esb.imaging.common.ImageMetadata imageMetadata)
            throws XMPException, IOException, ImageReadException
    {

        String xmpXml = new JpegImageParser().getXmpXml(new ByteSourceArray(imageData), new HashMap<String, Object>());

        XMPMeta metaData = XMPMetaFactory.parse(new ByteArrayInputStream(xmpXml.getBytes()));

        List<XMPMetadata> xmpMetadatas = new XMPMetadataFactory().buildXMPMetadata(metaData.iterator());
        imageMetadata.addXMPMetadata(xmpMetadatas);
    }

    protected void readIPTCAndExifMetadata(final byte[] imageData, final de.dpa.esb.imaging.common.ImageMetadata imageMetadata)
            throws Exception
    {
        IImageMetadata metaDataRecord = Imaging.getMetadata(imageData, null);

        for (IImageMetadata.IImageMetadataItem item : metaDataRecord.getItems())
        {
            if (item instanceof TiffImageMetadata.Item)
            {
                TiffField tiffField = ((TiffImageMetadata.Item) item).getTiffField();
                imageMetadata.addExifEntry(tiffField.getTagName(), tiffField.getValue().toString());
            }
            else
            {
                if (item instanceof ImageMetadata.Item)
                {
                    ImageMetadata.Item genericItem = (ImageMetadata.Item) item;
                    imageMetadata.addIPTCEntry(genericItem.getKeyword(), genericItem.getText());
                }
            }
        }
    }

    public void removeAllMetadata(final InputStream inputStream, final OutputStream outputStream)
            throws ImageWriteException, ImageReadException, IOException
    {
        ByteArrayOutputStream bufferWithoutIPTC = new ByteArrayOutputStream();
        removeIPTCMetadata(inputStream, bufferWithoutIPTC);
        ByteArrayOutputStream bufferWithoutIPCTAndExif = new ByteArrayOutputStream();
        removeExifMetadata(new ByteArrayInputStream(bufferWithoutIPTC.toByteArray()), bufferWithoutIPCTAndExif);

        boolean canThrow = false;
        try
        {
            removeXMPMetadata(new ByteArrayInputStream(bufferWithoutIPCTAndExif.toByteArray()), outputStream);
            canThrow = true;
        }
        finally
        {
            IoUtils.closeQuietly(canThrow, outputStream);
        }
    }

    public void writeMetadata(final InputStream inputStream, final de.dpa.esb.imaging.common.ImageMetadata imageMetadata,
            final OutputStream outputStream) throws ImageWriteException, IOException, XMPException, ImageReadException
    {
        new ImageMetadataWriter(new JpegXmpRewriter(), new JpegIptcRewriter(),timezone).write(inputStream, imageMetadata, outputStream);
    }
}
