package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oliver langer
 */
public class TagGroupBuilder
{
    private String informationType;
    private String specificLocation;
    Map<String,TagGroupItem> idToTagGroupItem;
    private String name;

    private TagGroupBuilder()
    {
        idToTagGroupItem = new HashMap<>();
    }

    public static TagGroupBuilder aTagGroup()
    {
        return new TagGroupBuilder();
    }

    public TagGroupBuilder withName(final String name )
    {
        this.name = name;
        return this;
    }

    public TagGroupBuilder withInformationType(String informationType)
    {
        this.informationType = informationType;
        return this;
    }

    public TagGroupBuilder withSpecificLocation(String specificLocation)
    {
        this.specificLocation = specificLocation;
        return this;
    }

    public TagGroupBuilder addTagGroupItem( final String id, final String name, final String type, final boolean isWritable)
    {
        idToTagGroupItem.put(id, new TagGroupItem(id, name, type, isWritable));

        return this;
    }
    public TagGroup build()
    {
        TagGroup tagGroup = new TagGroup(name, informationType, specificLocation, idToTagGroupItem);
        return tagGroup;
    }
}
