package de.dpa.metadatamapper;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import de.dpa.metadatamapper.imaging.ImageMetadataUtil;

import java.io.File;
import java.io.IOException;

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

    @Argument(alias = "k", required = false, description = "keep existing metadata. By default existing metadata will be removed")
    private static Boolean keepExistingMetadata = false;

    public static final String DEFAULT_MAPPING = "mapping/mapping-default.xml";

    @Argument(alias = "m", required = false, description = "")
    private static String mapping = null;

    private static void performMapping() throws Exception
    {
        System.out.print("Mapping metadata taken from \"" + g2doc + "\" into image given by input file \"" + inputImage
                + "\", writing result to output file \"" + outputImage + "\". ");

        ImageMetadataUtil imageMetadataUtil = ImageMetadataUtil.modifyImageAt(inputImage);
        
        if( mapping == null )
        {
            System.out.println("Using default mapping.");
            imageMetadataUtil.withIPTCMapping();
        }
        else
        {
            System.out.println("Using mapping file \"" + mapping + "\".");
            imageMetadataUtil.withPathToMapping( mapping);
        }

        if( !keepExistingMetadata )
        {
            imageMetadataUtil.removeMetadataFirst();
        }
        
        imageMetadataUtil.withPathToXMLDocument( g2doc )
                .mapToImage( outputImage );
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
        System.out.println("** MetadataMapper - Copyright (c) 2015 dpa Deutsche Presse-Agentur GmbH");
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
