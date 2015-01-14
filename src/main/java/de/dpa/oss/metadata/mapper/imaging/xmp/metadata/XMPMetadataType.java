package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.adobe.xmp.XMPException;

/**
 * @author oliver langer
 */
public enum XMPMetadataType
{
    STRING
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitString((XMPString) entry);
                }
            },
    DATE
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitDate((XMPDate) entry);
                }
            },
    BAG
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitBag((XMPBag) entry);
                }
            },
    SEQUENCE
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitSequence((XMPSequence) entry);
                }
            },
    ALTERNATIVES
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitAlternatives((XMPAlternatives) entry);
                }
            },
    ROOT_COLLECTION
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitRoot((XMPRootCollection) entry);
                }
            },
    SCHEMA
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {

                    return visitor.visitSchema((XMPSchema) entry);
                }
            },
    QUALIFIER
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {

                    return visitor.visitQualifier((XMPQualifier) entry);

                }
            },
    STRUCT
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitStruct((XMPStruct) entry);

                }
            },
    LOCALIZED_TEXT
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitLocalizedText((XMPLocalizedText) entry);
                }
            },
    INTEGER
            {
                @Override public <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException
                {
                    return visitor.visitInteger((XMPInteger) entry);
                }
            };

    public abstract <T> T accept(final XMPMetadata entry, final XMPMetadataTypeVisitor<T> visitor) throws XMPException;
}