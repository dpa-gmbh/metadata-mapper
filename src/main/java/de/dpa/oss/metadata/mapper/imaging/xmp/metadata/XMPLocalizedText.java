package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * @author oliver langer
 */
public class XMPLocalizedText extends XMPNamedBase
{
    public static final String DEFAULT_LANGUAGE = "x-default";
    private final String languageRFC3066IDOrXDefault;
    private final String localizedText;

    public XMPLocalizedText(final String namespace, final String name, final String localizedText)
    {
        super(namespace, name);
        this.localizedText = localizedText;
        this.languageRFC3066IDOrXDefault = DEFAULT_LANGUAGE;
    }

    public XMPLocalizedText(final String namespace, final String name, final String languageRFC3066IDOrXDefault, final String localizedText)
    {
        super(namespace, name);
        this.languageRFC3066IDOrXDefault = languageRFC3066IDOrXDefault;
        this.localizedText = localizedText;
    }

    @Override public XMPMetadataType getType()
    {
        return XMPMetadataType.LOCALIZED_TEXT;
    }

    public String getLanguageRFC3066IDOrXDefault()
    {
        return languageRFC3066IDOrXDefault;
    }

    public String getLocalizedText()
    {
        return localizedText;
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

        final XMPLocalizedText that = (XMPLocalizedText) o;

        if (languageRFC3066IDOrXDefault != null ?
                !languageRFC3066IDOrXDefault.equals(that.languageRFC3066IDOrXDefault) :
                that.languageRFC3066IDOrXDefault != null)
        {
            return false;
        }
        if (localizedText != null ? !localizedText.equals(that.localizedText) : that.localizedText != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (languageRFC3066IDOrXDefault != null ? languageRFC3066IDOrXDefault.hashCode() : 0);
        result = 31 * result + (localizedText != null ? localizedText.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "XMPLocalizedText{" +
                "languageRFC3066IDOrXDefault='" + languageRFC3066IDOrXDefault + '\'' +
                ", localizedText='" + localizedText + '\'' +
                '}';
    }

    public static void registerGsonHelper(final GsonBuilder gsonBuilder)
    {
        gsonBuilder.registerTypeAdapter(XMPLocalizedText.class,
                new JsonSerializer<XMPLocalizedText>()
                {
                    @Override public JsonElement serialize(final XMPLocalizedText xmpLocalizedText, final Type type,
                            final JsonSerializationContext jsonSerializationContext)
                    {
                        JsonObject jsonObject = XMPNamedBase.serialize(xmpLocalizedText);
                        jsonObject.addProperty("localizedText", xmpLocalizedText.getLocalizedText());
                        jsonObject.addProperty("language", xmpLocalizedText.getLanguageRFC3066IDOrXDefault());
                        return jsonObject;
                    }
                });
    }
}
