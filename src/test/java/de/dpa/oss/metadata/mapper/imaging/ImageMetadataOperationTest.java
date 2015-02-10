package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.Mapping;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.TimeZone;

public class ImageMetadataOperationTest
{
    @Test
    public void shouldMapBasedOnDPAMapping() throws Exception
    {
        // given
        InputStream imageInputStream = ResourceUtil.resourceAsStream("/content/mapping-dpa-example-image.jpeg", this.getClass());
        FileOutputStream fileOutputStream = new FileOutputStream("target/" + this.getClass().getSimpleName() 
                + "shouldMapBasedOnDPAMapping.jpg");
        String xmlDocument = ResourceUtil.resourceAsString("/content/mapping-dpa-example-news.xml", this.getClass());
        Document document = XmlUtils.toDocument(xmlDocument);
        
        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper( ImageMetadataUtil.getDPAMapping()).mapToImageMetadata(document, imageMetadata);

        // when
        new ImageMetadataOperation(TimeZone.getDefault()).writeMetadata(imageInputStream, imageMetadata, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    @Test
    public void shouldWriteMetadata() throws Exception
    {
        // given
        InputStream imageInputStream = ResourceUtil.resourceAsStream("/content/imageMetadata/testimage.jpg", this.getClass());
        FileOutputStream fileOutputStream = new FileOutputStream("target/" + this.getClass().getSimpleName() + "shouldWriteMetadata.jpg");

        InputStream mappingConfig = ResourceUtil.resourceAsStream("/content/imageMetadata/simple-test-mapping.xml", this.getClass());
        String xmlDocument = ResourceUtil.resourceAsString("/content/imageMetadata/simple-test-inputfile.xml", this.getClass());
        Document document = XmlUtils.toDocument(xmlDocument);
        Mapping mapping = new MetadataMappingConfigReader().readConfig(mappingConfig);

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper( mapping ).mapToImageMetadata(document, imageMetadata);

        // when
        new ImageMetadataOperation(TimeZone.getDefault()).writeMetadata( imageInputStream, imageMetadata, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();

        // then
    }
}