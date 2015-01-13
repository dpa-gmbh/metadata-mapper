package de.dpa.metadatamapper.imaging.common;

import com.adobe.xmp.*;
import com.adobe.xmp.options.PropertyOptions;
import com.google.common.base.Strings;
import de.dpa.metadatamapper.imaging.iptc.IptcFieldToType;
import de.dpa.metadatamapper.imaging.xmp.metadata.*;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.iptc.*;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author oliver langer
 */
public class ImageMetadataWriter
{
    public static Logger logger = Logger.getLogger(ImageMetadataWriter.class);
    final private JpegXmpRewriter xmpRewriter;
    final private JpegIptcRewriter iptcRewriter;
    private final TimeZone timeZone;

    static
    {
        try
        {
            XMPMetaFactory.getSchemaRegistry().registerNamespace("http://iptc.org/std/Iptc4xmpExt/2008-02-29/", "Iptc4xmpExt");
            XMPMetaFactory.getSchemaRegistry().registerNamespace("http://ns.useplus.org/ldf/xmp/1.0/", "plus");
        }
        catch (XMPException e)
        {
            logger.error("Unable to register namespace: http://iptc.org/std/Iptc4xmpExt/2008-02-29/");
        }
    }

    public ImageMetadataWriter(final JpegXmpRewriter xmpRewriter, final JpegIptcRewriter iptcRewriter, final TimeZone timeZone)
    {
        this.xmpRewriter = xmpRewriter;
        this.iptcRewriter = iptcRewriter;
        this.timeZone = timeZone;
    }

    private void writeIptcMetadata(final InputStream inputStream, final ImageMetadata imageMetadata, final OutputStream outputStream)
            throws ImageWriteException, ImageReadException, IOException
    {
        List<IptcRecord> iptcRecords = new ArrayList<>();
        for (String key : imageMetadata.getIptcEntries().keySet())
        {
            IptcTypes iptcType = IptcFieldToType.asIptcType(key);

            for (String value : imageMetadata.getIptcEntries().get(key))
            {
                iptcRecords.add(new IptcRecord(iptcType, value.getBytes(imageMetadata.getIimCharset()), value));
            }
        }

        PhotoshopApp13Data newdata = new PhotoshopApp13Data(iptcRecords, new ArrayList<IptcBlock>());
        iptcRewriter.writeIPTC(inputStream, outputStream, newdata);
    }

    private static class OutputCollection
    {
        enum CollectionType
        {
            ROOT, ARRAY, STRUCT
        }

        final XMPMeta destinationCollection;
        OutputCollection parentCollection;

        CollectionType collectionType;

        private boolean isFirstInCollection;
        private String collectionNamespace;
        private String collectionFieldname;
        private PropertyOptions collectionPropertyOptions;

        private OutputCollection()
        {
            destinationCollection = XMPMetaFactory.create();
            parentCollection = null;
            collectionType = CollectionType.ROOT;
            isFirstInCollection = true;
        }

        private OutputCollection(final OutputCollection parentCollection,
                final CollectionType collectionType, final String collectionNamespace, final String collectionFieldname,
                final PropertyOptions collectionPropertyOptions)
        {
            this.isFirstInCollection = true;
            this.parentCollection = parentCollection;
            this.destinationCollection = parentCollection.destinationCollection;
            this.collectionType = collectionType;
            this.collectionNamespace = collectionNamespace;
            this.collectionFieldname = collectionFieldname;
            this.collectionPropertyOptions = collectionPropertyOptions;
        }

        public void addPropertyToCollection(final String fieldNamespace, final String fieldname, final String value,
                final PropertyOptions propertyOptions)
                throws XMPException
        {

            switch (collectionType)
            {
            case ARRAY:
                if (isFirstInCollection)
                {
                    destinationCollection
                            .appendArrayItem(this.collectionNamespace, this.collectionFieldname, this.collectionPropertyOptions,
                                    value, propertyOptions);
                    isFirstInCollection = false;
                }
                else
                {
                    destinationCollection.appendArrayItem(this.collectionNamespace, this.collectionFieldname, value);
                }
                break;

            case STRUCT:
                destinationCollection
                        .setStructField(this.collectionNamespace, this.collectionFieldname, fieldNamespace, fieldname, value);
                break;
            case ROOT:
                destinationCollection.setProperty(fieldNamespace, fieldname, value);
                break;

            }
        }

        public void addLocalizedTextToCollection(final String fieldNamespace, final String fieldName, final String language,
                final String localizedText) throws XMPException
        {
            if (collectionType != CollectionType.ROOT)
            {
                throw new IllegalStateException("Adding of localized text is supported for root collection only");
            }
            destinationCollection.setLocalizedText(fieldNamespace, fieldName, null, language, localizedText);
        }

        public OutputCollection pushNewCollection(final CollectionType newCollectionType, final String namespace,
                final String collectionName,
                final PropertyOptions propertyOptions)
        {
            if (collectionType == CollectionType.ARRAY && newCollectionType == CollectionType.STRUCT)
            {
                throw new UnsupportedOperationException("Usage of structs within arrays is not supported");
            }
            return new OutputCollection(this, newCollectionType, namespace, collectionName, propertyOptions);
        }

        public OutputCollection popCollection()
        {
            if (parentCollection == null)
            {
                throw new IllegalStateException("Trying to pop root collection");
            }
            return parentCollection;
        }

        public XMPMeta getFilledXMPMetadata()
        {
            return destinationCollection;
        }
    }

