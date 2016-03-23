package de.dpa.oss.metadata.mapper;

import com.adobe.xmp.XMPException;
import com.google.common.io.ByteStreams;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.common.YAXPathExpressionException;
import de.dpa.oss.metadata.mapper.imaging.ChainedImageMetadataOperations;
import de.dpa.oss.metadata.mapper.imaging.ConfigToExifToolTagNames;
import de.dpa.oss.metadata.mapper.imaging.ConfigValidationException;
import de.dpa.oss.metadata.mapper.imaging.G2ToMetadataMapper;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolWrapper;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagGroupItem;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class offers the metadata mapper functionality. Its purpose is to construct the desired functionality and to
 * call it. Typical use:
 * <pre>
 *   MetadataMapper.modifyImageAt( "image.jpg").withXMLDocument(xmldoc).withDefaultMapping().mapToImage(resultImage)
 * </pre>
 *
 * @author oliver langer
 */
public class MetadataMapper
{
    private static Logger logger = LoggerFactory.getLogger(MetadataMapper.class);
    private static TagInfo tagInfo = null;
    private final InputStream imageInputStream;
    private Document xmlDocument = null;
    private MappingType mapping = null;
    private boolean emptyTargetTagGroups = false;
    private Map<String, String> tagGroupsToClear = null;
    private boolean removeAllTagGroups = false;
    private Path temporaryDirectory = null;

    public static MetadataMapper modifyImageAt(final String pathToSourceImage) throws FileNotFoundException
    {
        return new MetadataMapper(pathToSourceImage);
    }

    public static MetadataMapper modifyImageAt(final InputStream sourceImage) throws FileNotFoundException
    {
        return new MetadataMapper(sourceImage);
    }

    public static MetadataMapper explainMapping()
    {
        return new MetadataMapper();
    }

    private MetadataMapper(final String pathToSourceImage) throws FileNotFoundException
    {
        this(new FileInputStream(pathToSourceImage));
    }

    private MetadataMapper()
    {
        imageInputStream = null;
    }

    private MetadataMapper(final InputStream imageInputStream)
    {
        this.imageInputStream = imageInputStream;
    }

    public MetadataMapper useTemporaryDirectory( final String temporaryDirectoryPath )
    {
        temporaryDirectory = Paths.get(temporaryDirectoryPath);

        if( !Files.isDirectory(temporaryDirectory))
        {
            throw new IllegalArgumentException( "Given path does not point to a directory:" + temporaryDirectoryPath);
        }
        return this;
    }

    public MetadataMapper xmlDocument(final String pathToXMLDocument) throws Exception
    {
        logger.debug("Reading XML document :" + pathToXMLDocument);
        String xmlSource = new String(ByteStreams.toByteArray(new FileInputStream(pathToXMLDocument)));
        xmlDocument = XmlUtils.toDocument(xmlSource);
        return this;
    }

    public MetadataMapper xmlDocument(final Document xmlDocument)
    {
        this.xmlDocument = xmlDocument;
        return this;
    }

    public MetadataMapper useDefaultMapping() throws JAXBException
    {
        logger.debug("Using DefaultMapping");
        this.mapping = MetadataMapperConfigReader.getDefaultMapping();
        return this;
    }

    /**
     * @deprecated inheritance not supported in future
     */
    public MetadataMapper useDefaultMappingOverridenBy(final String pathToMapping) throws FileNotFoundException, JAXBException
    {
        logger.debug("Overriding default mapping by mapping definitions defined in:" + pathToMapping);
        this.mapping = MetadataMapperConfigReader.getDefaultConfigOverridenBy(pathToMapping);
        return this;
    }

    public MetadataMapper useCustomMapping( final Path mappingConfigFile ) throws FileNotFoundException, JAXBException
    {
        logger.debug( "Using custom mapping");
        this.mapping = MetadataMapperConfigReader.readCustomConfig(new FileInputStream(mappingConfigFile.toFile()));
        return this;
    }

    public MetadataMapper useDefaultMappingOverridenBy(final MappingType customMapping)
    {
        logger.debug("Overriding default mapping by mapping:" + customMapping.getName());
        this.mapping = customMapping;
        return this;
    }

