package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

/**
 * @author oliver langer
 */
public class XMPAlternatives extends XMPArrayBase
{
    public XMPAlternatives(final String namespace, final String name)
    {
        super(namespace, name);
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.ALTERNATIVES;
    }
}
