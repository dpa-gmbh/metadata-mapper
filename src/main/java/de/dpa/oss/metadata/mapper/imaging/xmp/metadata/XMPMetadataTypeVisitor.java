package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import com.adobe.xmp.XMPException;

/**
 * @author oliver langer
 */
public interface XMPMetadataTypeVisitor<T>
{
    T visitString(final XMPString entry) throws XMPException;

    T visitLocalizedText(final XMPLocalizedText entry) throws XMPException;

    T visitInteger(final XMPInteger entry) throws XMPException;

    T visitDate(final XMPDate entry) throws XMPException;

    T visitRoot(final XMPRootCollection entry) throws XMPException;

    T visitSchema(final XMPSchema entry) throws XMPException;

    T visitQualifier(final XMPQualifier entry) throws XMPException;

    T visitStruct(final XMPStruct entry) throws XMPException;

    T visitBag(final XMPBag entry) throws XMPException;

    T visitSequence(final XMPSequence entry) throws XMPException;

    T visitAlternatives(XMPAlternatives entry) throws XMPException;
}
