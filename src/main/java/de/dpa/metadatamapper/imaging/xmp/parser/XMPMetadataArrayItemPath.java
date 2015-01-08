package de.dpa.metadatamapper.imaging.xmp.parser;

/**
 * @author oliver langer
 */
public class XMPMetadataArrayItemPath extends XMPMetadataItemPath
{
    private int arrayIndex;

    public XMPMetadataArrayItemPath(final String prefix, final String path, final int arrayIndex)
    {
        super(prefix, path);
        this.arrayIndex = arrayIndex;
    }

    public int getArrayIndex()
    {
        return arrayIndex;
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
        if (!super.equals(o))
        {
            return false;
        }

        final XMPMetadataArrayItemPath that = (XMPMetadataArrayItemPath) o;

        if (arrayIndex != that.arrayIndex)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + arrayIndex;
        return result;
    }

    @Override public String toString()
    {
        return "" + prefix + ":" + path + "[" + arrayIndex + "]";
    }
}
