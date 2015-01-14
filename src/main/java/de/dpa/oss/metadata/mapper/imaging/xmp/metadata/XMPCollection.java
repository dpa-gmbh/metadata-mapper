package de.dpa.oss.metadata.mapper.imaging.xmp.metadata;

import java.util.List;

/**
 * @author oliver langer
 */
public interface XMPCollection extends XMPMetadata
{
    void add( final XMPMetadata itemToAdd );
    List<XMPMetadata> getMetadata();
    boolean hasItems();
}
