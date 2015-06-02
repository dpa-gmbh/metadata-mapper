package de.dpa.oss.metadata.mapper.imaging;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.dpa.oss.metadata.mapper.common.YAXPathExpressionException;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.*;

/**
 * @author oliver langer
 */
public class MetadataProcessingInfo
{
    private static Logger logger = LoggerFactory.getLogger(MetadataProcessingInfo.class);

    public static final String DEFAULT_PARTNAME = "@__default";

    /**
     * each part may have multiple xpath expressions assigned to it. These expressions are orderd by a rank
     */
    final Map<String, TreeMap<Integer, QualifiedXPath>> partNameToOrderedXPaths;
    final Map<String, String> partnameToDefaultValues;
    final MappingType.Metadata metadataMapping;

    public MetadataProcessingInfo(final MappingType.Metadata metadataMapping)
    {
        this.metadataMapping = metadataMapping;
        partNameToOrderedXPaths = createMapOfXPathsOrderedByRank(metadataMapping);
        partnameToDefaultValues = createMapOfDefaultValues(metadataMapping);
    }

    private Map<String, String> createMapOfDefaultValues(final MappingType.Metadata metadataMapping)
    {
        Map<String, String> toReturn = new HashMap<>();

        for (MappingType.Metadata.Default aDefault : metadataMapping.getDefault())
        {
            if (Strings.isNullOrEmpty(aDefault.getPart()))
            {
                toReturn.put(DEFAULT_PARTNAME, aDefault.getValue());
            }
            else
            {
                toReturn.put(aDefault.getPart(), aDefault.getValue());
            }
        }
        return toReturn;
    }

    /**
     * @return a map where the part maps to the selected values
     */
    public ListMultimap<String, String> selectXPathValues(final Document document) throws YAXPathExpressionException
    {
        ListMultimap<String, String> partnameToValue = ArrayListMultimap.create();
        for (String partname : partNameToOrderedXPaths.keySet())
        {
            List<String> selectedValues = selectFirstMatchingXPathValues(document, partNameToOrderedXPaths.get(partname));
            if (selectedValues != null && selectedValues.size() > 0)
            {
                partnameToValue.putAll(partname, selectedValues);
            }
            else
            {
                if (partnameToDefaultValues.containsKey(partname))
                {
                    partnameToValue.put(partname, partnameToDefaultValues.get(partname));
                }
            }
        }

        /*
         * now use default in cases where no value has been found
         */
        for (String partname : partnameToDefaultValues.keySet())
        {
            if( !partnameToValue.containsKey(partname) || partnameToValue.get(partname).isEmpty())
            {
                partnameToValue.put(partname, partnameToDefaultValues.get(partname));
            }
        }
        return partnameToValue;
    }

    public Iterator<String> getPartNames()
    {
        return partNameToOrderedXPaths.keySet().iterator();
    }

    /**
     * @return list of xpaths belonging to the given partname. The xpaths are given in the order defined by the rank
     */
    public Iterator<QualifiedXPath> getXPathsForPartName( final String partname )
    {
        if( !partNameToOrderedXPaths.containsKey(partname))
        {
            throw new IllegalArgumentException("Unable to find part with name: " + partname);
        }
        return partNameToOrderedXPaths.get(partname).values().iterator();
    }

    private List<String> selectFirstMatchingXPathValues(final Document document,
            final TreeMap<Integer, QualifiedXPath> integerQualifiedXPathTreeMap) throws YAXPathExpressionException
    {
        List<String> toReturn = new ArrayList<>();

        for (QualifiedXPath qualifiedXPath : integerQualifiedXPathTreeMap.values())
        {
            logger.debug("Evaluating xpath \"" + qualifiedXPath.getValue() + "\"");

            try
            {
                if (qualifiedXPath.getReturnType() == XPathReturnType.STRING)
                {
                    String selectedValue = XmlUtils.selectValue(qualifiedXPath.getValue(), document.getDocumentElement());
                    if (!Strings.isNullOrEmpty(selectedValue))
                    {
                        toReturn.add(selectedValue);
                        break;
                    }
                }
                else
                {
                    List<String> selectedNodeValues = XmlUtils.selectValues(qualifiedXPath.getValue(), document.getDocumentElement());
                    if (selectedNodeValues != null && selectedNodeValues.size() > 0)
                    {
                        toReturn.addAll(selectedNodeValues);
                        break;
                    }
                }
            }
            catch (YAXPathExpressionException e)
            {
                logger.error("Error evaluating XPath expression \"" + qualifiedXPath.getValue() + "\", reason: " + e.getMessage());
                throw e;
            }
        }
        return toReturn;
    }

    /**
     * Add xpaths element to a hash map. Order entries by rank
     */
    private Map<String, TreeMap<Integer, QualifiedXPath>> createMapOfXPathsOrderedByRank(final MappingType.Metadata metadata)
    {
        Map<String, TreeMap<Integer, QualifiedXPath>> partnameToOrderedXPath = new HashMap<>();
        for (QualifiedXPath qualifiedXPath : metadata.getXpath())
        {
            final String partName;

            if (Strings.isNullOrEmpty(qualifiedXPath.getPart()))
            {
                partName = DEFAULT_PARTNAME;
            }
            else
            {
                partName = qualifiedXPath.getPart();
            }

            final TreeMap<Integer, QualifiedXPath> rankToQualifiedPath;

            if (partnameToOrderedXPath.containsKey(partName))
            {
                rankToQualifiedPath = partnameToOrderedXPath.get(partName);
            }
            else
            {
                rankToQualifiedPath = new TreeMap<>();
                partnameToOrderedXPath.put(partName, rankToQualifiedPath);
            }

            Integer rank;
            if (qualifiedXPath.getRank() == null)
            {
                rank = 0;
            }
            else
            {
                rank = qualifiedXPath.getRank().intValue();
            }

            rankToQualifiedPath.put(rank, qualifiedXPath);
        }

        return partnameToOrderedXPath;
    }

    public String getMappingName()
    {
        return metadataMapping.getName();
    }

    public IIMMapping getIIMapping()
    {
        return metadataMapping.getIim();
    }

    public XMPMapping getXMPMapping()
    {
        return metadataMapping.getXmp();
    }

}
