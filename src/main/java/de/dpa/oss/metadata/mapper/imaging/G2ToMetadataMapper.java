package de.dpa.oss.metadata.mapper.imaging;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import de.dpa.oss.common.StringCharacterMapping;
import de.dpa.oss.metadata.mapper.common.DateTimeUtils;
import de.dpa.oss.metadata.mapper.common.YAXPathExpressionException;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.*;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Note: instantiate this class only once in order to safe preparation time which consists of reading and
 * interpreting the mapping configuration
 *
 * @author oliver langer
 */
public class G2ToMetadataMapper
{
    private static Logger logger = LoggerFactory.getLogger(G2ToMetadataMapper.class);

    private final List<MetadataProcessingInfo> metadataProcessingInfos;
    private final String mappingName;
    private StringCharacterMapping xmpStringCharacterMapping = null;
    private EncodingCharset xmpCharacterCharset = EncodingCharset.UTF8;
    private final IIMappingToImageMetadata iiMappingToImageMetadata;
    private EncodingCharset iimCharacterCharset;

    public G2ToMetadataMapper(final MappingType mapping)
    {
        mappingName = mapping.getName();
        metadataProcessingInfos = new ArrayList<>();
        for (MappingType.Metadata metadataMapping : mapping.getMetadata())
        {
            metadataProcessingInfos.add(new MetadataProcessingInfo(metadataMapping));
        }

        final Map<String, ConfigType.DateParser> idToDateParser;
        if (mapping.getConfig() != null)
        {
            idToDateParser = configureDateParser(mapping.getConfig());

            configureXMPMapping(mapping.getConfig());
        }
        else
        {
            idToDateParser = new HashMap<>();
        }

        iiMappingToImageMetadata = configureIIMMapping(mapping.getConfig(), idToDateParser);
    }

    private Map<String, ConfigType.DateParser> configureDateParser(final ConfigType config)
    {
        Map<String, ConfigType.DateParser> result = new HashMap<>();
        if (config.getDateParser() == null)
        {
            return result;
        }

        for (ConfigType.DateParser dateParser : config.getDateParser())
        {
            result.put(dateParser.getId(), dateParser);
        }

        return result;
    }

    private void configureXMPMapping(final ConfigType config)
    {
        if (config.getXmp() != null)
        {
            final CharacterMappingType mappingConfig;
            if (config.getXmp().getCharacterMappingRef() != null)
            {
                mappingConfig = (CharacterMappingType) config.getXmp().getCharacterMappingRef();
            }
            else
            {
                mappingConfig = null;
            }

            xmpStringCharacterMapping = ConfigStringCharacterMappingBuilder.stringCharacterMappingBuilder()
                    .withMappingConfigurartion(mappingConfig)
                    .withTargetCharsetAndFallbackReplacementChar(EncodingCharset.of(config.getXmp().getCharset()),
                            config.getXmp().getDefaultReplaceChar())
                    .build();
            if (config.getXmp().getCharset() != null)
            {
                xmpCharacterCharset = EncodingCharset.of(config.getXmp().getCharset());
            }
        }
    }

    private IIMappingToImageMetadata configureIIMMapping(final ConfigType config, final Map<String, ConfigType.DateParser> idToDateParser)
    {
        StringCharacterMapping iimStringCharacterMapping = null;
        iimCharacterCharset = EncodingCharset.ISO_8859_1;

        if (config != null)
        {
            if (config.getIim() != null)
            {
                final CharacterMappingType mappingConfig;
                if (config.getIim().getCharacterMappingRef() != null)
                {
                    mappingConfig = (CharacterMappingType) config.getIim().getCharacterMappingRef();
                }
                else
                {
                    mappingConfig = null;
                }
                iimStringCharacterMapping = ConfigStringCharacterMappingBuilder.stringCharacterMappingBuilder()
                        .withMappingConfigurartion(mappingConfig)
                        .withTargetCharsetAndFallbackReplacementChar(EncodingCharset.of(config.getIim().getCharset()),
                                config.getIim().getDefaultReplaceChar())
                        .build();
                if (config.getIim().getCharset() != null)
                {
                    iimCharacterCharset = EncodingCharset.of(config.getIim().getCharset());
                }
            }
        }

        if (iimStringCharacterMapping == null)
        {
            iimStringCharacterMapping = ConfigStringCharacterMappingBuilder.stringCharacterMappingBuilder().build();
        }
        return new IIMappingToImageMetadata(iimStringCharacterMapping, idToDateParser);
    }

