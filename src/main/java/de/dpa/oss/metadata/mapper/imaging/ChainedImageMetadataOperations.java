package de.dpa.oss.metadata.mapper.imaging;

import com.adobe.xmp.XMPException;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteStreams;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifTool;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolWrapper;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.XMPMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author oliver langer
 */
public class ChainedImageMetadataOperations
{
    private static Logger logger = LoggerFactory.getLogger(ChainedImageMetadataOperations.class);

    Path tmpFileDir = null;
    final private InputStream inputImage;
    final private OutputStream modifiedImage;
    private ListMultimap<String, String> tagsToSet = null;
    private ExifTool.CodedCharset iptcCodedCharset = null;
    private TimeZone timeZone = TimeZone.getDefault();
    private final ListMultimap<String, String> groupToSpecificLocationToRemove = ArrayListMultimap.create();
    private boolean clearAllMetadataGroups = false;

    public static ChainedImageMetadataOperations modifyImage(final InputStream inputImage, final OutputStream modifiedImage)
    {
        return new ChainedImageMetadataOperations(inputImage, modifiedImage);
    }

    private ChainedImageMetadataOperations(final InputStream inputImage, final OutputStream modifiedImage)
    {
        this.inputImage = inputImage;
        this.modifiedImage = modifiedImage;
    }

    public ChainedImageMetadataOperations useTemporaryDirectory(final Path tmpFileDir)
    {
        this.tmpFileDir = tmpFileDir;
        return this;
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

    @SuppressWarnings("UnusedDeclaration") public ChainedImageMetadataOperations useTimeZone(final TimeZone timeZone)
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

    public ChainedImageMetadataOperations clearAllMetadataGroups()
    {
        this.clearAllMetadataGroups = true;
        return this;
    }

    public ChainedImageMetadataOperations clearMetadataGroups(final Map<String, String> tagGroupsToClear)
    {
        for (String group : tagGroupsToClear.keySet())
        {
            groupToSpecificLocationToRemove.put(group, tagGroupsToClear.get(group));
        }

        return this;
    }

    public void execute(final ExifToolWrapper exifTool) throws XMPException, ExifToolIntegrationException, IOException
    {
        Path tmpFilePath = null;
        try
        {   // copy input image to tempoary file
            if (tmpFileDir != null)
            {
                tmpFilePath = Files.createTempFile(tmpFileDir, "metadata-mapper", "tmpimage");
            }
            else
            {
                tmpFilePath = Files.createTempFile("metadata-mapper", "tmpimage");
            }

            logger.debug("Using temporary file \"" + tmpFilePath + "\" for image processing.");
            try (OutputStream fos = Files.newOutputStream(tmpFilePath))
            {
                ByteStreams.copy(inputImage, fos);
            }

            ExifTool exifToolOperationChainBuilder = ExifTool.modifyImage(tmpFilePath.toFile());
            exifToolOperationChainBuilder.overwriteOriginalFile(true);

            if (tagsToSet != null)
            {
                exifToolOperationChainBuilder.setImageMetadata(tagsToSet);
            }
            if (iptcCodedCharset != null)
            {
                exifToolOperationChainBuilder.useEncodingCharsetForIPTC(iptcCodedCharset);
            }

            if (clearAllMetadataGroups)
            {
                exifToolOperationChainBuilder.clearAllTagGroups();
            }
            else if (groupToSpecificLocationToRemove.size() > 0)
            {
                for (Map.Entry<String, String> groupAndSpecificLocation : groupToSpecificLocationToRemove.entries())
                {
                    exifToolOperationChainBuilder.clearTagGroup(groupAndSpecificLocation.getKey(), groupAndSpecificLocation.getValue());
                }
            }

            exifToolOperationChainBuilder.execute(exifTool);
            try (InputStream inputStream = Files.newInputStream(tmpFilePath))
            {
                //Files.co
                ByteStreams.copy(inputStream, modifiedImage);
            }
        }
        finally
        {
            if (tmpFilePath != null)
            {
                Files.delete(tmpFilePath);
            }
        }
    }
}
