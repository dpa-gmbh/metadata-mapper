package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.imaging.G2ToMetadataMapper;
import de.dpa.oss.metadata.mapper.imaging.ImageMetadataUtil;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.common.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.TimeZone;

public class ExifToolBackendTest
{
    @Test
    public void shouldUseExifTool() throws Exception
    {
        // given
        InputStream imageInputStream = ResourceUtil.resourceAsStream("/content/mapping-dpa-example-image.jpeg", this.getClass());
        FileOutputStream fileOutputStream = new FileOutputStream("target/" + this.getClass().getSimpleName()
                + "shouldUseExifTool.jpg");
        String xmlDocument = ResourceUtil.resourceAsString("/content/mapping-dpa-example-news.xml", this.getClass());
        Document document = XmlUtils.toDocument(xmlDocument);

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper( ImageMetadataUtil.getDPAMapping()).mapToImageMetadata(document, imageMetadata);

        // when
        new ExifToolBackend(TimeZone.getDefault()).writeMetadata(imageInputStream, imageMetadata, fileOutputStream);

        // then
    }

}