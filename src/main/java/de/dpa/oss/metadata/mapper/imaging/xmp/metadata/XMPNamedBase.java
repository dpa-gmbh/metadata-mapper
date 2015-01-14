package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * @author oliver langer
 */
public abstract class XMPNamedBase extends XMPBase implements Serializable
{
    protected String name;

    public XMPNamedBase()
    {
    }

    public XMPNamedBase(final String namespace, final String name)
    {
        super(namespace);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
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

        final XMPNamedBase that = (XMPNamedBase) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPNamedMetadata{" +
                "name='" + name + '\'' +
                '}';
    }

    public static JsonObject serialize( final XMPNamedBase xmpNamedBase) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty( "namespace", xmpNamedBase.getNamespace());
        jsonObject.addProperty( "name", xmpNamedBase.getName());

        return jsonObject;
    }
}
