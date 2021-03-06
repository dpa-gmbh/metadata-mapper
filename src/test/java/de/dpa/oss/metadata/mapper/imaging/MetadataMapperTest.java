package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.metadata.mapper.MetadataMapper;
import de.dpa.oss.metadata.mapper.MetadataMapperConfigReader;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMapsTo;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.fail;

public class MetadataMapperTest
{
    @Test
    public void shouldValidateXMPEntry() throws ExifToolIntegrationException, ConfigValidationException
    {
        // given
        MappingType mapping = new MappingType();
        List<MappingType.Metadata> metadataList = mapping.getMetadata();
        XMPMapping xmpMapping = new XMPMapping();
        List<XMPMapsTo> mapsToList = xmpMapping.getMapsTo();
        MappingType.Metadata metadata = new MappingType.Metadata();
        metadata.setXmp(xmpMapping);
        metadataList.add(metadata);

        XMPMapsTo xmpMapsTo = new XMPMapsTo();
        mapsToList.add( xmpMapsTo );
        xmpMapsTo.setTargetNamespace("http://purl.org/dc/elements/1.1/");
        xmpMapsTo.setField("contributor");

        // when
        MetadataMapper.validate(mapping);

        // then no exception should be thrown
    }

    @Test
    public void shouldValidateXMPAndIIMEntries() throws ExifToolIntegrationException, ConfigValidationException
    {
        // given
        MappingType mapping = new MappingType();
        List<MappingType.Metadata> metadataList = mapping.getMetadata();
        XMPMapping xmpMapping = new XMPMapping();
        List<XMPMapsTo> mapsToList = xmpMapping.getMapsTo();
        MappingType.Metadata metadata = new MappingType.Metadata();

        metadata.setXmp(xmpMapping);
        metadataList.add(metadata);

        XMPMapsTo xmpMapsTo = new XMPMapsTo();
        mapsToList.add( xmpMapsTo );
        xmpMapsTo.setTargetNamespace("http://purl.org/dc/elements/1.1/");
        xmpMapsTo.setField("contributor");

        IIMMapping iimMapping = new IIMMapping();
        metadata.setIim(iimMapping);
        List<IIMMapping.MapsTo> iimMapsToList = iimMapping.getMapsTo();
        IIMMapping.MapsTo iimMapsTo = new IIMMapping.MapsTo();

        iimMapsTo.setField( "Keywords" );
        iimMapsTo.setDataset(BigInteger.valueOf(25));
        iimMapsToList.add(iimMapsTo);

        // when
        MetadataMapper.validate(mapping);

        // then no exception should be thrown
    }


    @Test(expected = ConfigValidationException.class)
    public void shouldThrowValidateExceptionIfXMPTagIsUnknown() throws ExifToolIntegrationException, ConfigValidationException
    {
        // given
        MappingType mapping = new MappingType();
        List<MappingType.Metadata> metadataList = mapping.getMetadata();
        XMPMapping xmpMapping = new XMPMapping();
        List<XMPMapsTo> mapsToList = xmpMapping.getMapsTo();
        MappingType.Metadata metadata = new MappingType.Metadata();
        metadata.setXmp(xmpMapping);
        metadataList.add(metadata);

        XMPMapsTo xmpMapsTo = new XMPMapsTo();
        mapsToList.add( xmpMapsTo );
        xmpMapsTo.setTargetNamespace("http://purl.org/dc/elements/1.1/");

        // when
        xmpMapsTo.setField("UnknownTagName");
        MetadataMapper.validate(mapping);
    }

    @Test(expected = ConfigValidationException.class)
    public void shouldThrowExceptionIfGroupIsUnknown() throws ExifToolIntegrationException, ConfigValidationException
    {
        // given
        MappingType mapping = new MappingType();
        List<MappingType.Metadata> metadataList = mapping.getMetadata();
        XMPMapping xmpMapping = new XMPMapping();
        List<XMPMapsTo> mapsToList = xmpMapping.getMapsTo();
        MappingType.Metadata metadata = new MappingType.Metadata();
        metadata.setXmp(xmpMapping);
        metadataList.add(metadata);

        XMPMapsTo xmpMapsTo = new XMPMapsTo();
        mapsToList.add( xmpMapsTo );
        xmpMapsTo.setTargetNamespace("UnknownTargetNamespace");
        xmpMapsTo.setField("contributor");

        // when
        MetadataMapper.validate(mapping);
    }

    @Test
    public void shouldValidateDefaultMapping() throws ExifToolIntegrationException, ConfigValidationException, JAXBException
    {
        // given
        MappingType defaultMapping = MetadataMapperConfigReader.getDefaultMapping();

        // when
        MetadataMapper.validate(defaultMapping);

        // then it should not throw any exception
    }

    @Test
    public void shouldValidateDPAMapping()
            throws ExifToolIntegrationException, ConfigValidationException, JAXBException, FileNotFoundException
    {
        // given
        MappingType defaultMapping = MetadataMapperConfigReader.getDefaultConfigOverridenBy("example/dpa-mapping.xml");

        // when
        MetadataMapper.validate(defaultMapping);

        // then it should not throw any exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionIfTemporaryDirectoryDoesNotExist() throws FileNotFoundException
    {
        // given
        MetadataMapper metadataMapper = MetadataMapper.modifyImageAt(this.getClass().getResourceAsStream("/content/150529-96-00696.jpeg"));

        // when
        metadataMapper.useTemporaryDirectory( "doesNotExist");

        // then
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionIfTemporaryDirectoryIsAFile() throws FileNotFoundException
    {
        // given
        MetadataMapper metadataMapper = MetadataMapper.modifyImageAt(this.getClass().getResourceAsStream("/content/150529-96-00696.jpeg"));

        // when
        metadataMapper.useTemporaryDirectory( "README.md");

        // then
        fail();
    }


}