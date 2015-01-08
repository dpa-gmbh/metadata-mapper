package de.dpa.metadatamapper.imaging.xmp.metadata;

/**
 * @author oliver langer
 */
public class XMPBag extends XMPArrayBase
{
    public XMPBag(final String namespace, final String name)
    {
        super(namespace, name);
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.BAG;
    }
}
