package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.metadata.mapper.MetadataMapperUtil;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.XMPMapsTo;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.math.BigInteger;
import java.util.List;

public class MetadataMapperUtilTest
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
        MetadataMapperUtil.validate(mapping);

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
        MetadataMapperUtil.validate(mapping);

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
        MetadataMapperUtil.validate(mapping);
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
        MetadataMapperUtil.validate(mapping);
    }

    @Test
    public void shouldValidateDPAMapping() throws ExifToolIntegrationException, ConfigValidationException, JAXBException
    {
        // given
        MappingType dpaMapping = MetadataMapperUtil.getDefaultMapping();

        // when
        MetadataMapperUtil.validate(dpaMapping);

        // then it should not throw any exception
    }
}