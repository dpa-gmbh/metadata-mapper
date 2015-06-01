package de.dpa.oss.metadata.mapper;

import com.google.common.base.Strings;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import de.dpa.oss.common.StringCharacterMappingTable;
import de.dpa.oss.metadata.mapper.imaging.ConfigStringCharacterMappingBuilder;
import de.dpa.oss.metadata.mapper.imaging.ConfigValidationException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolIntegrationException;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifToolWrapper;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.CharacterMappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oliver langer
 */
public class MetadataMapperCmd
{
    private static Logger logger = LoggerFactory.getLogger(MetadataMapperCmd.class);

    @Argument(alias = "i", required = false, description = "filename of input image")
    protected static String inputImage = null;

    @Argument(alias = "o", required = false, description = "filename of resulting image")
    protected static String outputImage = null;

    @Argument(alias = "d", required = false, description = "filename of input G2 document")
    protected static String g2doc = null;

    @Argument(alias = "v", required = false, description = "Validate given mappingCustomization file")
    protected static String validateMapping = null;

    @Argument(alias = "m", required = false, description = "mappingCustomization file which is used to override and/or enhance the default "
            + "mappingCustomization. By default it uses dpa mappingCustomization")
    protected static String mappingCustomization = null;

    @Argument(alias = "c", required = false, description =
            "Outputs configured character mappingCustomization table. Does not perform any mappingCustomization. "
                    + "Uses default mappingCustomization file if argument -m is omitted")
    protected static boolean printCharacterMappingTable = false;

    @Argument(alias = "t", required = false, description = "Path to exiftool. Alternatively you may set environment variable EXIFTOOL")
    protected static String exiftoolPath = null;

    @Argument(alias = "e", required = false, description =
            "Removes all tags from those tag groups which are used by the mappingCustomization. "
                    + "By default mapped tag values will be merged with existing tags")
    protected static Boolean emptyTagGroupBeforeMapping = false;

    @Argument(alias = "r", required = false, description = "comma-separated list of metadata tag groups to clear before mapping. "
            + "The syntax needs to match the exiftool syntax to specify containers: TAG_GROUP:TAG. For a list of available "
            + "containers see exiftool. Example: -r IPTC:ALL,XMP:XMP-dc")
    protected static String removeTagGroups;

    @Argument(alias = "R", required = false, description = "Removes all metadata from given file before processing")
    protected static Boolean removeAllTagGroups = false;

    @Argument(alias = "x", required = false, description = "Experimental feature: Dumps mapping information based on a given "
            + "document. At present state the output for XMP is not complete")
    protected static Boolean explainMapping = false;

    @Argument(alias = "h", required = false)
    protected static boolean help = false;


    private static void performMapping() throws Exception
    {
        if (!validateArgsForMapping())
        {
            Args.usage(MetadataMapperCmd.class);
            System.exit(1);
        }

        logger.info("Mapping metadata taken from \"" + g2doc + "\" into image given by input file \"" + inputImage
                + "\", writing result to output file \"" + outputImage + "\". ");

        MetadataMapper metadataMapper = MetadataMapper.modifyImageAt(inputImage);

        if (mappingCustomization == null)
        {
            logger.info("Using default mappingCustomization.");
            metadataMapper.useDefaultMapping();
        }
        else
        {
            logger.info("Using mappingCustomization file \"" + mappingCustomization + "\".");
            metadataMapper.useDefaultMappingOverridenBy(mappingCustomization);
        }

        if (emptyTagGroupBeforeMapping)
        {
            metadataMapper.emptyTargetTagGroups();
        }

        if (removeAllTagGroups)
        {
            logger.info("Removing ALL metadata properties before processing");
            metadataMapper.removeAllTagGroups();
        }
        else if (!Strings.isNullOrEmpty(removeTagGroups))
        {
            String[] tagGroups = removeTagGroups.split(",");
            Map<String, String> tagGroupsToRemove = new HashMap<>();
            for (String tagGroup : tagGroups)
            {
                String[] tagGroupWithTag = tagGroup.split(":");
                if (tagGroupWithTag.length != 2)
                {
                    System.err.println("** Illegal format for tag group to remove: " + tagGroup
                            + ". Required format looks like IPTC:ALL. Ignoring this entry.");
                }
                else
                {
                    tagGroupsToRemove.put(tagGroupWithTag[0], tagGroupWithTag[1]);
                }
            }
            metadataMapper.tagGroupsToRemoveBeforeMapping(tagGroupsToRemove);
        }

        metadataMapper.xmlDocument(g2doc)
                .executeMapping(outputImage);

        logger.info("Mappingperformed successfully");
    }

    private static void explainMapping() throws Exception
    {
        if (!validateArgsForMapping())
        {
            Args.usage(MetadataMapperCmd.class);
            System.exit(1);
        }

        MetadataMapper metadataMapper = MetadataMapper.explainMapping();

        if (mappingCustomization == null)
        {
            metadataMapper.useDefaultMapping();
        }
        else
        {
            metadataMapper.useDefaultMappingOverridenBy(mappingCustomization);
        }

        StringWriter stringWriter = new StringWriter();
        metadataMapper.xmlDocument(g2doc).explainMapping(stringWriter);
        System.out.println(stringWriter.toString());
    }

    private static boolean validateArgsForMapping() throws IOException
    {
        boolean checkSuccessful = true;

        if (explainMapping)
        {
            if (g2doc == null)
            {
                System.err.println("Explain mode requires an input document (-d)");
                checkSuccessful = false;
            }
        }
        else
        {
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

            if (outputImage == null)
            {
                System.err.println("* ERROR: output image (-o) not given");
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
            mappingTable = MetadataMapperConfigReader.getDefaultMapping();
        }
        else
        {
            mappingTable = MetadataMapperConfigReader.getDefaultConfigOverridenBy(mappingCustomization);
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
            Args.usage(MetadataMapperCmd.class);
            System.exit(1);
        }

        File file = new File(validateMapping);
        if (!(file.exists() && file.isFile()))
        {
            System.err.println("* ERROR: Unable to read mappingCustomization config: " + validateMapping);
        }

        final MappingType mappingToValidate = MetadataMapperConfigReader.getDefaultConfigOverridenBy(validateMapping);
        try
        {
            MetadataMapper.validate(mappingToValidate);

            logger.info("Mapping file \"" + validateMapping + "\" validated successfully.");
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
        //System.out.println("** MetadataMapper - Copyright (c) 2015 dpa Deutsche Presse-Agentur GmbH");
        try
        {
            Args.parse(MetadataMapperCmd.class, argv);
        }
        catch (IllegalArgumentException ex)
        {
            Args.usage(MetadataMapperCmd.class);
            System.exit(1);
        }

        if (help)
        {
            Args.usage(MetadataMapperCmd.class);
            System.exit(0);
        }

        if (exiftoolPath != null)
        {
            ExifToolWrapper.setPathToExifTool(exiftoolPath);
        }

        try
        {
            if (printCharacterMappingTable)
            {
                printCharacterMappingTable();
            }
            else
            {
                if (validateMapping != null)
                {
                    validateConfig();
                }
                else if (explainMapping)
                {
                    explainMapping();
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
        catch (Throwable t)
        {
            System.err.println("* ERROR: Unclassified error during mappingCustomization:" + t);
            System.exit(1);
        }

        System.exit(0);
    }
}
