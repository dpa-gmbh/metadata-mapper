package de.dpa.oss.metadata.mapper.imaging;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author oliver langer
 */
public class MetadataMappingConfigReader
{
    private static Logger logger = LoggerFactory.getLogger(MetadataMappingConfigReader.class);

    public static MappingType defaultConfig = null;
    public static final String DEFAULT_MAPPING = "/mapping/default-mapping.xml";

    public CustomizedMappingType readCustomizedDefaultConfig(final InputStream customizationsIS) throws JAXBException
    {
        final CustomizedMappingType customizations = readConfig(customizationsIS, CustomizedMappingType.class);
        final MappingType defaultMapping = getDefaultConfig();
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

        if( customizations.getConfig() == null )
        {
            customizations.setConfig(defaultMapping.getConfig());
        }
        return customizations;
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