    private void visitCollection(final OutputCollection outputCollection,
            final XMPCollection srcMetadata) throws XMPException
    {
        for (XMPMetadata metadata : srcMetadata.getMetadata())
        {
            writeXMPMetadata(outputCollection, metadata);
        }
    }

    private void writeXMPMetadata(final OutputCollection outputCollection, final XMPMetadata srcMetadata)
            throws XMPException
    {
        srcMetadata.getType().accept(srcMetadata, new XMPMetadataTypeVisitor<Void>()
        {
            @Override public Void visitString(final XMPString entry) throws XMPException
            {
                if (!Strings.isNullOrEmpty(entry.getValue()))
                {
                    outputCollection.addPropertyToCollection(entry.getNamespace(), entry.getName(), entry.getValue(), null);
                }
                return null;
            }

            @Override public Void visitInteger(final XMPInteger entry) throws XMPException
            {
                outputCollection.addPropertyToCollection(entry.getNamespace(), entry.getName(),
                        Integer.toString(entry.getValue()), null);

                return null;
            }

            @Override public Void visitAlternatives(final XMPAlternatives entry) throws XMPException
            {
                if (!entry.hasItems())
                {
                    return null;
                }

                PropertyOptions propertyOptions = new PropertyOptions();
                propertyOptions.setArray(true).setArrayAlternate(true);

                OutputCollection altOutputCollection = outputCollection
                        .pushNewCollection(OutputCollection.CollectionType.ARRAY, entry.getNamespace(),
                                entry.getName(), propertyOptions);

                visitCollection(altOutputCollection, entry);
                altOutputCollection.popCollection();

                return null;
            }

            @Override public Void visitBag(final XMPBag entry) throws XMPException
            {
                if (!entry.hasItems())
                {
                    return null;
                }

                PropertyOptions propertyOptions = new PropertyOptions();
                propertyOptions.setArray(true).setArrayOrdered(false);

                OutputCollection bagOutputCollection = outputCollection
                        .pushNewCollection(OutputCollection.CollectionType.ARRAY, entry.getNamespace(),
                                entry.getName(), propertyOptions);

                visitCollection(bagOutputCollection, entry);
                bagOutputCollection.popCollection();

                return null;
            }

            @Override public Void visitSequence(final XMPSequence entry) throws XMPException
            {
                if (!entry.hasItems())
                {
                    return null;
                }

                PropertyOptions propertyOptions = new PropertyOptions();
                propertyOptions.setArrayOrdered(true);

                OutputCollection sequenceOutputCollection = outputCollection
                        .pushNewCollection(OutputCollection.CollectionType.ARRAY, entry.getNamespace(),
                                entry.getName(), propertyOptions);

                visitCollection(sequenceOutputCollection, entry);
                sequenceOutputCollection.popCollection();

                return null;
            }

            @Override public Void visitRoot(final XMPRootCollection entry) throws XMPException
            {
                return null;
            }

            @Override public Void visitSchema(final XMPSchema entry) throws XMPException
            {
                return null;
            }

            @Override public Void visitQualifier(final XMPQualifier entry) throws XMPException
            {
                return null;
            }

            @Override public Void visitStruct(final XMPStruct entry) throws XMPException
            {
                if (!entry.hasItems())
                {
                    return null;
                }

                OutputCollection arrayOutputCollection = outputCollection
                        .pushNewCollection(OutputCollection.CollectionType.STRUCT, entry.getNamespace(),
                                entry.getName(), new PropertyOptions().setStruct(true));

                visitCollection(arrayOutputCollection, entry);
                arrayOutputCollection.popCollection();

                return null;
            }

            @Override public Void visitLocalizedText(final XMPLocalizedText entry) throws XMPException
            {
                if (!Strings.isNullOrEmpty(entry.getLocalizedText()))
                {
                    outputCollection.addLocalizedTextToCollection(entry.getNamespace(), entry.getName(),
                            entry.getLanguageRFC3066IDOrXDefault(),
                            entry.getLocalizedText());
                }
                return null;
            }

            @Override public Void visitDate(final XMPDate entry) throws XMPException
            {
                if (entry.getDate() == null)
                {
                    return null;
                }

                GregorianCalendar calendar = new GregorianCalendar(timeZone);
                calendar.setTime(entry.getDate());
                XMPDateTime xmpDate = XMPDateTimeFactory.createFromCalendar(calendar);
                outputCollection.addPropertyToCollection(entry.getNamespace(), entry.getName(), XMPUtils.convertFromDate(xmpDate), null);
                return null;
            }
        });
    }

    private XMPMeta createXMPMetadata(final ImageMetadata imageMetadata) throws XMPException
    {
        OutputCollection rootCollection = new OutputCollection();

        for (XMPMetadata xmpMetadata : imageMetadata.getXmpMetadata())
        {
            writeXMPMetadata(rootCollection, xmpMetadata);
        }
        return rootCollection.getFilledXMPMetadata();
    }

    public void write(final InputStream inputStream, final ImageMetadata imageMetadata, final OutputStream outputStream)
            throws ImageWriteException, ImageReadException, IOException, XMPException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        logger.debug("Writing IPTC-Metadata");
        writeIptcMetadata(inputStream, imageMetadata, buffer);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer.toByteArray());
        buffer.close();

        logger.debug("Writing XMP-Metadata");
        XMPMeta xmpMetadata = createXMPMetadata(imageMetadata);

        xmpRewriter.updateXmpXml(byteArrayInputStream, outputStream,
                XMPMetaFactory.serializeToString(xmpMetadata, null));
        byteArrayInputStream.close();
    }
}
