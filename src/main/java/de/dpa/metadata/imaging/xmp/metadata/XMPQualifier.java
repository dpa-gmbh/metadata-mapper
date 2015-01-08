package de.dpa.metadata.imaging.xmp.metadata;

import com.google.gson.*;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * @author oliver langer
 */
public class XMPQualifier extends XMPNamedBase implements Serializable
{
    final private String value;

    public XMPQualifier(final String namespace, final String name, final String value)
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

        final XMPQualifier that = (XMPQualifier) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);

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
        return "XMPQualifierMetadata{" +
                "value='" + value + '\'' +
                '}';
    }

    public static void registerGsonHelper(final GsonBuilder gsonBuilder)
    {
        gsonBuilder.registerTypeAdapter(XMPQualifier.class, new JsonSerializer<XMPQualifier>()
        {
            @Override public JsonElement serialize(final XMPQualifier xmpQualifierMetadata, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                JsonObject json = XMPNamedBase.serialize(xmpQualifierMetadata);
                json.addProperty("value", xmpQualifierMetadata.getValue());

                return json;
            }
        });
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.QUALIFIER;
    }
}
