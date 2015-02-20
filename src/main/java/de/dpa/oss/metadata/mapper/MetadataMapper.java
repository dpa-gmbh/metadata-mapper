package de.dpa.oss.metadata.mapper;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import de.dpa.oss.common.StringCharacterMappingTable;
import de.dpa.oss.metadata.mapper.imaging.ConfigStringCharacterMappingBuilder;
import de.dpa.oss.metadata.mapper.imaging.ConfigValidationException;
import de.dpa.oss.metadata.mapper.imaging.ImageMetadataUtil;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifTool;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.CharacterMappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oliver langer
 */
public class MetadataMapper
{
    @Argument(alias = "i", required = false, description = "filename of input image")
    private static String inputImage = null;

    @Argument(alias = "o", required = false, description = "filename of resulting image")
    private static String outputImage = null;

    @Argument(alias = "d", required = false, description = "filename of input G2 document")
    private static String g2doc = null;

    @Argument(alias = "v", required = false, description = "Validate given mappingCustomization file")
    private static String validateMapping = null;

    @Argument(alias = "e", required = false, description = "Removes all tags from those tag groups which are used by the mappingCustomization. "
        + "By default mapped tag values will be merged with existing tags")
    private static Boolean emptyTagGroupBeforeMapping = false;

    @Argument(alias = "m", required = false, description = "mappingCustomization file which is used to override and/or enhance the default "
            +"mappingCustomization. By default it uses dpa mappingCustomization")
    private static String mappingCustomization = null;

    @Argument(alias = "c", required = false, description = "Outputs configured character mappingCustomization table. Does not perform any mappingCustomization. "
            + "Uses default mappingCustomization file if argument -m is omitted")
    private static boolean printCharacterMappingTable = false;

    @Argument(alias = "t", required = false, description = "Path to exiftool. Alternatively you may set environment variable EXIFTOOL")
    private static String exiftoolPath = null;

    @Argument(alias = "h", required = false)
    private static boolean help = false;

    private static void performMapping() throws Exception
    {
        if (!validateArgsForMapping())
        {
            Args.usage(MetadataMapper.class);
            System.exit(1);
        }

        System.out.println("Mapping metadata taken from \"" + g2doc + "\" into image given by input file \"" + inputImage
                + "\", writing result to output file \"" + outputImage + "\". ");

        ImageMetadataUtil imageMetadataUtil = ImageMetadataUtil.modifyImageAt(inputImage);

        if (mappingCustomization == null)
        {
            System.out.println("Using default mappingCustomization.");
            imageMetadataUtil.withDefaultMapping();
        }
        else
        {
            System.out.println("Using mappingCustomization file \"" + mappingCustomization + "\".");
            imageMetadataUtil.withDefaultMappingOverridenBy(mappingCustomization);
        }

        if( emptyTagGroupBeforeMapping )
        {
            imageMetadataUtil.emptyTargetTagGroups();
        }

        imageMetadataUtil.withPathToXMLDocument(g2doc)
                .mapToImage(outputImage);

        System.out.println( "Mappingperformed successfully");
    }

    private static boolean validateArgsForMapping() throws IOException
    {
        boolean checkSuccessful = true;

        if (inputImage == null)
        {
            System.err.println("* ERROR: input image file not given");
            checkSuccessful = false;
        }
        else
        {
            File fileToCheck = new File(inputImage);
            if (!fileToCheck.exists() || !fileToCheck.isFile() || !fileToCheck.canRead())
            {
                System.err.println("* ERROR: input image file \"" + inputImage + "\" must exists and must be readable");
                checkSuccessful = false;
            }
        }

        if( outputImage == null )
        {
            System.err.println( "* ERROR: output image (-o) not given");
            checkSuccessful = false;
        }

        if (g2doc == null)
        {
            System.err.println("* ERROR: g2doc file not given");
            checkSuccessful = false;
        }
        else
        {
            File fileToCheck = new File(g2doc);
            if (!fileToCheck.exists() || !fileToCheck.isFile() || !fileToCheck.canRead())
            {
                System.err.println("* ERROR: g2 document file\"" + g2doc + "\" must exists and must be readable");
                checkSuccessful = false;
            }
        }

        return checkSuccessful;
    }

    public static final String FORMATTED_OUTPUT_PREFIX = "<html lang=\"en\" class=\"\"><table class=\"mappingTable\">\n"
            + "<tbody><tr>\n"
            + "  <th>Source Unicode<br>Codepoint (HEX)</th>\n"
            + "  <th>Source Character</th>\n"
            + "  <th>Mapped Unicode<br>Codepoint (HEX)</th>\n"
            + "  <th>Mapped Character</th>\n"
            + "  </tr>\n";

