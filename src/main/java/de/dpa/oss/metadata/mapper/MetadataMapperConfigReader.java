package de.dpa.oss.metadata.mapper;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.ConfigType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.CustomizedMappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author oliver langer
 */
public class MetadataMapperConfigReader
{
    private static Logger logger = LoggerFactory.getLogger(MetadataMapperConfigReader.class);

    public static MappingType defaultConfig = null;
    public static final String DEFAULT_MAPPING = "/image-metadata-mapping/default-mapping.xml";

    public static MappingType getDefaultConfigOverridenBy(final String resourcePath, Object caller)
            throws FileNotFoundException, JAXBException
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
        return new MetadataMapperConfigReader().readCustomizedDefaultConfig(is);
    }

    public static MappingType getDefaultMapping() throws JAXBException
    {
        return new MetadataMapperConfigReader().getDefaultConfig();
    }

    /**
     * Reads a customized mapping. This mapping is merged with the default mapping:
     * <ul>
     *     <li>
     *         New metadata mappings are added
     *     </li>
     *     <li>
     *         Metadata mapping which already exist in the default mapping override the default mapping
     *     </li>
     *     <li>
     *         timezone and charset definition for iim and xmp are taken from the override mapping
     *     </li>
     *     <li>
     *         Dateparser are merged in the same way metadata mappings are merged.
     *     </li>
     * </ul>
     */
    public CustomizedMappingType readCustomizedDefaultConfig(final InputStream customizationsIS) throws JAXBException
    {
        final CustomizedMappingType customizations = readConfig(customizationsIS, CustomizedMappingType.class);
        final MappingType defaultMapping = getDefaultConfig();

        mergeMetadataMappings(customizations, defaultMapping );
        mergeConfigs(customizations, defaultMapping);
        return customizations;
    }

    private void mergeConfigs(final CustomizedMappingType customizations, final MappingType defaultMapping)
    {
        if( customizations.getConfig() == null )
        {
            customizations.setConfig(defaultMapping.getConfig());
        }
        else
        {
            /* merge date parsers: */
            List<ConfigType.DateParser> customizedDateParser = customizations.getConfig().getDateParser();
            List<ConfigType.DateParser> defaultDateParser = defaultMapping.getConfig().getDateParser();
            Set<String> mergedParserNames = new HashSet<>();

            List<ConfigType.DateParser> mergedList = customizations.getConfig().getDateParser();
            for (ConfigType.DateParser dateParser : customizedDateParser)
            {
                mergedParserNames.add(dateParser.getId());
            }
            for (ConfigType.DateParser defaultParser : defaultDateParser)
            {
                if( !mergedParserNames.contains(defaultParser.getId()))
                {
                    mergedList.add( defaultParser);
                    mergedParserNames.add(defaultParser.getId());
                }
            }

        }
    }

    private void mergeMetadataMappings(final CustomizedMappingType customizations, final MappingType defaultMapping)
    {
        final List<Metadata> mergedMappings = new ArrayList<>();
        final Set<String> customizedMappings = new HashSet<>();

        for (Metadata customeMetadata : customizations.getMetadata())
        {
            mergedMappings.add( customeMetadata );
            customizedMappings.add(customeMetadata.getName().trim().toLowerCase());
        }

        // now add all default mappings which are not found in the customizedMappings
        for (Metadata mapping : defaultMapping.getMetadata() )
        {
            if( !customizedMappings.contains(mapping.getName().trim().toLowerCase()))
            {
                mergedMappings.add( mapping );
            }
        }

        customizations.getMetadata().clear();
        customizations.getMetadata().addAll( mergedMappings );
    }

    public MappingType getDefaultConfig() throws JAXBException
    {
        return readDefaultConfig();
    }

    private synchronized MappingType readDefaultConfig() throws JAXBException
    {
        if( defaultConfig != null )
        {
            return defaultConfig;
        }

        return readConfig( this.getClass().getResourceAsStream( DEFAULT_MAPPING),
                MappingType.class);
    }

    private <T> T readConfig( final InputStream is, Class<T> clazz ) throws JAXBException
    {
        final JAXBContext jaxbContext = JAXBContext
                .newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<T> root = unmarshaller.unmarshal( new StreamSource(is), clazz);

        if (root== null)
        {
            logger.info("Mapping configuration for given type not found. Type:" + clazz );
            return null;
        }
        else
        {
            return root.getValue();
        }
    }
}
