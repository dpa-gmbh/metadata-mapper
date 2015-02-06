package de.dpa.oss.metadata.mapper;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import de.dpa.oss.common.StringCharacterMappingTable;
import de.dpa.oss.metadata.mapper.imaging.ConfigStringCharacterMappingBuilder;
import de.dpa.oss.metadata.mapper.imaging.ConfigValidationException;
import de.dpa.oss.metadata.mapper.imaging.ImageMetadataUtil;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.CharacterMappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.Mapping;

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

    @Argument(alias = "v", required = false, description = "Validate given mapping file")
    private static String validateMapping = null;

    //@Argument(alias = "k", required = false, description = "keep existing metadata. By default existing metadata will be removed")
    //private static Boolean keepExistingMetadata = false;

    public static final String DEFAULT_MAPPING = "mapping/dpa-mapping.xml";

    @Argument(alias = "m", required = false, description = "filename of mapping file. By default it uses dpa mapping")
    private static String mapping = null;

    @Argument(alias = "c", required = false, description = "Outputs configured character mapping table. Does not perform any mapping. "
            + "Uses default mapping file if argument -m is omitted")
    private static boolean printCharacterMappingTable = false;

    private static void performMapping() throws Exception
    {
        if (!validateArgsForMapping())
        {
            Args.usage(MetadataMapper.class);
            System.exit(1);
        }

        System.out.print("Mapping metadata taken from \"" + g2doc + "\" into image given by input file \"" + inputImage
                + "\", writing result to output file \"" + outputImage + "\". ");

        ImageMetadataUtil imageMetadataUtil = ImageMetadataUtil.modifyImageAt(inputImage);

        if (mapping == null)
        {
            System.out.println("Using default mapping.");
            imageMetadataUtil.withIPTCDPAMapping();
        }
        else
        {
            System.out.println("Using mapping file \"" + mapping + "\".");
            imageMetadataUtil.withPathToMapping(mapping);
        }

        /**
         if (!keepExistingMetadata)
         {
         imageMetadataUtil.removeMetadataFirst();
         }*/

        imageMetadataUtil.withPathToXMLDocument(g2doc)
                .mapToImage(outputImage);
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
        final Mapping mappingTable;
        if (mapping == null)
        {
            mappingTable = ImageMetadataUtil.getDPAMapping();
        }
        else
        {
            mappingTable = ImageMetadataUtil.readMappingFile(mapping);
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
            System.err.println("* ERROR: No mapping file to validate");
            Args.usage(MetadataMapper.class);
            System.exit(1);
        }

        File file = new File(validateMapping);
        if (!(file.exists() && file.isFile()))
        {
            System.err.println("* ERROR: Unable to read mapping config: " + validateMapping);
        }

        Mapping mappingToValidate = ImageMetadataUtil.readMappingFile(validateMapping);
        try
        {
            ImageMetadataUtil.validate(mappingToValidate);

            System.out.println("Mapping file \"" + validateMapping + "\" validated successfully.");
            System.exit(0);
        }
        catch (ConfigValidationException ex)
        {
            System.err.println("* ERROR: Validation failed for metadata mapping named \"" + ex.getMetadataMappingName()
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
            System.err.println("* ERROR: Unclassified error during mapping:" + e);
        }

        System.exit(0);
    }
}
