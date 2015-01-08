package de.dpa.metadatamapper.imaging.xmp.metadata;

/**
 * @author oliver langer
 */
public class XMPSequence extends XMPArrayBase
{
    public XMPSequence(final String namespace, final String name)
    {
        super(namespace, name);
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.SEQUENCE;
    }
}
