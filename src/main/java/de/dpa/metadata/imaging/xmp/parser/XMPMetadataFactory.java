package de.dpa.metadata.imaging.xmp.parser;

import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.google.common.base.Strings;
import de.dpa.metadata.imaging.xmp.metadata.*;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oliver langer
 */
public class XMPMetadataFactory
{
    public static Logger logger = Logger.getLogger(XMPMetadataFactory.class);
    /**
     * Allow strings as follows "ns:name[arrayIndex]"
     * where "[arrayIndex]" is optional
     */
    private final Pattern namePattern = Pattern.compile("([^:]*:)?([^\\Q[\\E]*)(.*)");

    public List<XMPMetadata> buildXMPMetadata(final XMPIterator iterator)
    {
        final XMPMetadataFactoryContextBuilder contextBuilder = new XMPMetadataFactoryContextBuilder();

        while (iterator.hasNext())
        {
            XMPPropertyInfo prop = (XMPPropertyInfo) iterator.next();
            PropertyOptions options = prop.getOptions();
            try
            {
                createMetadataItem(contextBuilder, prop, options);
            }
            catch (Throwable t)
            {
                logger.warn("error while interpreting XMP metadata: " + t);
            }
        }

        return contextBuilder.getMetadataList();
    }

    protected void createMetadataItem(final XMPMetadataFactoryContextBuilder context, final XMPPropertyInfo prop,
            final PropertyOptions options)
    {

        if (options.isSchemaNode())
        {
            context.addSingleMetadata("", createSchemaMetadata(prop));
        }
        else if (options.isQualifier())
        {
            context.addSingleMetadata(prop.getPath(), createQualifier(prop));
        }
        else if (options.isArray() || options.isArrayOrdered() || options.isArrayAlternate() || options.isArrayAltText())
        {
            context.addCollectionMetadata(prop.getPath(), createArray(prop, options));
        }
        else if (options.isStruct())
        {
            context.addStructMetadata(prop.getPath(), createStruct(prop));
        }
        else if (options.isSimple())
        {
            context.addSingleMetadata(prop.getPath(), createSimple(prop));
        }
        else
        {
            context.addSingleMetadata(prop.getPath(), createUnknown(prop));
        }
    }

    private XMPUnknown createUnknown(final XMPPropertyInfo prop)
    {
        return new XMPUnknown(namespaceOf(prop), prop.getPath(), prop.getValue().toString());
    }

    private XMPSchema createSchemaMetadata(final XMPPropertyInfo prop)
    {
        return new XMPSchema(prop.getNamespace());
    }

    private XMPQualifier createQualifier(final XMPPropertyInfo prop)
    {
        return new XMPQualifier(namespaceOf(prop), nameOf(prop), prop.getValue().toString());
    }

    private XMPStruct createStruct(final XMPPropertyInfo prop)
    {
        return new XMPStruct(prop.getNamespace(), nameOf(prop));
    }

    private XMPCollection createArray(final XMPPropertyInfo prop, final PropertyOptions options)
    {
        /** TODO FIXME: not sure whether this mapping is correct here. Need to verify */
        switch (options.getOptions())
        {
        case PropertyOptions.ARRAY_ORDERED:
            return new XMPSequence(prop.getNamespace(), nameOf(prop));
        case PropertyOptions.ARRAY_ALT_TEXT:
            return new XMPAlternatives(prop.getNamespace(), nameOf(prop));
        case PropertyOptions.ARRAY_ALTERNATE:
            return new XMPBag(prop.getNamespace(), nameOf(prop));
        default:
            return new XMPBag(prop.getNamespace(), nameOf(prop));
        }
    }

    protected XMPString createSimple(final XMPPropertyInfo prop)
    {

        return new XMPString(namespaceOf(prop), nameOf(prop), valueOf(prop));
    }

    protected String namespaceOf(final XMPPropertyInfo prop)
    {
        return prop.getNamespace();
    }

    protected String nameOf(final XMPPropertyInfo prop)
    {
        if (Strings.isNullOrEmpty(prop.getPath()))
        {
            return null;
        }
        Matcher matcher = namePattern.matcher(prop.getPath());

        if (matcher.matches())
        {
            return matcher.group(2);
        }
        else
        {
            return "";
        }
    }

    protected String valueOf(final XMPPropertyInfo prop)
    {
        if (prop.getValue() == null)
        {
            return "";
        }
        else
        {
            return prop.getValue().toString();
        }
    }
}
