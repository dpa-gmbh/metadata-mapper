package de.dpa.oss.metadata.mapper;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import de.dpa.oss.common.StringCharacterMappingTable;
import de.dpa.oss.metadata.mapper.imaging.ConfigStringCharacterMappingBuilder;
import de.dpa.oss.metadata.mapper.imaging.ImageMetadataUtil;
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
    @Argument(alias = "i", required = true, description = "filename of input image")
    private static String inputImage;

    @Argument(alias = "o", required = true, description = "filename of resulting image")
    private static String outputImage;

    @Argument(alias = "d", required = true, description = "filename of input G2 document")
    private static String g2doc;

    //@Argument(alias = "k", required = false, description = "keep existing metadata. By default existing metadata will be removed")
    //private static Boolean keepExistingMetadata = false;

    public static final String DEFAULT_MAPPING = "mapping/dpa-mapping.xml";

    @Argument(alias = "m", required = false, description = "")
    private static String mapping = null;

    @Argument(required = false, description = "print config infos. If given no mapping will be performed. "
            + "Supported arguments: m - print mapping table", delimiter = ",")
    private static String[] print = null;

    private static void performMapping() throws Exception
    {
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

    private static void printMappingTable() throws FileNotFoundException, JAXBException
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

    private static void printMode() throws FileNotFoundException, JAXBException
    {
        if (print.length == 0)
        {
            Args.usage(MetadataMapper.class);
            System.exit(1);
        }

        for (int i = 0; i < print.length; i++)
        {
            switch (print[i])
            {
            case "m":
                printMappingTable();
                break;
            default:
                System.out.println("* Unsuppored option for print argument: " + print[i]);
                Args.usage(MetadataMapper.class);
                System.exit(1);
            }
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
            if (parameterValidate())
            {
                if (print == null)
                {
                    performMapping();
                }
                else
                {
                    printMode();
                }
            }
            else
            {
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            System.out.println("* ERROR while accessing giving files");
            System.exit(1);
        }
        catch (Exception e)
        {
            System.out.println("Unclassified error during mapping:" + e);
        }
    }
}
