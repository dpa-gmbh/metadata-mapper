package de.dpa.oss.metadata.mapper;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolWrapper;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

/**
 * @author oliver langer
 */
public class MappingTest
{
    @Test
    public void shouldUseCustomMapping_DigitalImageGUID() throws Exception
    {
        // given
        InputStream imageInputStream = ResourceUtil.resourceAsStream("/content/mapping-dpa-example-image.jpeg", this.getClass());
        String xmlDocument = ResourceUtil.resourceAsString("/content/guid-example.xml", this.getClass());
        Document document = XmlUtils.toDocument(xmlDocument);

        // when
        MetadataMapper.modifyImageAt(imageInputStream)
                .useDefaultMappingOverridenBy("example/dpa-mapping.xml")
                .xmlDocument(document)
                .executeMapping("target/" + this.getClass().getSimpleName()
                        + "-shouldApplyCustomizedMapping.jpg");

        // then
        File generatedJPG = new File("target/" + this.getClass().getSimpleName()
                + "-shouldApplyCustomizedMapping.jpg");
        List iptc = ExifToolWrapper.anExifTool().build().readTagGroup(generatedJPG, "IPTC:ALL");
        Map<String,String> tagValues;
        tagValues = (Map<String,String>) iptc.get(0);
        assertThat( tagValues, hasEntry("ObjectName", "urn:newsml:dpa.com:20090101:150105-99-07656"));
    }
}
