package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.google.gson.*;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * @author oliver langer
 */
public class XMPSchema extends XMPBase implements Serializable
{
    public XMPSchema(final String namespace)
    {
        super(namespace);
    }

    public static void registerGsonHelper(final GsonBuilder gsonBuilder)
    {
        gsonBuilder.registerTypeAdapter(XMPSchema.class, new JsonSerializer<XMPSchema>()
        {
            @Override public JsonElement serialize(final XMPSchema xmpSchemaMetadata, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty( "namespace", xmpSchemaMetadata.getNamespace());

                return jsonObject;
            }
        });
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.SCHEMA;
    }
}
