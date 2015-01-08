package de.dpa.metadata.imaging;

import de.dpa.metadata.common.ResourceUtil;
import de.dpa.metadata.imaging.common.ImageMetadata;
import de.dpa.metadata.imaging.common.XmlUtils;
import de.dpa.metadata.imaging.configuration.generated.Mapping;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageMetadataOperationTest
{
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
        new ImageMetadataOperation().writeMetadata( imageInputStream, imageMetadata, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();

        // then
    }

    @Test
    public void shouldWorkWithEmptyXMPMetadata()
    {
        // given
        // when
        // then
    }
}