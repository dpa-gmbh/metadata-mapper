package de.dpa.metadatamapper.imaging;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import de.dpa.metadatamapper.common.ExtXPathException;
import de.dpa.metadatamapper.imaging.common.DateTimeUtils;
import de.dpa.metadatamapper.imaging.common.ImageMetadata;
import de.dpa.metadatamapper.imaging.configuration.generated.*;
import de.dpa.metadatamapper.imaging.iptc.IptcFieldToType;
import de.dpa.metadatamapper.imaging.xmp.metadata.*;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcType;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private final ConfigType mappingConfig;
    
    public G2ToMetadataMapper(final Mapping mapping)
    {
        mappingConfig = mapping.getConfig();
        metadataProcessingInfos = new ArrayList<>();
        for (Mapping.Metadata metadataMapping : mapping.getMetadata())
        {
            metadataProcessingInfos.add(new MetadataProcessingInfo(metadataMapping));
        }
        /*if( mapping.getConfig() != null )
        {
            readConfig( );
        }*/
    }

    private void readConfig(final ConfigType config)
    {
        if( config.getIim() != null && config.getIim().getCharacterMappingRef() != null )
        {
        }
    }

    public void mapToImageMetadata(final Document document, final ImageMetadata imageMetadata) throws ExtXPathException
    {
        for (MetadataProcessingInfo metadataProcessingInfo : metadataProcessingInfos)
        {
            logger.debug("Processing mapping " + metadataProcessingInfo.getMappingName());
            ListMultimap<String, String> partnameToSelectedValue = metadataProcessingInfo.selectXPathValues(document);
            mapToIIM(mappingConfig.getIim(), imageMetadata, partnameToSelectedValue, metadataProcessingInfo.getIIMapping());

            mapToXMP(mappingConfig.getXmp(), imageMetadata, partnameToSelectedValue, metadataProcessingInfo.getXMPMapping());
        }
    }

    private void mapToXMP(final ConfigType.Xmp config, final ImageMetadata imageMetadata,
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
                collection.add(new XMPString(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField(), value));
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
                            Integer.parseInt(value)));
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
                collection.add(new XMPLocalizedText(xmpMapsTo.getTargetNamespace(), xmpMapsTo.getField(), "x-default", value));
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
                    Date date = DateTimeUtils.parseDate(value);
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

    private void mapToIIM(final ConfigType.Iim config, final ImageMetadata imageMetadata,
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
                mapToIIM(config, imageMetadata, partnameToSelectedValue.get(partRef), mapsTo);
            }
        }
    }

    private void mapToIIM(final ConfigType.Iim config, final ImageMetadata imageMetadata, final List<String> valueList,
            final IIMMapping.MapsTo mappingInfo)
    {
        if (valueList == null || valueList.isEmpty())
        {
            return;
        }

        IptcType iptcType = IptcFieldToType.asIptcType(mappingInfo.getField());
        if (iptcType == null)
        {
            logger.debug("Mapping unknown IPTCType=\"" + mappingInfo.getField() + "\", DS=" + mappingInfo.getDataset());
            iptcType = IptcTypes.getUnknown(mappingInfo.getDataset().intValue());
        }

        if (mappingInfo.getTargetType() == IIMMappingTargetType.LIST_OF_STRING)
        {
            for (String value : valueList)
            {
                imageMetadata.addIPTCEntry(iptcType.getName(), value);
            }
        }
        else
        {
            imageMetadata.addIPTCEntry(iptcType.getName(), valueList.get(0));
        }
    }
}
