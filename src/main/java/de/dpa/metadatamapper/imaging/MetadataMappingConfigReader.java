package de.dpa.metadatamapper.imaging;

import de.dpa.metadatamapper.imaging.configuration.generated.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * @author oliver langer
 */
public class MetadataMappingConfigReader
{
    private static Logger logger = LoggerFactory.getLogger(MetadataMappingConfigReader.class);

    public Mapping readConfig(final InputStream is) throws JAXBException
    {
        final JAXBContext jaxbContext = JAXBContext.newInstance(de.dpa.metadatamapper.imaging.configuration.generated.Mapping.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object unmarshalled = unmarshaller.unmarshal(is);

        if( unmarshalled == null )
        {
            logger.info( "No mapping configuration file found. Unable to map G2 document data to images");
            return null;
        }
        else
        {
            return ((Mapping) unmarshalled);
        }
    }
}
