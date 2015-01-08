package de.dpa.metadatamapper.imaging.xmp.metadata;

import java.io.Serializable;

/**
 * @author oliver langer
 */
public abstract class XMPBase implements XMPMetadata, Serializable
{
    protected String namespace;

    protected XMPBase()
    {
    }

    public XMPBase(final String namespace)
    {
        this.namespace = namespace;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(final String namespace)
    {
        this.namespace = namespace;
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

        final XMPBase that = (XMPBase) o;

        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return namespace != null ? namespace.hashCode() : 0;
    }

    @Override public String toString()
    {
        return "XMPMetadata{" +
                "namespace='" + namespace + '\'' +
                '}';
    }
}
