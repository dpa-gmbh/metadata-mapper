package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.google.gson.*;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Used in case the OPTIONS value is unknown
 *
 * @author oliver langer
 */
public class XMPUnknown extends XMPBase implements Serializable
{
    private String path;
    private String value;

    public XMPUnknown(final String namespace, final String path, final String value)
    {
        super(namespace);
        this.path = path;
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public String getPath()
    {
        return path;
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

        final XMPUnknown that = (XMPUnknown) o;

        if (path != null ? !path.equals(that.path) : that.path != null)
        {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPUnknownMetadata{" +
                "path='" + path + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static void registerGsonHelper( final GsonBuilder gsonBuilder ) {
        gsonBuilder.registerTypeAdapter( XMPUnknown.class, new JsonSerializer<XMPUnknown>() {
            @Override public JsonElement serialize(final XMPUnknown xmpUnknownMetadata, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("namespace", xmpUnknownMetadata.getNamespace());
                jsonObject.addProperty("path", xmpUnknownMetadata.getPath());
                jsonObject.addProperty("value", xmpUnknownMetadata.getValue());
                return null;
            }
        });
    }

    @Override public XMPMetadataType getType()
    {

        return null;
    }
}
