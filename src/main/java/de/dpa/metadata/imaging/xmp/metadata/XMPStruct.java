package de.dpa.metadata.imaging.xmp.metadata;

import com.google.gson.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oliver langer
 */
public class XMPStruct extends XMPNamedBase implements XMPCollection, Serializable
{
    private List<XMPMetadata> members = new ArrayList<>();

    public XMPStruct(final String namespace, final String name)
    {
        super(namespace, name);
    }

    public void add(final XMPMetadata metadata)
    {
        members.add(metadata);
    }

    @Override public List<XMPMetadata> getMetadata()
    {
        return members;
    }

    @Override public boolean hasItems()
    {
        return members.size()>0;
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

        final XMPStruct that = (XMPStruct) o;

        if (members != null ? !members.equals(that.members) : that.members != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (members != null ? members.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPStructMetadata{" +
                "value=" + members +
                '}';
    }

    public static void registerGsonHelper(final GsonBuilder gsonBuilder)
    {
        gsonBuilder.registerTypeAdapter(XMPStruct.class, new JsonSerializer<XMPStruct>()
        {
            @Override public JsonElement serialize(final XMPStruct xmpStructMetadata, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                JsonObject json = XMPNamedBase.serialize(xmpStructMetadata);
                for (XMPMetadata item : xmpStructMetadata.members)
                {
                    json.add("value", jsonSerializationContext.serialize(item));
                }

                return json;
            }
        });
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.STRUCT;
    }
}
