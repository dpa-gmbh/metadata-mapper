package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPDateTimeFactory;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteStreams;
import de.dpa.oss.metadata.mapper.imaging.ConfigToExifToolTagNames;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline.EntryWriter;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline.RootEntryWriter;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.*;

import java.io.*;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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

    public void writeMetadata(final InputStream inputStream, final de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata imageMetadata,
            final OutputStream outputStream) throws XMPException
    {
        ListMultimap<String, String> tagToValues = ArrayListMultimap.create();
        EntryWriter entryWriter = new RootEntryWriter(tagToValues);
        xmpToTagValues(imageMetadata.getXmpMetadata(), entryWriter);
        iimToTagValues(imageMetadata.getIptcEntries(), entryWriter );

        ExifTool exifTool = new ExifTool();

        File tempImageFile = null;
        try
        {
            tempImageFile = File.createTempFile("metadata-mapper", "tmpimage");
            FileOutputStream fileOutputStream = new FileOutputStream(tempImageFile);
            ByteStreams.copy(inputStream, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            exifTool.setImageMeta(tempImageFile, tagToValues);
            ByteStreams.copy(new FileInputStream(tempImageFile), outputStream);
            tempImageFile.delete();

        }
        catch (Throwable t)
        {
            if (tempImageFile != null)
            {
                tempImageFile.delete();
            }
        }

    }

    private void xmpToTagValues(final List<XMPMetadata> xmpMetadata, final EntryWriter entryWriter)
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
                entryWriter.write(ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()),
                        entry.getName(), entry.getValue());
                return null;
            }

            @Override public Void visitLocalizedText(final XMPLocalizedText entry) throws XMPException
            {
                if (!Strings.isNullOrEmpty(entry.getLanguageRFC3066IDOrXDefault())
                        && !XMPLocalizedText.DEFAULT_LANGUAGE.equalsIgnoreCase(entry.getLanguageRFC3066IDOrXDefault()))
                {
                    entryWriter.write(ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()),
                            entry.getLanguageRFC3066IDOrXDefault(), entry.getLocalizedText());
                }
                else
                {
                    entryWriter.write(ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()),
                            null, entry.getLocalizedText());
                }
                return null;
            }

            @Override public Void visitInteger(final XMPInteger entry) throws XMPException
            {
                entryWriter.write(ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()),
                        entry.getName(), Integer.toString(entry.getValue()));
                return null;
            }

            @Override public Void visitDate(final XMPDate entry) throws XMPException
            {
                if (entry.getDate() != null)
                {
                    GregorianCalendar calendar = new GregorianCalendar(timeZone);
                    calendar.setTime(entry.getDate());
                    XMPDateTime xmpDate = XMPDateTimeFactory.createFromCalendar(calendar);
                    entryWriter.write(ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()),
                            entry.getName(), XMPUtils.convertFromDate(xmpDate));
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
                EntryWriter structEntryWriter = entryWriter.beginStruct(
                        ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()), entry.getName());
                for (XMPMetadata item : entry.getMetadata())
                {
                    xmpToTagValue(structEntryWriter, item);
                }
                structEntryWriter.endStruct();
                return null;
            }

            @Override public Void visitBag(final XMPBag entry) throws XMPException
            {
                EntryWriter arrayEntryWriter = entryWriter.beginArray(
                        ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()), entry.getName());
                for (XMPMetadata item : entry.getItems())
                {
                    xmpToTagValue(arrayEntryWriter, item);
                }

                arrayEntryWriter.endArray();

                return null;
            }

            @Override public Void visitSequence(final XMPSequence entry) throws XMPException
            {
                EntryWriter arrayEntryWriter = entryWriter.beginArray(
                        ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()), entry.getName());
                for (XMPMetadata item : entry.getItems())
                {
                    xmpToTagValue(arrayEntryWriter, item);
                }

                arrayEntryWriter.endArray();

                return null;
            }

            @Override public Void visitAlternatives(final XMPAlternatives entry) throws XMPException
            {
                EntryWriter langAltEntryWriter = entryWriter.beginLangAlt(
                        ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(entry.getNamespace()), entry.getName());
                for (XMPMetadata item : entry.getItems())
                {
                    xmpToTagValue(langAltEntryWriter, item);
                }
                langAltEntryWriter.endLangAlt();

                return null;
            }
        });
    }

    private void iimToTagValues(final ListMultimap<String, String> iptcEntries, final EntryWriter entryWriter)
    {
        for (String tagname : iptcEntries.keySet())
        {
            List<String> values = iptcEntries.get(tagname);
            final EntryWriter currentWriter;
            boolean isArray;
            if( values.size()>1)
            {
                isArray = true;
                currentWriter = entryWriter.beginArray( ConfigToExifToolTagNames.IPTC_EXIFTOOL_NAMESPACEREF, tagname);
            }
            else
            {
                isArray = false;
                currentWriter = entryWriter;
            }

            for (String value : values)
            {
                currentWriter.write(ConfigToExifToolTagNames.IPTC_EXIFTOOL_NAMESPACEREF, tagname, value );
            }

            if( isArray )
            {
                currentWriter.endArray();
            }
        }
    }
}
