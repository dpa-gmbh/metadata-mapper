package de.dpa.oss.metadata.mapper.imaging;

import com.adobe.xmp.XMPException;
import com.google.common.io.ByteStreams;
import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.common.YAXPathExpressionException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifTool;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagInfo;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.Mapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMappingTargetType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMapsTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.TimeZone;

/**
 * @author oliver langer
 */
public class ImageMetadataUtil
{
    private static Logger logger = LoggerFactory.getLogger(ImageMetadataUtil.class);
    private static TagInfo tagInfo = null;
    public static final String DPA_MAPPING_RESOURCE = "/mapping/dpa-mapping.xml";
    private static Mapping dpaMapping = null;
    private TimeZone timeZone = TimeZone.getDefault();
    private final FileInputStream imageInputStream;
    private Document xmlDocument = null;
    private Mapping mapping = null;
    private boolean removeMetadata = false;

    public static ImageMetadataUtil modifyImageAt(final String pathToSourceImage) throws FileNotFoundException
    {
        return new ImageMetadataUtil(pathToSourceImage);
    }

    public ImageMetadataUtil(final String pathToSourceImage) throws FileNotFoundException
    {
        imageInputStream = new FileInputStream(pathToSourceImage);
    }

    public ImageMetadataUtil withTimeZone(final TimeZone timeZone)
    {
        this.timeZone = timeZone;
        return this;
    }

    public ImageMetadataUtil withPathToXMLDocument(final String pathToXMLDocument) throws Exception
    {
        String xmlSource = new String(ByteStreams.toByteArray(new FileInputStream(pathToXMLDocument)));
        xmlDocument = XmlUtils.toDocument(xmlSource);
        return this;
    }

    public ImageMetadataUtil withIPTCDPAMapping() throws JAXBException
    {
        this.mapping = getDPAMapping();
        return this;
    }

    public ImageMetadataUtil withPathToMapping(final String pathToMapping) throws FileNotFoundException, JAXBException
    {
        this.mapping = readMappingFile(pathToMapping);
        return this;
    }

    public ImageMetadataUtil removeMetadataFirst()
    {
        this.removeMetadata = true;
        return this;
    }

    public void mapToImage(final String pathToResultingImage)
            throws IOException, XMPException, YAXPathExpressionException
    {
        try (FileOutputStream fileOutputStream = new FileOutputStream(pathToResultingImage))
        {
            mapToImage(fileOutputStream);
        }
    }

    /**
     * Note: does not close the output stream
     */
    public void mapToImage(final OutputStream imageOutput)
            throws YAXPathExpressionException, XMPException, IOException
    {
        if (imageOutput == null || xmlDocument == null || mapping == null)
        {
            throw new IllegalArgumentException("At least one parameter (image input, image output, source xml, mapping) is missing");
        }

        InputStream inputStream;

        if (removeMetadata)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            new ImageMetadataOperation(timeZone).removeAllMetadata(imageInputStream, byteArrayOutputStream);
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.reset();
            byteArrayOutputStream.close();
        }
        else
        {
            inputStream = this.imageInputStream;
        }

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper(mapping).mapToImageMetadata(xmlDocument, imageMetadata);
        new ImageMetadataOperation(timeZone).writeMetadata(inputStream, imageMetadata, imageOutput);
    }

    public static Mapping readMappingResource(final String resourcePath, Object caller) throws FileNotFoundException, JAXBException
    {
        return readMapping(ResourceUtil.resourceAsStream(resourcePath, caller.getClass()));
    }

    public static Mapping readMappingFile(final String path) throws FileNotFoundException, JAXBException
    {
        return readMapping(new FileInputStream(path));
    }

    public static Mapping readMapping(final InputStream is) throws JAXBException
    {
        return new MetadataMappingConfigReader().readConfig(is);
    }

    private static synchronized Mapping readDPAMapping()
    {
        if (dpaMapping == null)
        {
            InputStream mappingConfig = ResourceUtil.resourceAsStream(DPA_MAPPING_RESOURCE, ImageMetadataUtil.class);
            try
            {
                dpaMapping = new MetadataMappingConfigReader().readConfig(mappingConfig);
            }
            catch (JAXBException e)
            {
                logger.error("Reading dpa mapping file failed: " + DPA_MAPPING_RESOURCE);
                dpaMapping = new Mapping();
            }
        }

        return dpaMapping;
    }

    public static Mapping getDPAMapping()
    {
        if (dpaMapping == null)
        {
            readDPAMapping();
        }

        return dpaMapping;
    }

    protected static synchronized TagInfo getExifToolTagInfo() throws ExifToolIntegrationException
    {
        if (tagInfo == null)
        {
            // todo cache somehow
            tagInfo = ExifTool.anExifTool().build().getSupportedTagsOfGroups();
        }

        return tagInfo;
    }

    /**
     * Validates the given configuration. In case of an validation error an {@link ConfigValidationException} is thrown
     *
     * @param mappingToValidate
     * @throws ExifToolIntegrationException
     * @throws ConfigValidationException
     */
    public static void validate(final Mapping mappingToValidate)
            throws ExifToolIntegrationException, ConfigValidationException
    {
        TagInfo tagInfo = ImageMetadataUtil.getExifToolTagInfo();

        for (Mapping.Metadata metadata : mappingToValidate.getMetadata())
        {
            if (metadata.getXmp() != null)
            {
                for (XMPMapsTo xmpMapsTo : metadata.getXmp().getMapsTo())
                {
                    validateXMPElements(metadata, tagInfo, xmpMapsTo, "");
                }
            }
            if (metadata.getIim() != null)
            {
                for (IIMMapping.MapsTo iimMapsTo : metadata.getIim().getMapsTo())
                {
                    if (!tagInfo.hasGroupContainingTagWithId(ConfigToExifToolTagNames.IPTC_APPLICATION_TAGGROUP_NAME,
                            iimMapsTo.getDataset().toString()))
                    {
                        throw new ConfigValidationException(metadata.getName(), ConfigToExifToolTagNames.IPTC_APPLICATION_TAGGROUP_NAME,
                                iimMapsTo.getField());
                    }
                }
            }
        }
    }

    /**
     * exiftool uses a compound name scheme: A member of a struct has its structure name as prefix.
     */
    private static void validateXMPElements(final Mapping.Metadata metadata, final TagInfo tagInfo, final XMPMapsTo mappingItem,
            final String strPrefix)
            throws ConfigValidationException
    {
        String targetNamespace = mappingItem.getTargetNamespace();
        String tagGroupname = ConfigToExifToolTagNames.getTagGroupnameByConfigNamespace(targetNamespace);

        if (!tagInfo.hasGroupContainingTagWithId(tagGroupname, strPrefix + mappingItem.getField()))
        {
            throw new ConfigValidationException(metadata.getName(), mappingItem.getTargetNamespace(), strPrefix+mappingItem.getField() );
        }

        if( mappingItem.getTargetType() == XMPMappingTargetType.STRUCT || mappingItem.getTargetType() == XMPMappingTargetType.SEQUENCE
                || mappingItem.getTargetType() == XMPMappingTargetType.BAG )
        {
            final String prefix;
            if( mappingItem.getTargetType() == XMPMappingTargetType.STRUCT )
            {
                prefix = strPrefix + mappingItem.getField();
            }
            else
            {
                prefix = strPrefix;
            }

            for (XMPMapsTo child : mappingItem.getMapsTo())
            {
                validateXMPElements(metadata, tagInfo, child, prefix);
            }
        }
    }
}

