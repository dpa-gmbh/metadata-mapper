package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oliver langer
 */
public class XMPRootCollection extends XMPBase implements XMPCollection, Serializable
{
    private final List<XMPMetadata> metadataItems;

    public XMPRootCollection()
    {
        this.metadataItems = new ArrayList<>();
    }

    @Override public void add(final XMPMetadata itemToAdd)
    {
        metadataItems.add(itemToAdd);
    }

    @Override public List<XMPMetadata> getMetadata()
    {
        return metadataItems;
    }

    @Override public boolean hasItems()
    {
        return metadataItems.size()>0;
    }

    public static void registerGsonHelper( final GsonBuilder gsonBuilder ) {
        gsonBuilder.registerTypeAdapter( XMPRootCollection.class, new JsonSerializer<XMPRootCollection>() {
            @Override public JsonElement serialize(final XMPRootCollection xmpRootMetadataCollection, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                return jsonSerializationContext.serialize( xmpRootMetadataCollection.metadataItems);
            }
        });
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.ROOT_COLLECTION;
    }
}