    public static final String FORMATTED_OUTPUT_ENTRY = "  <tr class=\"mappingEntry\">\n"
            + "    <td class=\"srcCP\">0x%1$s</td>\n"
            + "    <td class=\"srcChar\">&#x%1$s;</td>\n"
            + "    <td class=\"mappedCP\">0x%3$s</td>\n"
            + "    <td class=\"mappedChar\">&#x%3$s;</td>\n"
            + "  </tr>\n";

    public static final String FORMATTED_OUTPUT_SUFFIX = "</tbody></table></html>";

    private static void printCharacterMappingTable() throws FileNotFoundException, JAXBException
    {
        final MappingType mappingTable;
        if (mappingCustomization == null)
        {
            mappingTable = ImageMetadataUtil.getDefaultMapping();
        }
        else
        {
            mappingTable = ImageMetadataUtil.getDefaultConfigOverridenBy(mappingCustomization);
        }

        Map<Integer, String> codepointAlternativeCharacters = new HashMap<>();

        if (mappingTable.getConfig() != null)
        {
            if (mappingTable.getConfig().getIim().getCharacterMappingRef() != null)
            {
                System.out.println("IIM Character Mapping Table\n");
                StringCharacterMappingTable stringCharacterMapping = ConfigStringCharacterMappingBuilder.stringCharacterMappingBuilder()
                        .withMappingConfigurartion((CharacterMappingType) mappingTable.getConfig().getIim().getCharacterMappingRef())
                        .buildTable();
                System.out.print(FORMATTED_OUTPUT_PREFIX);
                System.out.print(stringCharacterMapping.toString(FORMATTED_OUTPUT_ENTRY, codepointAlternativeCharacters));
                System.out.println(FORMATTED_OUTPUT_SUFFIX);
            }

            if (mappingTable.getConfig().getXmp().getCharacterMappingRef() != null)
            {
                System.out.println("XMP Character Mapping Table\n");
                StringCharacterMappingTable stringCharacterMapping = ConfigStringCharacterMappingBuilder.stringCharacterMappingBuilder()
                        .withMappingConfigurartion((CharacterMappingType) mappingTable.getConfig().getXmp().getCharacterMappingRef())
                        .buildTable();
                System.out.print(FORMATTED_OUTPUT_PREFIX);
                System.out.println(stringCharacterMapping.toString(FORMATTED_OUTPUT_ENTRY, codepointAlternativeCharacters));
                System.out.println(FORMATTED_OUTPUT_SUFFIX);
            }

        }
    }

    private static void validateConfig()
            throws FileNotFoundException, JAXBException, ExifToolIntegrationException, ConfigValidationException
    {
        if (validateMapping == null)
        {
            System.err.println("* ERROR: No mappingCustomization file to validate");
            Args.usage(MetadataMapper.class);
            System.exit(1);
        }

        File file = new File(validateMapping);
        if (!(file.exists() && file.isFile()))
        {
            System.err.println("* ERROR: Unable to read mappingCustomization config: " + validateMapping);
        }

        final MappingType mappingToValidate = ImageMetadataUtil.getDefaultConfigOverridenBy(validateMapping);
        try
        {
            ImageMetadataUtil.validate(mappingToValidate);

            System.out.println("Mapping file \"" + validateMapping + "\" validated successfully.");
            System.exit(0);
        }
        catch (ConfigValidationException ex)
        {
            System.err.println("* ERROR: Validation failed for metadata mappingCustomization named \"" + ex.getMetadataMappingName()
                    + "\" using group reference \"" + ex.getConfiguredNamespace() + "\", field \"" + ex.getConfiguredFieldname() + "\"");
            System.exit(1);
        }
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

        if( help )
        {
            Args.usage( MetadataMapper.class);
            System.exit(0);
        }

        if( exiftoolPath != null )
        {
            ExifTool.setPathToExifTool( exiftoolPath );
        }

        try
        {
            if (printCharacterMappingTable)
            {
                printCharacterMappingTable();
            }
            else
            {
                if( validateMapping != null )
                {
                    validateConfig();
                }
                else
                {
                    performMapping();
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("* ERROR while accessing giving files");
            System.exit(1);
        }
        catch (Exception e)
        {
            System.err.println("* ERROR: Unclassified error during mappingCustomization:" + e);
        }

        System.exit(0);
    }
}
