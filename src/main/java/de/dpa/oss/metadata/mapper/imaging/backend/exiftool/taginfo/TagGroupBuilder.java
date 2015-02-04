package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo;

/**
 * @author oliver langer
 */
public class TagGroupBuilder
{
    private String informationType;
    private String specificLocation;

    private TagGroupBuilder()
    {
    }

    public static TagGroupBuilder aTagGroup()
    {
        return new TagGroupBuilder();
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

    public TagGroupBuilder but()
    {
        return aTagGroup().withInformationType(informationType).withSpecificLocation(specificLocation);
    }

    public TagGroup build()
    {
        TagGroup tagGroup = new TagGroup(informationType, specificLocation);
        return tagGroup;
    }
}
