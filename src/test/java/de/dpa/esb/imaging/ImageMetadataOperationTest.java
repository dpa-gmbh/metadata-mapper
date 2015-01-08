package de.dpa.esb.imaging;

import de.dpa.esb.common.ResourceUtil;
import de.dpa.esb.imaging.common.ImageMetadata;
import de.dpa.esb.imaging.common.XmlUtils;
import de.dpa.esb.imaging.configuration.generated.Mapping;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageMetadataOperationTest
{
    @Test
    public void shouldWriteMetadata() throws Exception
    {
        // given
        InputStream imageInputStream = ResourceUtil.resourceAsStream("/content/imageMetadata/testimage.jpg", this.getClass());
        FileOutputStream fileOutputStream = new FileOutputStream(this.getClass().getSimpleName() + "shouldWriteMetadata.jpg");

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