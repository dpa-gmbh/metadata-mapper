package de.dpa.metadata.imaging.xmp.parser;

import de.dpa.metadata.imaging.xmp.metadata.XMPCollection;

/**
 * @author oliver langer
 */
public class XMPMetadataFactoryContext
{
    private XMPMetadataItemPath pathItem;
    private XMPCollection xmpMetadata;

    protected XMPMetadataFactoryContext(final XMPMetadataItemPath pathItem, final XMPCollection collection)
    {
        this.pathItem = pathItem;
        this.xmpMetadata = collection;
    }

    public XMPMetadataItemPath getPathItem()
    {
        return pathItem;
    }

    public XMPCollection getMetadataCollection()
    {
        return xmpMetadata;
    }

    @Override public String toString()
    {
        return "XMPMetadataBaseContext{" +
                ", class='" + getClass().getSimpleName()+ '\'' +
                ", pathItem=" + pathItem +
                '}';
    }
}
