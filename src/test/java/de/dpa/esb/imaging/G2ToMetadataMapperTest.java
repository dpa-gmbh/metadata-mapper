package de.dpa.esb.imaging;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import de.dpa.esb.common.ResourceUtil;
import de.dpa.esb.imaging.common.ImageMetadata;
import de.dpa.esb.imaging.common.XmlUtils;
import de.dpa.esb.imaging.configuration.generated.Mapping;
import de.dpa.esb.imaging.xmp.metadata.XMPCollection;
import de.dpa.esb.imaging.xmp.metadata.XMPString;
import de.dpa.esb.imaging.xmp.metadata.XMPStruct;
import org.w3c.dom.Document;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

public class G2ToMetadataMapperTest
{
    // TODO ENABLE @Test
    public void shouldMapSimpleTestInputFile() throws Exception
    {
        // given
        InputStream mappingConfig = ResourceUtil.resourceAsStream("/content/imageMetadata/simple-test-mapping.xml", this);
        String xmlDocument = ResourceUtil.resourceAsString("/content/imageMetadata/simple-test-inputfile.xml",this);

        Document document = XmlUtils.toDocument(xmlDocument);
        Mapping mapping = new MetadataMappingConfigReader().readConfig(mappingConfig);

        G2ToMetadataMapper g2ToMetadataMapper = new G2ToMetadataMapper(mapping);

        // when
        ImageMetadata imageMetadata = new ImageMetadata();
        g2ToMetadataMapper.mapToImageMetadata(document, imageMetadata);

        assertThat(imageMetadata.getXmpMetadata(), hasSize(1));
        assertThat(imageMetadata.getXmpMetadata().get(0), instanceOf(XMPStruct.class));
        XMPStruct structMetadata = (XMPStruct) imageMetadata.getXmpMetadata().get(0);
        assertThat(structMetadata.getMetadata(), hasSize(2));
        assertThat(structMetadata.getMetadata().get(0), instanceOf(XMPString.class));
        assertThat(((XMPString) structMetadata.getMetadata().get(0)).getValue(), is("Hamburg"));
        assertThat(structMetadata.getMetadata().get(1), instanceOf(XMPCollection.class));
        XMPCollection linesArray = (XMPCollection) structMetadata.getMetadata().get(1);
        assertThat(linesArray.getMetadata(), hasSize(2));
        assertThat(((XMPString) linesArray.getMetadata().get(0)).getValue(), is("first line"));
        assertThat(((XMPString) linesArray.getMetadata().get(1)).getValue(), is("second line"));
        // then
    }
}