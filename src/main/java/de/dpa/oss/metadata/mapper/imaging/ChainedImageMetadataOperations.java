package de.dpa.oss.metadata.mapper.imaging;

import com.adobe.xmp.XMPException;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteStreams;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifTool;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.XMPMetadata;

import java.io.*;
import java.util.*;

/**
 * @author oliver langer
 */
public class ChainedImageMetadataOperations
{
    final private InputStream inputImage;
    final private OutputStream modifiedImage;
    private ListMultimap<String, String> tagsToSet = null;
    private ExifTool.CodedCharset iptcCodedCharset = null;
    private TimeZone timeZone = TimeZone.getDefault();
    private final ListMultimap<String, String> groupToSpecificLocationToRemove = ArrayListMultimap.create();


    public static ChainedImageMetadataOperations modifyImage(final InputStream inputImage, final OutputStream modifiedImage)
    {
        return new ChainedImageMetadataOperations(inputImage, modifiedImage);
    }

    private ChainedImageMetadataOperations(final InputStream inputImage, final OutputStream modifiedImage)
    {
        this.inputImage = inputImage;
        this.modifiedImage = modifiedImage;
    }

    public ChainedImageMetadataOperations setMetadata(final ImageMetadata imageMetadata) throws XMPException
    {
        tagsToSet = ImageMetadataToExifToolTagInfoFactory
                .createExifToolTagInfo(imageMetadata, timeZone);

        if (imageMetadata.getIimCharset() != null)
        {
            switch (imageMetadata.getIimCharset())
            {
            case ISO_8859_1:
                iptcCodedCharset = ExifTool.CodedCharset.LATIN1;
                break;
            default:
                iptcCodedCharset = ExifTool.CodedCharset.UTF8;
            }
        }
        return this;
    }

    public ChainedImageMetadataOperations useTimeZone(final TimeZone timeZone)
    {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * The intention of this method is to avoid a mixture of metadata filled by a particular mapping and already existing metadata
     * in the file. This method collects all tag groups(like e. g. IPTC:ALL, XMP:XMP-dc, ...) referred by the mapping.
     * Then it removes of these groups from the file
     *
     * @param metadataMapping underlying metadata mapper
     * @return
     */
    public ChainedImageMetadataOperations clearMetadataGroupsReferredByMapping(final ImageMetadata metadataMapping)
    {
        if (metadataMapping.getIptcEntries().size() > 0)
        {
            groupToSpecificLocationToRemove.put("IPTC", "ALL");
        }

        Set<String> usedXMPNamespaces = new HashSet<>();
        for (XMPMetadata xmpMetadata : metadataMapping.getXmpMetadata())
        {
            String namespace = xmpMetadata.getNamespace();
            if (!Strings.isNullOrEmpty(namespace))
            {
                usedXMPNamespaces.add(namespace);
            }
        }

        for (String usedXMPNamespace : usedXMPNamespaces)
        {
            groupToSpecificLocationToRemove.put("XMP", ConfigToExifToolTagNames.getExiftoolNamespaceRefForConfigNamspace(usedXMPNamespace));
        }

        return this;
    }

    public void execute(final ExifTool exifTool) throws XMPException
    {

        File tempImageFile = null;
        try
        {   // copy input image to tempoary file
            tempImageFile = File.createTempFile("metadata-mapper", "tmpimage");
            FileOutputStream fis = new FileOutputStream(tempImageFile);
            ByteStreams.copy(inputImage, fis);
            fis.close();

            ExifTool.ExifToolOperationChainBuilder exifToolOperationChainBuilder = ExifTool.modifyImage(tempImageFile);

            if (tagsToSet != null)
            {
                exifToolOperationChainBuilder.setImageMetadata(tagsToSet);
            }
            if (iptcCodedCharset != null)
            {
                exifToolOperationChainBuilder.useEncodingCharsetForIPTC(iptcCodedCharset);
            }

            if (groupToSpecificLocationToRemove.size() > 0)
            {
                for (Map.Entry<String,String> groupAndSpecificLocation : groupToSpecificLocationToRemove.entries())
                {
                    exifToolOperationChainBuilder.clearTagGroup(groupAndSpecificLocation.getKey(), groupAndSpecificLocation.getValue());
                }
            }

            exifToolOperationChainBuilder.execute(exifTool);
            FileInputStream modifiedTempStream = new FileInputStream(tempImageFile);
            ByteStreams.copy(modifiedTempStream, modifiedImage);
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
}