    /**
     * TODO
     * experimental state: XMP mapping info is incomplete. Only root field is being shown.
     */
    public void explainMapToImageMetadata(final Document document, final Writer writer)
            throws YAXPathExpressionException, IOException
    {
        writer.write("Explain Metadata Mapping Config: " + mappingName);
        logger.info("Explaining Metadata Mapping: " + mappingName);
        for (MetadataProcessingInfo metadataProcessingInfo : metadataProcessingInfos)
        {
            writer.write("\nmetadata name:" + metadataProcessingInfo.getMappingName());
            logger.debug("Processing mapping " + metadataProcessingInfo.getMappingName());
            ListMultimap<String, String> partnameToSelectedValue = metadataProcessingInfo.selectXPathValues(document);
            Iterator<String> partNames = metadataProcessingInfo.getPartNames();

            ImageMetadata imageMetadata = new ImageMetadata();
            while (partNames.hasNext())
            {

                final String partname = partNames.next();
                writer.write("\n\tpart: " + partname);

                List<String> values = partnameToSelectedValue.get(partname);

                if (values.size() > 1)
                {
                    writer.write("\n\t\tG2 Values:");

                    for (String value : values)
                    {
                        writer.write("\n\t\t\t\"" + value + "\"");
                    }
                }
                else if (values.size() == 1)
                {
                    writer.write("\n\t\tG2 Value:\"" + values.get(0) + "\"");
                }

                mapToIIM(imageMetadata, partnameToSelectedValue, metadataProcessingInfo.getIIMapping());
                mapToXMP(imageMetadata, partnameToSelectedValue, metadataProcessingInfo.getXMPMapping());


                writer.write("\n\t\tXPath expressions (in order of execution):");
                Iterator<QualifiedXPath> xPathsForPartName = metadataProcessingInfo.getXPathsForPartName(partname);
                while (xPathsForPartName.hasNext())
                {
                    QualifiedXPath qualifiedXPath = xPathsForPartName.next();
                    int rank = 1;
                    if (qualifiedXPath.getRank() != null)
                    {
                        rank = qualifiedXPath.getRank().intValue();
                    }
                    writer.write("\n\t\t\tRank=" + rank + " XPath=" + qualifiedXPath.getValue());
                }

                if (metadataProcessingInfo.getIIMapping() != null)
                {
                    writer.write("\n\t\tIIM Mapping:");
                    List<IIMMapping.MapsTo> mapsTo = metadataProcessingInfo.getIIMapping().getMapsTo();
                    for (IIMMapping.MapsTo iimTarget : mapsTo)
                    {

                        if (Strings.isNullOrEmpty(iimTarget.getPartRef()))
                        {
                            writer.write("\n\t\t\tField=" + iimTarget.getField());
                        }
                        else if (iimTarget.getPartRef().equals(partname))
                        {
                            writer.write("\n\t\t\tField=" + iimTarget.getField());
                        }

                        if (imageMetadata.getIptcEntries().containsKey(iimTarget.getField()))
                        {
                            List<String> mappedValues = imageMetadata.getIptcEntries().get(iimTarget.getField());
                            if (mappedValues.size() > 1)
                            {
                                writer.write("\n\t\t\tValues:");

                                for (String value : mappedValues)
                                {
                                    writer.write("\n\t\t\t\t\"" + value + "\"");
                                }
                            }
                            else if (mappedValues.size() == 1)
                            {
                                writer.write("\n\t\t\tValue:\"" + mappedValues.get(0) + "\"");
                            }
                        }
                    }
                }

                // TODO
                if (metadataProcessingInfo.getXMPMapping() != null)
                {
                    writer.write("\n\t\tXMP Mapping Target:");
                    List<XMPMapsTo> mapsTo = metadataProcessingInfo.getXMPMapping().getMapsTo();
                    for (XMPMapsTo xmpMapsTo : mapsTo)
                    {
                        if (Strings.isNullOrEmpty(xmpMapsTo.getPartRef()))
                        {
                            writer.write("\n\t\t\tField=" + xmpMapsTo.getField());
                        }
                        else if (xmpMapsTo.getPartRef().equals(partname))
                        {
                            writer.write("\n\t\t\tField=" + xmpMapsTo.getField());
                        }
                    }
                }
            }

        }
    }

    public void mapToImageMetadata(final Document document, final ImageMetadata imageMetadata) throws YAXPathExpressionException
    {
        logger.debug("Mapping metadata to XMP and IIM structs");
        for (MetadataProcessingInfo metadataProcessingInfo : metadataProcessingInfos)
        {
            logger.debug("Processing mapping " + metadataProcessingInfo.getMappingName());
            ListMultimap<String, String> partnameToSelectedValue = metadataProcessingInfo.selectXPathValues(document);
            imageMetadata.setIimCharset(iimCharacterCharset);
            mapToIIM(imageMetadata, partnameToSelectedValue, metadataProcessingInfo.getIIMapping());

            imageMetadata.setXmpCharset(xmpCharacterCharset);
            mapToXMP(imageMetadata, partnameToSelectedValue, metadataProcessingInfo.getXMPMapping());
        }
    }

    private void mapToXMP(final ImageMetadata imageMetadata,
            final ListMultimap<String, String> partnameToSelectedValue,
            final XMPMapping xmpMapping)
    {
        if (xmpMapping == null)
        {
            return;
        }
        for (XMPMapsTo xmpMapsTo : xmpMapping.getMapsTo())
        {
            XMPRootCollection rootMetadataCollection = new XMPRootCollection();

            mapToXMP(xmpMapsTo, partnameToSelectedValue, rootMetadataCollection, false);
            for (XMPMetadata xmpMetadata : rootMetadataCollection.getMetadata())
            {
                imageMetadata.addXMPMetadata(xmpMetadata);
            }
        }
    }

