package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import java.io.Serializable;

/**
 * @author oliver langer
 */
public class XMPInteger extends XMPNamedBase implements Serializable
{
    private int value;

    public XMPInteger(final String namespace, final String name, final int value)
    {
        super(namespace, name);
        this.value = value;
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.INTEGER;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(final int value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof XMPInteger))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final XMPInteger that = (XMPInteger) o;

        if (value != that.value)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + value;
        return result;
    }

    @Override public String toString()
    {
        return "XMPIntegerNamedMetadata{" +
                "value=" + value +
                '}';
    }
}
