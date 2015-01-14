package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author oliver langer
 */
public class XMPDate extends XMPNamedBase
{
    private final Date date;

    public XMPDate(final String namespace, final String name, final Date date)
    {
        super(namespace, name);
        this.date = date;
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.DATE;
    }

    public Date getDate()
    {
        return date;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof XMPDate))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final XMPDate that = (XMPDate) o;

        if (date != null ? !date.equals(that.date) : that.date != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPDateNamedMetadata{" +
                "date=" + date +
                '}';
    }

    public static void registerGsonHelper( final GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter( XMPDate.class,
                new JsonSerializer<XMPDate>() {
                    @Override public JsonElement serialize(final XMPDate xmpDateNamedMetadata, final Type type,
                            final JsonSerializationContext jsonSerializationContext)
                    {
                        JsonObject jsonObject = XMPNamedBase.serialize(xmpDateNamedMetadata);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSXXX");
                        StringBuffer stringBuffer = new StringBuffer();
                        if( xmpDateNamedMetadata.getDate() != null )
                        {
                            simpleDateFormat.format(xmpDateNamedMetadata.getDate(), stringBuffer, new FieldPosition(0));
                        }
                        jsonObject.addProperty("value", stringBuffer.toString());
                        return jsonObject;
                    }
                });
    }
}
