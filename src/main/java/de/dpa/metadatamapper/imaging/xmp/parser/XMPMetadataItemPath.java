package de.dpa.metadatamapper.imaging.xmp.parser;

import java.util.UUID;

/**
 * @author oliver langer
 */
public class XMPMetadataItemPath
{
    public static XMPMetadataItemPath ROOT_PATH_ITEM = new XMPMetadataItemPath(UUID.randomUUID().toString(),
            UUID.randomUUID().toString());

    final String prefix;
    final String path;

    public XMPMetadataItemPath(final String prefix, final String path)
    {
        this.prefix = prefix;
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String getPrefix()
    {
        return prefix;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final XMPMetadataItemPath that = (XMPMetadataItemPath) o;

        if (path != null ? !path.equals(that.path) : that.path != null)
        {
            return false;
        }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "" + prefix + ":" + path;
    }
}
