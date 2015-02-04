package de.dpa.oss.metadata.mapper.imaging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oliver langer
 */
public class ConfigToExifToolTagNames
{
    public static final String IPTC_APPLICATION_TAGGROUP_NAME = "IPTC::ApplicationRecord";
    private static Map<String, String> configNamespaceToTagGroupname;

    static
    {
        configNamespaceToTagGroupname = new HashMap<>();
        configNamespaceToTagGroupname.put("http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/", "XMP::iptcCore");
        configNamespaceToTagGroupname.put("http://iptc.org/std/Iptc4xmpExt/2008-02-29/", "XMP::iptcExt");
        configNamespaceToTagGroupname.put("http://purl.org/dc/elements/1.1/", "XMP::dc");
        configNamespaceToTagGroupname.put("http://ns.adobe.com/photoshop/1.0/", "XMP::photoshop");
        configNamespaceToTagGroupname.put("http://ns.adobe.com/xap/1.0/rights/", "XMP::xmpRights");
        configNamespaceToTagGroupname.put("http://ns.useplus.org/ldf/xmp/1.0/", "XMP::plus");
    }

    public static String getTagGroupnameByConfigNamespace(final String configNamespace)
    {
        if (configNamespaceToTagGroupname.containsKey(configNamespace))
        {
            return configNamespaceToTagGroupname.get(configNamespace);
        }
        else
        {
            return null;
        }
    }
}
