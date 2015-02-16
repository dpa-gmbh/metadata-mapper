package de.dpa.oss.metadata.mapper.imaging.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.*;
import de.dpa.oss.metadata.mapper.imaging.EncodingCharset;
import de.dpa.oss.metadata.mapper.imaging.xmp.metadata.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oliver langer
 */
public class ImageMetadata implements Serializable
{
    private EncodingCharset iimCharset = EncodingCharset.ISO_8859_1;
    private String exifCharset = "iso8859-15";
    private EncodingCharset xmpCharset = EncodingCharset.UTF8;
    
    private ListMultimap<String,String> exifEntries = ArrayListMultimap.create();
    private ListMultimap<String,String> iptcEntries = ArrayListMultimap.create();
    private List<XMPMetadata> xmpMetadata = new ArrayList<>();

    public void addExifEntry(final String key, final String value) throws Exception
    {
          exifEntries.put(key,value);
    }

    public void addIPTCEntry(final String key, final String value)
    {
        iptcEntries.put( key, value );
    }

    public void addXMPMetadata( final XMPMetadata xmpMetadata ) {
        this.xmpMetadata.add( xmpMetadata );
    }

    public void addXMPMetadata(final List<XMPMetadata> xmpMetadata)
    {
        this.xmpMetadata.addAll(xmpMetadata);
    }

    public String getJson()
    {
        return createGson().toJson(this);
    }

    public ListMultimap<String, String> getIptcEntries()
    {
        return iptcEntries;
    }

    public List<XMPMetadata> getXmpMetadata()
    {
        return xmpMetadata;
    }

    public EncodingCharset getIimCharset()
    {
        return iimCharset;
    }

    public void setIimCharset(final EncodingCharset iimCharset)
    {
        this.iimCharset = iimCharset;
    }

    public String getExifCharset()
    {
        return exifCharset;
    }

    public void setExifCharset(final String exifCharset)
    {
        this.exifCharset = exifCharset;
    }

    public EncodingCharset getXmpCharset()
    {
        return xmpCharset;
    }

    public void setXmpCharset(final EncodingCharset xmpCharset)
    {
        this.xmpCharset = xmpCharset;
    }

    private Gson createGson()
    {
        GsonBuilder gsonBuilder = new GsonBuilder();

        XMPString.registerGsonHelper(gsonBuilder);
        XMPDate.registerGsonHelper(gsonBuilder);
        XMPQualifier.registerGsonHelper(gsonBuilder);
        XMPStruct.registerGsonHelper(gsonBuilder);
        XMPSchema.registerGsonHelper(gsonBuilder);
        XMPUnknown.registerGsonHelper(gsonBuilder);
        ImageMetadata.registerGsonHelper(gsonBuilder);
        XMPLocalizedText.registerGsonHelper(gsonBuilder);
        XMPRootCollection.registerGsonHelper(gsonBuilder);

        return gsonBuilder.create();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ImageMetadata))
        {
            return false;
        }

        final ImageMetadata that = (ImageMetadata) o;

        if (exifCharset != null ? !exifCharset.equals(that.exifCharset) : that.exifCharset != null)
        {
            return false;
        }
        if (exifEntries != null ? !exifEntries.equals(that.exifEntries) : that.exifEntries != null)
        {
            return false;
        }
        if (iimCharset != null ? !iimCharset.equals(that.iimCharset) : that.iimCharset != null)
        {
            return false;
        }
        if (iptcEntries != null ? !iptcEntries.equals(that.iptcEntries) : that.iptcEntries != null)
        {
            return false;
        }
        if (xmpCharset != null ? !xmpCharset.equals(that.xmpCharset) : that.xmpCharset != null)
        {
            return false;
        }
        if (xmpMetadata != null ? !xmpMetadata.equals(that.xmpMetadata) : that.xmpMetadata != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = iimCharset != null ? iimCharset.hashCode() : 0;
        result = 31 * result + (exifCharset != null ? exifCharset.hashCode() : 0);
        result = 31 * result + (xmpCharset != null ? xmpCharset.hashCode() : 0);
        result = 31 * result + (exifEntries != null ? exifEntries.hashCode() : 0);
        result = 31 * result + (iptcEntries != null ? iptcEntries.hashCode() : 0);
        result = 31 * result + (xmpMetadata != null ? xmpMetadata.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return getJson();
    }

    public static void registerGsonHelper(final GsonBuilder gsonBuilder)
    {
        gsonBuilder.registerTypeAdapter(ImageMetadata.class, new JsonSerializer<ImageMetadata>()
        {
            @Override public JsonElement serialize(final ImageMetadata imageMetadata, final Type type,
                    final JsonSerializationContext jsonSerializationContext)
            {
                JsonObject jsonObject = new JsonObject();

                JsonElement exifJson = jsonSerializationContext.serialize(imageMetadata.exifEntries);
                jsonObject.add("exif", exifJson);

                JsonElement iptcJson = jsonSerializationContext.serialize(imageMetadata.iptcEntries);

                jsonObject.add("iptc", iptcJson);
                JsonElement xmpJson = jsonSerializationContext.serialize(imageMetadata.xmpMetadata);
                jsonObject.add("xmp", xmpJson);

                return jsonObject;
            }
        });
    }
}
