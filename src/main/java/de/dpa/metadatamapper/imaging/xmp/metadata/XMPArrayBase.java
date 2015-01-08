package de.dpa.metadatamapper.imaging.xmp.metadata;

import com.google.gson.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oliver langer
 */
abstract class XMPArrayBase extends XMPNamedBase implements XMPCollection, Serializable
{
    private List<XMPMetadata> metadataList = new ArrayList<>();

    public XMPArrayBase(final String namespace, final String name)
    {
        super(namespace, name);
    }

    public void add(final XMPMetadata item)
    {
        metadataList.add(item);
    }

    @Override public List<XMPMetadata> getMetadata()
    {
        return metadataList;
    }

    @Override public boolean hasItems()
    {
        return metadataList.size()>0;
    }

    public List<XMPMetadata> getItems()
    {
        return metadataList;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof XMPArrayBase))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final XMPArrayBase that = (XMPArrayBase) o;

        if (metadataList != null ? !metadataList.equals(that.metadataList) : that.metadataList != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (metadataList != null ? metadataList.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPArrayNamedMetadata{" +
                "metadataList=" + metadataList +
                '}';
    }

    public static void registerGsonHelper(final GsonBuilder gsonBuilder)
    {
        gsonBuilder.registerTypeAdapter(XMPArrayBase.class, new JsonSerializer<XMPArrayBase>()
        {
            @Override public JsonElement serialize(final XMPArrayBase xmpArray, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                JsonElement arrayContent = jsonSerializationContext.serialize(xmpArray.metadataList);
                JsonObject json = XMPNamedBase.serialize(xmpArray);
                json.add("values", arrayContent);

                return json;
            }
        });
    }
}