    /**
     * Expects a list of metadata mapper to remove before mapping is being performed
     *
     * @param tagGroupsToClear list of tagGroupsToClear to erase. exiftool format is expected. that is: GROUP:TAG
     *                         E.g. IPTC:ALL, XMP:XMP-dc
     */
    public MetadataMapper tagGroupsToRemoveBeforeMapping(final Map<String, String> tagGroupsToClear)
    {
        this.tagGroupsToClear = tagGroupsToClear;
        return this;
    }

    public MetadataMapper removeAllTagGroups()
    {
        this.removeAllTagGroups = true;
        return this;
    }

    public MetadataMapper emptyTargetTagGroups()
    {
        this.emptyTargetTagGroups = true;
        return this;
    }

    public void executeMapping(final String pathToResultingImage)
            throws IOException, XMPException, YAXPathExpressionException, ExifToolIntegrationException
    {
        try (FileOutputStream fileOutputStream = new FileOutputStream(pathToResultingImage))
        {
            executeMapping(fileOutputStream);
        }
    }

    /**
     * Note: does not close the output stream
     */
    public void executeMapping(final OutputStream imageOutput)
            throws YAXPathExpressionException, XMPException, IOException, ExifToolIntegrationException
    {
        if (imageOutput == null || xmlDocument == null || mapping == null)
        {
            throw new IllegalArgumentException("At least one parameter (image input, image output, source xml, mapping) is missing");
        }

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper(mapping).mapToImageMetadata(xmlDocument, imageMetadata);
        ChainedImageMetadataOperations chainedImageMetadataOperations = ChainedImageMetadataOperations
                .modifyImage(imageInputStream, imageOutput);

        if( temporaryDirectory != null )
        {
            chainedImageMetadataOperations.useTemporaryDirectory(temporaryDirectory);
        }

        if (emptyTargetTagGroups)
        {
            chainedImageMetadataOperations.clearMetadataGroupsReferredByMapping(imageMetadata);
        }

        if (removeAllTagGroups)
        {
            chainedImageMetadataOperations.clearAllMetadataGroups();
        }
        else if (tagGroupsToClear != null && tagGroupsToClear.size() > 0)
        {
            chainedImageMetadataOperations.clearMetadataGroups(tagGroupsToClear);
        }

        chainedImageMetadataOperations.setMetadata(imageMetadata);
        chainedImageMetadataOperations.execute(ExifToolWrapper.anExifTool().build());
    }

    /**
     * Used to dump the mapping. The dump includes the xpaths, the selected values and the list of target
     * fields.
     *
     * Note: this implementation is in experimental state. The XMP-part is not dumped out accordingly.
     */
    public void explainMapping( final Writer writer ) throws IOException, YAXPathExpressionException
    {
        if (writer == null || xmlDocument == null || mapping == null)
        {
            throw new IllegalArgumentException("At least one parameter (writer, source xml, mapping) is missing");
        }

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper(mapping).explainMapToImageMetadata(xmlDocument, writer);
    }

    protected static synchronized TagInfo getExifToolTagInfo() throws ExifToolIntegrationException
    {
        if (tagInfo == null)
        {
            // todo cache somehow
            tagInfo = ExifToolWrapper.anExifTool().build().getSupportedTagsOfGroups();
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
        TagInfo tagInfo = MetadataMapper.getExifToolTagInfo();

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
                    else
                    {
                        TagGroupItem tagInfoById = tagInfo.getGroupByName(ConfigToExifToolTagNames.IPTC_APPLICATION_TAGGROUP_NAME)
                                .getTagInfoById(iimMapsTo.getDataset().toString());
                        if (!tagInfoById.getName().equals(iimMapsTo.getField()))
                        {
                            throw new ConfigValidationException(metadata.getName(), ConfigToExifToolTagNames.IPTC_APPLICATION_TAGGROUP_NAME,
                                    iimMapsTo.getField());
                        }
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
            throw new ConfigValidationException(metadata.getName(), mappingItem.getTargetNamespace(), strPrefix + mappingItem.getField());
        }

        if (mappingItem.getTargetType() == XMPMappingTargetType.STRUCT || mappingItem.getTargetType() == XMPMappingTargetType.SEQUENCE
                || mappingItem.getTargetType() == XMPMappingTargetType.BAG)
        {
            final String prefix;
            if (mappingItem.getTargetType() == XMPMappingTargetType.STRUCT)
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

