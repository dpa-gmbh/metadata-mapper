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
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMappingTargetType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMapsTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.io.*;

/**
 * @author oliver langer
 */
public class ImageMetadataUtil
{
    private static Logger logger = LoggerFactory.getLogger(ImageMetadataUtil.class);
    private static TagInfo tagInfo = null;
    private final FileInputStream imageInputStream;
    private Document xmlDocument = null;
    private MappingType mapping = null;
    private boolean emptyTargetTagGroups = false;

    public static ImageMetadataUtil modifyImageAt(final String pathToSourceImage) throws FileNotFoundException
    {
        return new ImageMetadataUtil(pathToSourceImage);
    }

    public ImageMetadataUtil(final String pathToSourceImage) throws FileNotFoundException
    {
        imageInputStream = new FileInputStream(pathToSourceImage);
    }

    public ImageMetadataUtil withPathToXMLDocument(final String pathToXMLDocument) throws Exception
    {
        logger.debug( "Reading XML document :" + pathToXMLDocument);
        String xmlSource = new String(ByteStreams.toByteArray(new FileInputStream(pathToXMLDocument)));
        xmlDocument = XmlUtils.toDocument(xmlSource);
        return this;
    }

    public ImageMetadataUtil withDefaultMapping() throws JAXBException
    {
        logger.debug( "Using DefaultMapping");
        this.mapping = getDefaultMapping();
        return this;
    }

    public ImageMetadataUtil withDefaultMappingOverridenBy(final String pathToMapping) throws FileNotFoundException, JAXBException
    {
        logger.debug( "Overriding default mapping by mapping definitions defined in:" + pathToMapping);
        this.mapping = getDefaultConfigOverridenBy(pathToMapping);
        return this;
    }

    public ImageMetadataUtil emptyTargetTagGroups()
    {
        this.emptyTargetTagGroups = true;
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

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper(mapping).mapToImageMetadata(xmlDocument, imageMetadata);
        ChainedImageMetadataOperations chainedImageMetadataOperations = ChainedImageMetadataOperations
                .modifyImage(imageInputStream, imageOutput);

        if(emptyTargetTagGroups)
        {
            chainedImageMetadataOperations.clearMetadataGroupsReferredByMapping( imageMetadata );
        }

        chainedImageMetadataOperations.setMetadata(imageMetadata);
        chainedImageMetadataOperations.execute(ExifTool.anExifTool().build());

    }

    public static MappingType getDefaultConfigOverridenBy(final String resourcePath, Object caller) throws FileNotFoundException, JAXBException
    {
        return getDefaultConfigOverridenBy(ResourceUtil.resourceAsStream(resourcePath, caller.getClass()));
    }

    public static MappingType getDefaultConfigOverridenBy(final String path) throws FileNotFoundException, JAXBException
    {
        return getDefaultConfigOverridenBy(new FileInputStream(path));
    }

    /**
     * Reads the default configuration and overrides it with the specified one
     */
    public static MappingType getDefaultConfigOverridenBy(final InputStream is) throws JAXBException
    {
        return new MetadataMappingConfigReader().readCustomizedDefaultConfig(is);
    }

    public static MappingType getDefaultMapping() throws JAXBException
    {
        return new MetadataMappingConfigReader().getDefaultConfig();
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
     * @throws ExifToolIntegrationException
     * @throws ConfigValidationException
     */
    public static void validate(final MappingType mappingToValidate)
            throws ExifToolIntegrationException, ConfigValidationException
    {
        TagInfo tagInfo = ImageMetadataUtil.getExifToolTagInfo();

        for (MappingType.Metadata metadata : mappingToValidate.getMetadata())
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
    private static void validateXMPElements(final MappingType.Metadata metadata, final TagInfo tagInfo, final XMPMapsTo mappingItem,
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

