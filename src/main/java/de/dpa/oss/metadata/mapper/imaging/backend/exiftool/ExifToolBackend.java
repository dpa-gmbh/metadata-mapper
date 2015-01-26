package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPDateTimeFactory;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPUtils;
import com.google.common.io.ByteStreams;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.*;

import java.io.*;
import java.util.*;

/**
 * @author oliver langer
 */
public class ExifToolBackend
{
    private TimeZone timeZone;

    public ExifToolBackend(final TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    void writeMetadata(final InputStream inputStream, final de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata imageMetadata,
            final OutputStream outputStream) throws XMPException
    {
        Map<String, String> tagToValues = new HashMap<>();
        EntryWriter entryWriter = new RootEntryWriter(tagToValues);
        xmpToTagValues(imageMetadata.getXmpMetadata(), tagToValues, entryWriter);

        ExifTool exifTool = new ExifTool();

        File tempImageFile = null;
        try
        {
            tempImageFile = File.createTempFile( "metadata-mapper", "tmpimage");
            FileOutputStream fileOutputStream = new FileOutputStream(tempImageFile);
            ByteStreams.copy( inputStream, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            exifTool.setImageMeta(tempImageFile, tagToValues);
            ByteStreams.copy( new FileInputStream(tempImageFile),outputStream);
            tempImageFile.delete();

        }
        catch (Throwable t)
        {
            if( tempImageFile != null )
            {
                tempImageFile.delete();
            }
        }

    }

    private void xmpToTagValues(final List<XMPMetadata> xmpMetadata, final Map<String, String> tagToValues, final EntryWriter entryWriter)
            throws XMPException
    {
        for (XMPMetadata metadata : xmpMetadata)
        {
            xmpToTagValue(entryWriter, metadata);
        }
    }

    private void xmpToTagValue(final EntryWriter entryWriter, final XMPMetadata metadata)
            throws XMPException
    {
        metadata.getType().accept(metadata, new XMPMetadataTypeVisitor<Void>()
        {
            @Override public Void visitString(final XMPString entry) throws XMPException
            {
                entryWriter.write(entry.getName(), entry.getValue());
                return null;
            }

            @Override public Void visitLocalizedText(final XMPLocalizedText entry) throws XMPException
            {
                entryWriter.write(entry.getName(), entry.getLocalizedText());
                return null;
            }

            @Override public Void visitInteger(final XMPInteger entry) throws XMPException
            {
                entryWriter.write(entry.getName(), Integer.toString(entry.getValue()));
                return null;
            }

            @Override public Void visitDate(final XMPDate entry) throws XMPException
            {
                if( entry.getDate() != null )
                {
                    GregorianCalendar calendar = new GregorianCalendar(timeZone);
                    calendar.setTime(entry.getDate());
                    XMPDateTime xmpDate = XMPDateTimeFactory.createFromCalendar(calendar);
                    entryWriter.write(entry.getName(), XMPUtils.convertFromDate(xmpDate));
                }
                return null;
            }

            @Override public Void visitRoot(final XMPRootCollection entry) throws XMPException
            {
                return null;
            }

            @Override public Void visitSchema(final XMPSchema entry) throws XMPException
            {
                throw new UnsupportedOperationException();
            }

            @Override public Void visitQualifier(final XMPQualifier entry) throws XMPException
            {
                throw new UnsupportedOperationException();
            }

            @Override public Void visitStruct(final XMPStruct entry) throws XMPException
            {
                StructEntryWriter structEntryWriter = StructEntryWriter.beginStruct();
                for (XMPMetadata item : entry.getMetadata())
                {
                    xmpToTagValue(structEntryWriter,item);
                }
                String structString = structEntryWriter.endStruct();
                entryWriter.write( entry.getName(), structString );
                return null;
            }

            @Override public Void visitBag(final XMPBag entry) throws XMPException
            {
                ArrayEntryWriter arrayEntryWriter = new ArrayEntryWriter();
                for (XMPMetadata item : entry.getItems())
                {
                    xmpToTagValue(arrayEntryWriter,item);
                }
                entryWriter.write(entry.getName(), arrayEntryWriter.endArray());

                return null;
            }

            @Override public Void visitSequence(final XMPSequence entry) throws XMPException
            {
                ArrayEntryWriter arrayEntryWriter = new ArrayEntryWriter();
                for (XMPMetadata item : entry.getItems())
                {
                    xmpToTagValue(arrayEntryWriter,item);
                }
                entryWriter.write(entry.getName(), arrayEntryWriter.endArray());

                return null;
            }

            @Override public Void visitAlternatives(final XMPAlternatives entry) throws XMPException
            {
                ArrayEntryWriter arrayEntryWriter = new ArrayEntryWriter();
                for (XMPMetadata item : entry.getItems())
                {
                    xmpToTagValue(arrayEntryWriter,item);
                }
                entryWriter.write(entry.getName(), arrayEntryWriter.endArray());

                return null;
            }
        });
    }
}
