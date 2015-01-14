package de.dpa.oss.metadata.mapper.imaging;

import com.adobe.xmp.XMPException;
import com.google.common.io.ByteStreams;
import de.dpa.oss.metadata.mapper.MetadataMapper;
import de.dpa.oss.metadata.mapper.common.ExtXPathException;
import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.Mapping;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.io.*;

/**
 * @author oliver langer
 */
public class ImageMetadataUtil
{
    private final FileInputStream imageInputStream;
    private Document xmlDocument = null;
    private Mapping mapping = null;
    private boolean removeMetadata = false;

    public static ImageMetadataUtil modifyImageAt(final String pathToSourceImage) throws FileNotFoundException
    {
        return new ImageMetadataUtil(pathToSourceImage);
    }

    public ImageMetadataUtil(final String pathToSourceImage) throws FileNotFoundException
    {
        imageInputStream = new FileInputStream(pathToSourceImage);
    }

    public ImageMetadataUtil withPathToXMLDocument(final String pathToXMLDocument) throws Exception
    {
        String xmlSource = new String(ByteStreams.toByteArray(new FileInputStream(pathToXMLDocument)));
        xmlDocument = XmlUtils.toDocument(xmlSource);
        return this;
    }

    public ImageMetadataUtil withIPTCDPAMapping() throws JAXBException
    {
        InputStream mappingInputStream = ResourceUtil.resourceAsStream("/mapping/dpa-mapping.xml", MetadataMapper.class);
        this.mapping = new MetadataMappingConfigReader().readConfig(mappingInputStream);
        return this;
    }

    public ImageMetadataUtil withPathToMapping(final String pathToMapping) throws FileNotFoundException, JAXBException
    {
        FileInputStream mappingInputStream = new FileInputStream(pathToMapping);
        this.mapping = new MetadataMappingConfigReader().readConfig(mappingInputStream);
        return this;
    }

    public ImageMetadataUtil removeMetadataFirst()
    {
        this.removeMetadata = true;
        return this;
    }
    
    public void mapToImage(final String pathToResultingImage)
            throws IOException, ImageWriteException, XMPException, ExtXPathException, ImageReadException
    {
        try (FileOutputStream fileOutputStream = new FileOutputStream(pathToResultingImage))
        {
            mapToImage(fileOutputStream);
        }
    }

    /**
     * Note: does not close the output stream
     */
    public void mapToImage(final OutputStream imageOutput)
            throws ExtXPathException, ImageWriteException, ImageReadException, XMPException, IOException
    {
        if (imageOutput == null || xmlDocument == null || mapping == null)
        {
            throw new IllegalArgumentException("At least one parameter (image input, image output, source xml, mapping) is missing");
        }
        
        InputStream inputStream;
        
        if( removeMetadata )
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            new ImageMetadataOperation().removeAllMetadata( imageInputStream, byteArrayOutputStream);
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.reset();
            byteArrayOutputStream.close();
        }
        else
        {
            inputStream = this.imageInputStream;
        }
        
        ImageMetadata imageMetadata = new ImageMetadata();
        new G2ToMetadataMapper(mapping).mapToImageMetadata(xmlDocument, imageMetadata);
        new ImageMetadataOperation().writeMetadata(inputStream, imageMetadata, imageOutput);
    }
}