    private void mapToXMP(final XMPMapsTo xmpMapsTo,
            final ListMultimap<String, String> partnameToSelectedValue, final XMPCollection collection,
            final boolean multipleValuesValid)
    {

        XMPMappingTargetType targetType = xmpMapsTo.getTargetType();
        if (targetType == null)
        {
            return;
        }
        String partReference = xmpMapsTo.getPartRef();
        if (Strings.isNullOrEmpty(partReference))
        {
            partReference = MetadataProcessingInfo.DEFAULT_PARTNAME;
        }

        if (!partnameToSelectedValue.containsKey(partReference))
        {
            /* no data available for target field. If the target field is a primitive type (STRING, DATE,...) then mapping
               ends here
              */
            if (targetType != XMPMappingTargetType.BAG && targetType != XMPMappingTargetType.SEQUENCE
                    && targetType != XMPMappingTargetType.STRUCT)
            {
                return;
            }
        }

        switch (targetType)
        {
        case TEXT:
            for (String value : partnameToSelectedValue.get(partReference))
            {
                collection.add(new XMPString(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField(), xmpStringCharacterMapping.map(value)));
                if (!multipleValuesValid)
                {
                    // only one element for the root collection
                    break;
                }
            }
            break;
        case INTEGER:
            for (String value : partnameToSelectedValue.get(partReference))
            {
                try
                {
                    collection.add(new XMPInteger(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField(),
                            Integer.parseInt(xmpStringCharacterMapping.map(value))));
                }
                catch (NumberFormatException ex)
                {
                    logger.warn("Integer value expected for partReference=\"" + partReference + "\" but got this value:" + value);
                }
                if (!multipleValuesValid)
                {
                    // only one element for the root collection
                    break;
                }
            }
            break;
        case LANG_ALT:
            for (String value : partnameToSelectedValue.get(partReference))
            {
                collection.add(new XMPLocalizedText(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField(), "x-default",
                        xmpStringCharacterMapping.map(value)));
                if (!multipleValuesValid)
                {
                    // only one element for the root collection
                    break;
                }
            }
            break;
        case DATE:
            try
            {
                for (String value : partnameToSelectedValue.get(partReference))
                {
                    Date date = DateTimeUtils.parseDate(xmpStringCharacterMapping.map(value));
                    collection.add(new XMPDate(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField(), date));
                    if (!multipleValuesValid)
                    {
                        // only one element for the root collection
                        break;
                    }
                }
            }
            catch (Throwable e)
            {
                logger.error("Unable to parse and map date field \"" + partReference + "\" with date string: "
                        + partnameToSelectedValue.get(partReference).get(0));
            }
            break;
        case BAG:
            XMPBag xmpBagNamedMetadata = new XMPBag(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField()
            );

            mapToXMPComplexType(xmpBagNamedMetadata, xmpMapsTo, partnameToSelectedValue, true);
            if (xmpBagNamedMetadata.hasItems())
            {
                collection.add(xmpBagNamedMetadata);
            }
            break;
        case SEQUENCE:
            XMPSequence xmpSequenceNamedMetadata = new XMPSequence(xmpMapsTo.getTargetNamespace(),
                    xmpMapsTo.getField());
            mapToXMPComplexType(xmpSequenceNamedMetadata, xmpMapsTo, partnameToSelectedValue, true);
            if (xmpSequenceNamedMetadata.hasItems())
            {
                collection.add(xmpSequenceNamedMetadata);
            }
            break;
        case STRUCT:
            XMPStruct xmpStructMetadata = new XMPStruct(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField());
            mapToXMPComplexType(xmpStructMetadata, xmpMapsTo, partnameToSelectedValue, false);
            if (xmpStructMetadata.hasItems())
            {
                collection.add(xmpStructMetadata);
            }
            break;
        default:
            logger.info("Mapping of XMP type \"" + targetType.name() + "\" not supported");
        }
    }

    private void mapToXMPComplexType(final XMPCollection targetCollection, final XMPMapsTo rootMappingItem,
            final ListMultimap<String, String> partnameToSelectedValue, final boolean multipleValuesValid)
    {
        for (XMPMapsTo innerElements : rootMappingItem.getMapsTo())
        {
            mapToXMP(innerElements, partnameToSelectedValue, targetCollection, multipleValuesValid);
        }
    }

    private void mapToIIM(final ImageMetadata imageMetadata,
            final ListMultimap<String, String> partnameToSelectedValue,
            final IIMMapping iiMapping)
    {
        if (iiMapping == null)
        {
            return;
        }

        for (IIMMapping.MapsTo mapsTo : iiMapping.getMapsTo())
        {
            String partRef = mapsTo.getPartRef();
            if (Strings.isNullOrEmpty(partRef))
            {
                partRef = MetadataProcessingInfo.DEFAULT_PARTNAME;
            }

            if (partnameToSelectedValue.containsKey(partRef))
            {
                iiMappingToImageMetadata.map(mapsTo, partnameToSelectedValue.get(partRef), imageMetadata);
            }
        }
    }
}
