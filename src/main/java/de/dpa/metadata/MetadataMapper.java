package de.dpa.metadata;

import com.google.common.io.ByteStreams;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import de.dpa.metadata.common.ResourceUtil;
import de.dpa.metadata.imaging.G2ToMetadataMapper;
import de.dpa.metadata.imaging.ImageMetadataOperation;
import de.dpa.metadata.imaging.MetadataMappingConfigReader;
import de.dpa.metadata.imaging.common.ImageMetadata;
import de.dpa.metadata.imaging.common.XmlUtils;
import de.dpa.metadata.imaging.configuration.generated.Mapping;
import org.w3c.dom.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author oliver langer
 */
public class MetadataMapper
{
    @Argument(alias = "i", required = true, description = "filename of input image")
    private static String inputImage;

    @Argument(alias = "o", required = true, description = "filename of resulting image")
    private static String outputImage;

    @Argument(alias = "d", required = true, description = "filename of input G2 document")
    private static String g2doc;

    public static final String DEFAULT_MAPPING = "mapping/mapping-default.xml";

    @Argument(alias = "m", required = false, description = "")
    private static String mapping = null;

    private static void performMapping() throws Exception
    {
        System.out.print("Mapping metadata taken from \"" + g2doc + "\" into image given by input file \"" + inputImage
                + "\", writing result to output file \"" + outputImage + "\". ");

        InputStream mappingInpuStream;

        if( mapping == null )
        {
            System.out.println( "Using default mapping.");
            mappingInpuStream = ResourceUtil.resourceAsStream( "/mapping/mapping-default.xml", MetadataMapper.class);
        }
        else
        {
            System.out.println( "Using mapping file \"" + mapping + "\".");
            mappingInpuStream = new FileInputStream( mapping);
        }

        Mapping configuredMapping = new MetadataMappingConfigReader().readConfig(mappingInpuStream);

        FileInputStream imageInputStream = new FileInputStream(inputImage);
        FileOutputStream imageOutputStream = new FileOutputStream(outputImage);
        String g2xml = new String( ByteStreams.toByteArray(new FileInputStream( g2doc )) );
        Document g2DOM = XmlUtils.toDocument(g2xml);

        ImageMetadata imageMetadata = new ImageMetadata();

        new G2ToMetadataMapper( configuredMapping).mapToImageMetadata( g2DOM, imageMetadata );

        new ImageMetadataOperation().writeMetadata( imageInputStream, imageMetadata, imageOutputStream );
        imageOutputStream.close();
    }

    private static boolean parameterValidate() throws IOException
    {
        boolean checkSuccessful = true;

        File fileToCheck = new File(inputImage);
        if (!fileToCheck.exists() || !fileToCheck.isFile() || !fileToCheck.canRead())
        {
            System.out.println("* ERROR: input image file \"" + inputImage + "\" must exists and must be readable");
            checkSuccessful = false;
        }

        fileToCheck = new File(g2doc);
        if (!fileToCheck.exists() || !fileToCheck.isFile() || !fileToCheck.canRead())
        {
            System.out.println("* ERROR: g2 document file\"" + g2doc + "\" must exists and must be readable");
            checkSuccessful = false;
        }

        return checkSuccessful;
    }

    public static void main(String argv[])
    {
        System.out.println("** MetadataMapper");
        try
        {
            Args.parse(MetadataMapper.class, argv);
        }
        catch (IllegalArgumentException ex)
        {
            Args.usage(MetadataMapper.class);
            System.exit(1);
        }

        try
        {
            if (parameterValidate())
            {
                performMapping();
            }
            else
            {
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            System.out.println( "* ERROR while accessing giving files");
            System.exit(1);
        }
        catch (Exception e)
        {
            System.out.println( "Unclassified error during mapping:" + e );
        }
    }
}
