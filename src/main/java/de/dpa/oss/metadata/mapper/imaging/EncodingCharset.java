package de.dpa.oss.metadata.mapper.imaging;

import com.google.common.base.Strings;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMEncodingType;

/**
 * @author oliver langer
 */
public enum EncodingCharset
{
    UTF8() {
        @Override public String charsetName()
        {
            return "utf-8";
        }
    },
    ISO_8859_1() {
        @Override public String charsetName()
        {
            return "iso-8859-1";
        }
    },
    ISO_8859_15() {
        @Override public String charsetName()
        {
            return "iso-8859-15";
        }
    };

    public static EncodingCharset of(IIMEncodingType iimEncodingType)
    {
        if( iimEncodingType == null )
        {
            return UTF8;
        }
        switch( iimEncodingType )
        {
        case ISO_8859_1:
            return ISO_8859_1;
        case UTF_8:
            return UTF8;
        default:
            throw new IllegalStateException( "Unknown configured encoding type: " + iimEncodingType );
        }
    }

    public static EncodingCharset of(final String encodingStr)
    {
        if(Strings.isNullOrEmpty(encodingStr ))
        {
            return UTF8;
        }
        if( "utf8".equalsIgnoreCase( encodingStr ) || "utf-8".equalsIgnoreCase(encodingStr))
        {
            return UTF8;
        }

        throw new IllegalStateException( "Unknown configured encoding type: " + encodingStr );
    }

    public abstract String charsetName();
}
