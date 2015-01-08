package de.dpa.esb.imaging.xmp.metadata;

import com.google.gson.*;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * @author oliver langer
 */
public class XMPString extends XMPNamedBase implements Serializable
{
    private final String value;

    public XMPString(final String namespace, final String name, final String value)
    {
        super(namespace, name);
        this.value = value;
    }

    public String getValue()
    {
        return value;
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

        final XMPString that = (XMPString) o;

        if (value != null ? !value.equals(that.value) : that.value != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPStringNamedMetadata{" +
                "value='" + value + '\'' +
                '}';
    }

    public static void registerGsonHelper( final GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter( XMPString.class,
                new JsonSerializer<XMPString>() {
                    @Override public JsonElement serialize(final XMPString xmpStringNamedMetadata, final Type type,
                            final JsonSerializationContext jsonSerializationContext)
                    {
                        JsonObject jsonObject = XMPNamedBase.serialize(xmpStringNamedMetadata);
                        jsonObject.addProperty( "value", xmpStringNamedMetadata.getValue() );
                        return jsonObject;
                    }
                });
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.STRING;
    }
}
