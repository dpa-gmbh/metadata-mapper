package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.common.DateTimeUtils;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.*;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class G2ToMetadataMapperTest
{
    @Test
    public void shouldMapXMPMetadata() throws Exception
    {
        // given
        InputStream mappingConfig = ResourceUtil.resourceAsStream("/content/imageMetadata/simple-test-mapping.xml", this);
        String xmlDocument = ResourceUtil.resourceAsString("/content/imageMetadata/simple-test-inputfile.xml",this);

        Document document = XmlUtils.toDocument(xmlDocument);
        MappingType mapping = new MetadataMappingConfigReader().readCustomizedDefaultConfig(mappingConfig);

        G2ToMetadataMapper g2ToMetadataMapper = new G2ToMetadataMapper(mapping);

        // when
        ImageMetadata imageMetadata = new ImageMetadata();
        g2ToMetadataMapper.mapToImageMetadata(document, imageMetadata);

        assertThat( imageMetadata.getXmpMetadata(), is(notNullValue()));
        
        List<XMPMetadata> xmpMetadata = imageMetadata.getXmpMetadata();
        assertThat(xmpMetadata, hasSize(8));
        assertThat(xmpMetadata.get(0), instanceOf(XMPString.class));
        assertThat( ((XMPString) xmpMetadata.get(0)).getValue(), is("A sample string") );
        assertThat(xmpMetadata.get(1), instanceOf( XMPInteger.class));
        assertThat( ((XMPInteger) xmpMetadata.get(1)).getValue(), is( 12) );
        
        assertThat(xmpMetadata.get(2), instanceOf(XMPDate.class));
        Date expectedDate = DateTimeUtils.parseDate("2014-06-19T16:12:40+02:00");
        assertThat(((XMPDate) xmpMetadata.get(2)).getDate(), is(expectedDate));

        assertThat(xmpMetadata.get(3), instanceOf(XMPLocalizedText.class));
        assertThat(((XMPLocalizedText) xmpMetadata.get(3)).getLocalizedText(), is("An example for LangAlt field"));
        assertThat(xmpMetadata.get(4), instanceOf(XMPStruct.class));
        XMPStruct xmpStruct = (XMPStruct) xmpMetadata.get(4);
        assertThat(xmpStruct.getMetadata(), hasSize(2));
        assertThat(xmpStruct.getMetadata().get(0), instanceOf(XMPString.class));
        assertThat(xmpStruct.getMetadata().get(1), instanceOf(XMPString.class));

        assertThat(xmpMetadata.get(5), instanceOf(XMPSequence.class));
        XMPSequence seq = (XMPSequence) xmpMetadata.get(5);
        assertThat( seq.getMetadata(), hasSize(2));
        assertThat( seq.getMetadata().get(0), instanceOf( XMPString.class));
        assertThat( ((XMPString) seq.getMetadata().get(0)).getValue(), is( "SeqItem 1"));
        assertThat( seq.getMetadata().get(1), instanceOf( XMPString.class));
        assertThat( ((XMPString) seq.getMetadata().get(1)).getValue(), is( "SeqItem 2"));
        assertThat(xmpMetadata.get(6), instanceOf(XMPBag.class));
        
        XMPBag bag = (XMPBag) xmpMetadata.get(6);
        assertThat( bag.getMetadata(), hasSize(3));
        assertThat( bag.getMetadata().get(0), instanceOf( XMPString.class));
        assertThat( ((XMPString) bag.getMetadata().get(0)).getValue(), is( "bag1"));
        assertThat( bag.getMetadata().get(1), instanceOf( XMPString.class));
        assertThat( ((XMPString) bag.getMetadata().get(1)).getValue(), is( "bag2"));
        assertThat( bag.getMetadata().get(2), instanceOf( XMPString.class));
        assertThat( ((XMPString) bag.getMetadata().get(2)).getValue(), is( "bag3"));
    }
}