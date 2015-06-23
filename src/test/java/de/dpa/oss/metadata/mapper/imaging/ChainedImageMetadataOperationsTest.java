package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.MetadataMapperConfigReader;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolWrapper;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
        new G2ToMetadataMapper(MetadataMapperConfigReader.getDefaultMapping()).mapToImageMetadata(document, imageMetadata);

        // when
        ChainedImageMetadataOperations.modifyImage(imageInputStream, fileOutputStream)
                .setMetadata(imageMetadata)
                .execute(ExifToolWrapper.anExifTool().build());
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
        MappingType mapping = new MetadataMapperConfigReader().readCustomConfigOverridingDefault(mappingConfig);

        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper(mapping).mapToImageMetadata(document, imageMetadata);

        // when
        ChainedImageMetadataOperations.modifyImage(imageInputStream, fileOutputStream)
                .setMetadata(imageMetadata)
                .execute(ExifToolWrapper.anExifTool().build());
        fileOutputStream.flush();
        fileOutputStream.close();

        // then
    }

    @Test
    public void shouldNotLeaveAnyTemporaryFileInConfiguredTempFolder() throws Exception
    {
        Path tmpDir = null;
        try
        {
            // given
            tmpDir = Files.createTempDirectory("tmpdir");

            InputStream imageInputStream = ResourceUtil.resourceAsStream("/content/imageMetadata/testimage.jpg", this.getClass());
            FileOutputStream fileOutputStream = new FileOutputStream(
                    "target/" + this.getClass().getSimpleName() + "shouldWriteMetadata.jpg");

            InputStream mappingConfig = ResourceUtil.resourceAsStream("/content/imageMetadata/simple-test-mapping.xml", this.getClass());
            String xmlDocument = ResourceUtil.resourceAsString("/content/imageMetadata/simple-test-inputfile.xml", this.getClass());
            Document document = XmlUtils.toDocument(xmlDocument);
            MappingType mapping = new MetadataMapperConfigReader().readCustomConfigOverridingDefault(mappingConfig);

            ImageMetadata imageMetadata = new ImageMetadata();
            new G2ToMetadataMapper(mapping).mapToImageMetadata(document, imageMetadata);

            // when
            ChainedImageMetadataOperations
                    .modifyImage(imageInputStream, fileOutputStream)
                    .useTemporaryDirectory(tmpDir )
                    .setMetadata(imageMetadata)
                    .execute(ExifToolWrapper.anExifTool().build());
            fileOutputStream.flush();
            fileOutputStream.close();

            // then
            assertThat(Files.newDirectoryStream(tmpDir).iterator().hasNext(), is(false));
        }
        finally
        {
            Files.delete(tmpDir);
        }
    }
}