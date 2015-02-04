package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagInfo;

/**
 * @author oliver langer
 */
public class ConfigValidationException extends Exception
{
    private final String metadataMappingName;
    private final String configuredNamespace;
    private final String configuredFieldname;

    public ConfigValidationException(final String metadataMappingName, final String configuredNamespace, final String configuredFieldname)
    {
        super("Error in metadata mapping named \"" + metadataMappingName
                + "\": configured <namespace>:<tag>=" + configuredNamespace + ":" + configuredFieldname + " not supported");
        this.metadataMappingName = metadataMappingName;
        this.configuredFieldname = configuredFieldname;
        this.configuredNamespace = configuredNamespace;
    }

    public String getConfiguredFieldname()
    {
        return configuredFieldname;
    }

    public String getConfiguredNamespace()
    {
        return configuredNamespace;
    }

    public String getMetadataMappingName()
    {
        return metadataMappingName;
    }
}
