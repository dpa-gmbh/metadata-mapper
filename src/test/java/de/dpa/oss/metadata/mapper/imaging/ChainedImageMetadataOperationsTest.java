package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifTool;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.InputStream;

public class ChainedImageMetadataOperationsTest
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
        new G2ToMetadataMapper( ImageMetadataUtil.getDefaultMapping()).mapToImageMetadata(document, imageMetadata);

        // when
        ChainedImageMetadataOperations.modifyImage(imageInputStream,fileOutputStream)
                .setMetadata( imageMetadata)
                .execute(ExifTool.anExifTool().build());
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
        MappingType mapping = new MetadataMappingConfigReader().readCustomizedDefaultConfig(mappingConfig);

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper( mapping ).mapToImageMetadata(document, imageMetadata);

        // when
        ChainedImageMetadataOperations.modifyImage(imageInputStream,fileOutputStream)
                .setMetadata(imageMetadata)
                .execute(ExifTool.anExifTool().build());
        fileOutputStream.flush();
        fileOutputStream.close();

        // then
    }
}