package de.dpa.oss.metadata.mapper.imaging.iptc;

import org.apache.commons.imaging.formats.jpeg.iptc.IptcTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oliver langer
 */
public class IptcFieldToType
{
    static private Map<String, IptcTypes> nameToType = new HashMap<>();

    static
    {
        for (IptcTypes iptcTypes : IptcTypes.values())
        {
            nameToType.put(iptcTypes.getName(), iptcTypes);
        }
    }

    public static IptcTypes asIptcType(final String fieldname)
    {
        if (nameToType.containsKey(fieldname))
        {
            return nameToType.get(fieldname);
        }
        else
        {
            return null;
        }
    }
}
